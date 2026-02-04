package com.riverstone.unknown303.umlt.patcher;

import com.riverstone.unknown303.umlt.core.util.Util;
import com.riverstone.unknown303.umlt.patcher.tasks.DecompileMinecraftTask;
import com.riverstone.unknown303.umlt.patcher.tasks.DownloadMinecraftTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

import java.io.File;

public class PatcherPlugin implements Plugin<Project> {
    private File globalCacheDir;
    private File localCacheDir;

    @Override
    public void apply(Project project) {
        PatcherExtension extension =
                project.getExtensions().create("patcher", PatcherExtension.class, project.getObjects());

        globalCacheDir = Util.createFolder(project.getGradle().getGradleUserHomeDir(),
                "caches" + File.separator + "umlt" + File.separator + "patcher");
        localCacheDir = Util.createFolder(project.getLayout().getBuildDirectory().getAsFile().get(),
                "umlt" + File.separator + "patcher");

        TaskProvider<DownloadMinecraftTask> downloadMC = project.getTasks().register(
                "downloadMinecraft", DownloadMinecraftTask.class,
                task -> {
                    task.setGroup("patcher");
                    task.getMinecraftVersion().convention(extension.getMinecraftVersion());
                    task.getOutputDir().set(new File(globalCacheDir, "mcJars"));
                });

        TaskProvider<DecompileMinecraftTask> decompileMC = project.getTasks().register(
                "decompileMinecraft", DecompileMinecraftTask.class,
                task -> {
                    task.setGroup("patcher");
                    task.getMinecraftVersion().convention(extension.getMinecraftVersion());
                    task.getClientJar().set(getClientJar(extension.getMinecraftVersion().get()));
                    task.getServerJar().set(getServerJar(extension.getMinecraftVersion().get()));
                    task.getClientOutputDir().set(new File(new File(localCacheDir, "decompiled-vanilla"), extension.getMinecraftVersion().get() + "-client"));
                    task.getServerOutputDir().set(new File(new File(localCacheDir, "decompiled-vanilla"), extension.getMinecraftVersion().get() + "-server"));
                    task.getMemoryAllocated().convention(4096);
                });
    }

    public File getGlobalCacheDir() {
        return this.globalCacheDir;
    }

    public File getLocalCacheDir() {
        return localCacheDir;
    }

    public static PatcherPlugin getPlugin(Project project) {
        return project.getPlugins().getPlugin(PatcherPlugin.class);
    }

    private File getClientJar(String version) {
        return new File(new File(globalCacheDir, "mcJars/" + version), "client-" + version.replace('.', '_') + ".jar");
    }

    private File getServerJar(String version) {
        return new File(new File(globalCacheDir, "mcJars/" + version), "server-" + version.replace('.', '_') + ".jar");
    }
}