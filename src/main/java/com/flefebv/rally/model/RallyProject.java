package com.flefebv.rally.model;

import com.google.gson.JsonObject;
import com.intellij.util.xmlb.annotations.Attribute;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Store Rally Project properties.
 *
 * @author flefebv
 *         Date: 08/06/2015
 */
public class RallyProject extends RallyObject {

    static final String TYPE = "project";

    public enum State {
        Open,
        Closed
    }

    private String name;

    public RallyProject() {}

    public RallyProject (JsonObject obj) {
        super(obj);
        this.name = obj.get("Name").getAsString();
    }

    public RallyProject (long objectID, Date creationDate, String name) {
        super(objectID, creationDate);
        this.name = name;
    }

    public String getName () {
        return name;
    }

    public void setName (@NotNull String name) {
        this.name = name;
    }

    @Override
    public String toString () {
        return "RallyProject{" +
                "objectID='" + objectID + '\'' +
                "name='" + name + '\'' +
                '}';
    }

    public static QueryRequest createQuery (String... fields) {
        QueryRequest q = new QueryRequest(TYPE);
        q.setFetch(new Fetch(fields));
        return q;
    }

    public static QueryRequest openProjectsQuery (String... fields) {
        QueryRequest q = createQuery(fields);
        q.setQueryFilter(new QueryFilter("State", "=", State.Open.name()));
        q.setOrder("Name ASC");
        return q;
    }
}
