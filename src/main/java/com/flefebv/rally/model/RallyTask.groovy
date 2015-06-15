package com.flefebv.rally.model;

import com.google.gson.JsonObject;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.util.Fetch;

/**
 * Created with IntelliJ IDEA.
 *
 * @author flefebv
 *         Date: 08/06/2015
 */
class RallyTask extends RallyArtifact {

    enum State { Defined, InProgress, Completed }

    State state

    RallyTask (JsonObject obj) {
        super(obj)
        this.state = obj.get("State").getAsString().replaceAll("-", "")
    }

    RallyTask (long objectID, Date creationDate, String formattedID, String name, String state) {
        super(objectID, creationDate, formattedID, name)
        this.state = state.replaceAll("-", "")
    }

    static QueryRequest createQuery (String... fields) {
        QueryRequest q = new QueryRequest("task")
        q.setFetch(new Fetch(fields))
        q.setOrder("LastUpdateDate DESC")
        return q
    }
}
