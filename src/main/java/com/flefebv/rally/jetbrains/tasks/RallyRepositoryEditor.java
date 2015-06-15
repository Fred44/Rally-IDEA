package com.flefebv.rally.jetbrains.tasks;

import com.flefebv.rally.model.RallyProject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.tasks.impl.TaskUiUtil;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.Consumer;
import com.intellij.util.ui.FormBuilder;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;

/**
 * Created by fred on 07/06/15.
 */
public class RallyRepositoryEditor extends BaseRepositoryEditor<RallyRepository> {

    private static final Logger LOG = Logger.getInstance(RallyRepository.class);

    private JBLabel myProjectLabel;
    private ComboBox myProjectCombobox;

    public RallyRepositoryEditor (Project project,
                                    RallyRepository repository,
                                    Consumer<RallyRepository> changeListener) {
        super(project, repository, changeListener);

        myTestButton.setText("Login");
        myTestButton.setEnabled(myRepository.isConfigured());

        installListener(myProjectCombobox);

        if (myRepository.getRallyProject() != null) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
                @Override
                public void run () {
                    new FetchRallyProjects().queue();
                }
            });
        }
    }

    @Override
    protected void afterTestConnection(boolean connectionSuccessful) {
        super.afterTestConnection(connectionSuccessful);
        if (connectionSuccessful) {
            new FetchRallyProjects().queue();
        } else {
            myProjectCombobox.removeAllItems();
        }
    }

    @Nullable
    @Override
    protected JComponent createCustomPanel() {
        myProjectLabel = new JBLabel("Project:", SwingConstants.RIGHT);
        myProjectCombobox =  new ComboBox(200);
        myProjectCombobox.setRenderer(new TaskUiUtil.SimpleComboBoxRenderer<RallyProject>("Login first") {
            @NotNull
            @Override
            protected String getDescription (@NotNull RallyProject item) {
                return item.getName();
            }
        });
        return FormBuilder.createFormBuilder()
                .addLabeledComponent(myProjectLabel, myProjectCombobox)
                .getPanel();
    }

    @Override
    public void apply() {
        myRepository.setRallyProject((RallyProject) myProjectCombobox.getSelectedItem());
        myTestButton.setEnabled(myRepository.isConfigured());
        super.apply();
    }

    @Override
    public void setAnchor(@Nullable JComponent anchor) {
        super.setAnchor(anchor);
        myProjectLabel.setAnchor(anchor);
    }

    private class FetchRallyProjects extends TaskUiUtil.ComboBoxUpdater<RallyProject> {

        public FetchRallyProjects () {
            super(RallyRepositoryEditor.this.myProject, "Downloading Rally Projects...", myProjectCombobox);
        }

        @NotNull
        @Override
        protected Collection<RallyProject> fetch (ProgressIndicator indicator) throws Exception {
            myRepository.refreshProjects();
            return myRepository.getProjects();
        }

        @Nullable
        @Override
        public RallyProject getSelectedItem () {
            return myRepository.getRallyProject();
        }
    }
}
