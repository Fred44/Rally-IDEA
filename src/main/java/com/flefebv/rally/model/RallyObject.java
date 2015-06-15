package com.flefebv.rally.model;

import com.google.gson.JsonObject;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Store PersistableObject properties.
 * https://rally1.rallydev.com/slm/doc/webservice/objectModel.sp#PersistableObject
 *
 * @author flefebv
 *         Date: 08/06/2015
 */
public abstract class RallyObject {

    protected long objectID;
    protected Date creationDate;
    protected String ref;

    public RallyObject() {}

    public RallyObject (JsonObject obj) {
        this.ref = obj.get("_ref").getAsString();
        this.objectID = obj.get("ObjectID").getAsLong();
        this.creationDate = Helper.toDate(obj.get("CreationDate").getAsString());
    }

    public RallyObject (long objectID, Date creationDate) {
        this.objectID = objectID;
        this.creationDate = creationDate;
    }

    @Transient
    public Date getCreationDate () {
        return creationDate;
    }

    @Transient
    public long getObjectID () {
        return objectID;
    }

    public String getRef () {
        return ref;
    }

    public void setRef (@NotNull String ref) {
        this.ref = ref;
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RallyObject that = (RallyObject) o;

        if (ref != null ? !ref.equals(that.ref) : that.ref != null) return false;

        return true;
    }

    @Override
    public int hashCode () {
        return ref != null ? ref.hashCode() : 0;
    }

    @Override
    public String toString () {
        return "RallyObject{objectID=" + objectID + ", creationDate=" + creationDate + "}";
    }
}
