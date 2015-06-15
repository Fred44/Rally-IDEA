package com.flefebv.rally.jetbrains.tasks;

import com.flefebv.rally.model.RallyProject;
import com.flefebv.rally.model.RallyTask;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.mantis.MantisProject;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import org.apache.axis.AxisFault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author flefebv
 *         Date: 05/06/2015
 */
@Tag("Rally")
public class RallyRepository extends BaseRepository {

    private static final Logger LOG = Logger.getInstance(RallyRepository.class);

    private static final Pattern ID_PATTERN = Pattern.compile("\\d+");


    private List<RallyProject> myRallyProjects = null;
    private RallyProject rallyProject;

    public RallyRepository () {}

    public RallyRepository (TaskRepositoryType type) {
        super(type);
    }

    public RallyRepository (RallyRepository other) {
        super(other);
        rallyProject = other.rallyProject;
        myRallyProjects = other.myRallyProjects;
    }

    private void ensureProjectsRefreshed() throws Exception {
        if (myRallyProjects == null) refreshProjects();
    }

    public void refreshProjects () throws Exception {
        RallyRestApi api = null;
        myRallyProjects = fetchOpenProjects();
    }

    public List<RallyProject> getProjects () throws Exception {
        ensureProjectsRefreshed();
        return myRallyProjects == null ? Collections.<RallyProject>emptyList() : myRallyProjects;
    }

    private List<RallyProject> fetchOpenProjects () throws IOException, URISyntaxException {
        RallyRestApi api = null;
        List<RallyProject> projects = new ArrayList<>();

        try {
            api = rallyAPI();
            QueryResponse resp = api.query(RallyProject.openProjectsQuery("ObjectID", "CreationDate", "Name"));
            if (resp.wasSuccessful()) {
                LOG.debug(resp.getTotalResultCount() + " projects fetched");

                for (JsonElement jsObj : resp.getResults()) {
                    projects.add(new RallyProject(jsObj.getAsJsonObject()));
                }
            } else handleServerError(resp);
            return projects;
        } finally {
            if (api != null) api.close();
        }
    }

    private List<RallyTask> fetchTasks (String query, int offset, int limit, boolean withClosed) throws URISyntaxException, IOException {
        List<RallyTask> tasks = new ArrayList<RallyTask>();
        RallyRestApi rally = null;
        QueryFilter filter = null;

        try {
            rally = rallyAPI();

            QueryRequest q = RallyTask.createQuery("ObjectID", "CreationDate", "FormattedID", "Name", "State", "Description");
            if (rallyProject != null) {
                q.setProject(rallyProject.getRef());
            }
            filter = new QueryFilter("Owner", "=", getUsername());
            if (StringUtil.isNotEmpty(query))
                filter = filter.and(new QueryFilter("FormattedID", "contains", query).or(new QueryFilter("Name", "contains", query)));
            if (!withClosed)
                filter = filter.and(new QueryFilter("State", "<", RallyTask.State.Completed.toString()));
            if (filter != null)
                q.setQueryFilter(filter);

            LOG.debug("fetch Task with = " + filter);
            q.setStart(offset);
            q.setLimit(limit);
            q.setOrder("LastUpdateDate DESC");
            QueryResponse queryResponse = rally.query(q);
            if (queryResponse.wasSuccessful()) {
                LOG.debug(queryResponse.getTotalResultCount() + " tasks fetched");

                for (JsonElement result : queryResponse.getResults()) {
                    tasks.add(new RallyTask(result.getAsJsonObject()));
                }
            } else handleServerError(queryResponse);
            return tasks;

        } finally {
            if (rally != null) {
                rally.close();
            }
        }
    }

    @Nullable
    @Override
    public String extractId(@NotNull String taskName) {
        Matcher matcher = ID_PATTERN.matcher(taskName);
        return matcher.find() ? matcher.group() : null;
    }

    @Nullable
    @Override
    public Task findTask (@NotNull String id) throws Exception {
        RallyRestApi api = null;
//        System.out.println("Find : " + id);
        try {
            api = rallyAPI();

            GetResponse resp = api.get(new GetRequest("/task/" + id + ".js"));
            if (resp.wasSuccessful()) {
                return new ArtifactTask(new RallyTask(resp.getObject()), this);
            } else {
                return null;
            }
        } finally {
            if (api != null) {
                api.close();
            }
        }
    }

    @Override
    public Task[] getIssues (@Nullable final String query, final int offset, final int limit, final boolean withClosed, @NotNull ProgressIndicator cancelled) throws Exception {
        if (rallyProject == null) throw new Exception(TaskBundle.message("failure.configuration"));

        List<ArtifactTask> tasks = new ArrayList<ArtifactTask>();

        List<RallyTask> rallyTasks = fetchTasks(query, offset, limit, withClosed);
        for (RallyTask t : rallyTasks) {
            tasks.add(new ArtifactTask(t, this));
        }
        return tasks.toArray(new Task[0]);
    }

    @NotNull
    @Override
    public BaseRepository clone () {
        return new RallyRepository(this);
    }

    @Nullable
    @Override
    public TaskRepository.CancellableConnection createCancellableConnection () {

        return new TaskRepository.CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                RallyRestApi rally = null;
                try {
                    rally = rallyAPI();

                    QueryRequest q = new QueryRequest("user");
                    q.setFetch(new Fetch("UserProfile.DefaultProject"));
                    q.setQueryFilter(new QueryFilter("UserName", "=", getUsername()));
                    QueryResponse resp = rally.query(q);
                    if (!resp.wasSuccessful()) {
                        throw new Exception("Connection failed");
                    }
                } finally {
                    if (rally != null) {
                        rally.close();
                    }
                }
            }

            @Override
            public void cancel() {}
        };
    }

    private Exception handleException(@NotNull Exception e) throws Exception {
        if (e instanceof IOException) {
            throw new Exception(TaskBundle.message("failure.server.message", e.getMessage()), e);
        }
        throw e;
    }

    private void handleServerError(QueryResponse response) {
        String allErrors = "";
        LOG.error("Rally query failed");
        for (String err : response.getErrors()) {
            LOG.error("\t" + err);
            allErrors += "\n" + "err";
        }
        throw new RuntimeException(TaskBundle.message("failure.server.message", allErrors));
    }

    private RallyRestApi rallyAPI () throws URISyntaxException {
        RallyRestApi restApi = new RallyRestApi(new URI(getUrl()), getUsername(), getPassword());
        if (isUseProxy()) {
            HttpConfigurable proxy = HttpConfigurable.getInstance();
            restApi.setProxy(new URI("http", null, proxy.PROXY_HOST, proxy.PROXY_PORT, null, null, null));
        }
        restApi.setApplicationName("Rally-IDEA");
        restApi.setWsapiVersion("v2.0");
        return restApi;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        if (!(o instanceof RallyRepository)) return false;
        RallyRepository repository = (RallyRepository)o;

        if (!Comparing.equal(rallyProject, repository.rallyProject)) return false;

        return true;
    }

    @Override
    public boolean isConfigured () {
        return super.isConfigured()  && StringUtil.isNotEmpty(getUsername()) && StringUtil.isNotEmpty(getPassword());
    }

    @Nullable
    public RallyProject getRallyProject() {
        return rallyProject;
    }

    public void setRallyProject (@Nullable RallyProject rallyProject) {
        this.rallyProject = rallyProject;
    }
}
