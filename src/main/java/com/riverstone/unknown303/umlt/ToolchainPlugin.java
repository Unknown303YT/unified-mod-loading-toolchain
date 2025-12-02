package com.riverstone.unknown303.umlt;

import com.riverstone.unknown303.umlt.tasks.download.DownloadMinecraftTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class ToolchainPlugin implements Plugin<Project> {
    private File cacheDir;

    @Override
    public void apply(Project project) {
        cacheDir = project.getLayout().getBuildDirectory().dir("umlt").get().getAsFile();
        cacheDir.mkdirs();

        UMLTExtension extension =
                project.getExtensions().create("umlt", UMLTExtension.class, project);

        TaskProvider<DownloadMinecraftTask> downloadMC = project.getTasks().register(
                "downloadMinecraft", DownloadMinecraftTask.class, task -> {
                    task.getMinecraftVersion().set(extension.getMinecraftVersion());
                    task.getOutputDir().set(new File(cacheDir, "vanillaJars"));
                });
    }

    public File getCacheDir() {
        return this.cacheDir;
    }

    public static ToolchainPlugin getPlugin(Project project) {
        return project.getPlugins().getPlugin(ToolchainPlugin.class);
    }
}
