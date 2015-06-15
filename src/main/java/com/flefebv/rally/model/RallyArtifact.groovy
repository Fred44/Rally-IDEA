package com.flefebv.rally.model;

import com.google.gson.JsonObject;

/**
 * Created with IntelliJ IDEA.
 *
 * @author flefebv
 *         Date: 08/06/2015
 */
abstract class RallyArtifact extends RallyObject {

    final String formattedID
    String name
    String description
    Date lastUpdateDate

    RallyArtifact (JsonObject obj) {
        super(obj)
        this.formattedID = obj.get("FormattedID").getAsString()
        this.name = obj.get("Name").getAsString()
        if (obj.has("Description")) {
            this.description = obj.get("Description").getAsString()
        }
        if (obj.has("LastUpdateDate")) {
            this.lastUpdateDate = Helper.toDate(obj.get("LastUpdateDate").getAsString())
        }
    }

    RallyArtifact (long objectID, Date creationDate, String formattedID, String name) {
        super(objectID, creationDate)
        this.formattedID = formattedID
        this.name = name
    }

    @Override
    String toString () {
        return "RallyArtifact{" +
                "formattedID='" + formattedID + '\'' +
                ", name='" + name + '\'' +
                '}'
    }
}
