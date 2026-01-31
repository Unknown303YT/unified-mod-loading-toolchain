package com.riverstone.unknown303.umlt;

import com.riverstone.unknown303.umlt.tasks.mapping.provider.DownloadMojMapsTask;
import com.riverstone.unknown303.umlt.tasks.mapping.provider.DownloadIntermediaryMappingsTask;
import com.riverstone.unknown303.umlt.tasks.download.DownloadMinecraftTask;
import com.riverstone.unknown303.umlt.util.Util;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class ToolchainPlugin implements Plugin<Project> {
    private File cacheDir;

    @Override
    public void apply(Project project) {
        UMLTExtension extension =
                project.getExtensions().create("umlt", UMLTExtension.class, project);

        cacheDir = extension.useGlobalCache().get() ?
                Util.createFolder(project.getGradle().getGradleUserHomeDir(),
                        "caches" + File.separator + "umlt") :
                Util.createFolder(project.getLayout().getBuildDirectory().getAsFile().get(), "umlt");

        TaskProvider<DownloadMinecraftTask> downloadMC = project.getTasks().register(
                "downloadMinecraft", DownloadMinecraftTask.class, task -> {
                    task.getMinecraftVersion().set(extension.getMinecraftVersion());
                    task.getOutputDir().set(new File(cacheDir, "vanillaJars"));
                });

        TaskProvider<DownloadIntermediaryMappingsTask> downloadIntermediaryMappings = project.getTasks().register(
                "downloadIntermediaryMappings", DownloadIntermediaryMappingsTask.class,
                task -> {
                    task.getMinecraftVersion().set(extension.getMinecraftVersion());
                    task.getOutputDir().set(new File(cacheDir, "mappings/template"));
                });

        TaskProvider<DownloadMojMapsTask> downloadMojMaps = project.getTasks().register(
                "downloadMojMaps", DownloadMojMapsTask.class,
                task -> {
                    task.getMinecraftVersion().set(extension.getMinecraftVersion());
                    task.getOutputDir().set(new File(cacheDir, "mappings" + File.separator +  "mojmaps"));
                });
    }

    public File getCacheDir() {
        return this.cacheDir;
    }

    public static ToolchainPlugin getPlugin(Project project) {
        return project.getPlugins().getPlugin(ToolchainPlugin.class);
    }
}
