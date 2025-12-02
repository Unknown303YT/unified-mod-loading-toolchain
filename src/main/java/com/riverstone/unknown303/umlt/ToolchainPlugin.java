package com.riverstone.unknown303.umlt;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

public class ToolchainPlugin implements Plugin<Project> {
    private File cacheDir;

    @Override
    public void apply(Project project) {
        UMLTExtension extension =
                project.getExtensions().create("umlt", UMLTExtension.class, project);
        cacheDir = project.getLayout().getBuildDirectory().dir("umlt").get().getAsFile();
        cacheDir.mkdirs();
    }

    public File getCacheDir() {
        return this.cacheDir;
    }

    public static ToolchainPlugin getPlugin(Project project) {
        return project.getPlugins().getPlugin(ToolchainPlugin.class);
    }
}
