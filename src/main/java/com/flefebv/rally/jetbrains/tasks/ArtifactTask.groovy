package com.flefebv.rally.jetbrains.tasks;

import com.flefebv.rally.model.RallyArtifact
import com.flefebv.rally.model.RallyTask;
import com.intellij.tasks.Comment;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskType
import com.intellij.tasks.impl.LocalTaskImpl;
import icons.TasksIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 *
 * @author flefebv
 *         Date: 08/06/2015
 */
class ArtifactTask extends Task {

    private RallyArtifact myArtifact;
    private RallyRepository myRepository;

    public ArtifactTask (RallyArtifact artifact, RallyRepository repository) {
        this.myArtifact = artifact;
        this.myRepository = repository;
    }

    @NotNull
    @Override
    public String getId () {
        return myArtifact.getObjectID();
    }

    @NotNull
    @Override
    public String getSummary () {
        return myArtifact.getFormattedID() + " : " + myArtifact.getName();
    }

    @Nullable
    @Override
    public String getDescription () {
        return myArtifact.getDescription();
    }

    @NotNull
    @Override
    public Comment[] getComments () {
        return Comment.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public Icon getIcon () {
        return LocalTaskImpl.getIconFromType(getType(), isIssue());
    }

    @NotNull
    @Override
    public TaskType getType () {
        return TaskType.OTHER;
    }

    @Nullable
    @Override
    public Date getUpdated () {
        return myArtifact.getLastUpdateDate();
    }

    @Nullable
    @Override
    public Date getCreated () {
        return myArtifact.getCreationDate();
    }

    @Override
    public boolean isClosed () {
        if (myArtifact instanceof RallyTask) {
            return myArtifact.state == RallyTask.State.Completed
        }
        return false;
    }

    @Override
    public boolean isIssue () {
        return false;
    }

    @Nullable
    @Override
    public String getIssueUrl () {
        return myRepository.getUrl() + "/#/detail/task/" + myArtifact.getObjectID();
    }

    @Override
    String getPresentableName() {
        return getSummary();
    }
}
