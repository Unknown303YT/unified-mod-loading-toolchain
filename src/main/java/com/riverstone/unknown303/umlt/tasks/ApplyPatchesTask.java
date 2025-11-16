package com.riverstone.unknown303.umlt.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

public abstract class ApplyPatchesTask extends DefaultTask {
    private final ExecOperations execOperations;

    @Inject
    public ApplyPatchesTask(ExecOperations execOperations) {
        this.execOperations = execOperations;
    }

    @TaskAction
    public void run() {
        File patchDir = new File(getProject().getProjectDir(), "patches");
        File sourceDir = new File(
                getProject().getLayout().getBuildDirectory().get().getAsFile(),
                "vanilla-remapped-source");

        if (!patchDir.exists() || !patchDir.isDirectory()) {
            getLogger().lifecycle("No patches directory found, skipping patch application.");
            return;
        }

        File[] patches = patchDir.listFiles((dir, name) -> name.endsWith(".patch") || name.endsWith(".diff"));
        if (patches == null || patches.length == 0) {
            getLogger().lifecycle("No patch files found in patches/, skipping.");
            return;
        }

        // Sort patches to ensure consistent order
        Arrays.sort(patches);

        for (File patch : patches) {
            getLogger().lifecycle("Applying patch: " + patch.getName());
            try {
                execOperations.exec(exec -> {
                    exec.setWorkingDir(sourceDir);
                    exec.commandLine("git", "apply", patch.getAbsolutePath());
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to apply patch: " + patch.getName(), e);
            }
        }

        getLogger().lifecycle("All patches applied successfully.");
    }
}