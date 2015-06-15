package com.flefebv.rally.jetbrains.tasks;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.config.TaskRepositoryEditor;
import com.intellij.tasks.impl.BaseRepositoryType;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author flefebv
 *         Date: 05/06/2015
 */
class RallyRepositoryType extends BaseRepositoryType<RallyRepository> {

    static final Icon RallyIcon = IconLoader.getIcon("/icons/rally.png", RallyRepository)

    @NotNull
    @Override
    String getName () {
        return "Rally"
    }

    @NotNull
    @Override
    Icon getIcon () {
        return RallyIcon
    }

    @NotNull
    @Override
    TaskRepository createRepository () {
        return new RallyRepository(this)
    }

    @Override
    Class<RallyRepository> getRepositoryClass () {
        return RallyRepository
    }

    @NotNull
    @Override
    TaskRepositoryEditor createEditor(RallyRepository repository, Project project, Consumer<RallyRepository> changeListener) {
        return new RallyRepositoryEditor(project, repository, changeListener)
    }
}
