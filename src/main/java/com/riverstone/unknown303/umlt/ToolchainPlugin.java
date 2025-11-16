package com.riverstone.unknown303.umlt;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ToolchainPlugin implements Plugin<Project> {
    private File cacheDir;

    @Override
    public void apply(@NotNull Project project) {
        cacheDir = new File(project.getGradle().getGradleUserHomeDir(),
                "caches" + File.separator + "umlt");
        UMLTExtension extension =
                project.getExtensions().create("umlt", UMLTExtension.class);
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public static ToolchainPlugin getPlugin(Project project) {
        return project.getPlugins().withType(ToolchainPlugin.class)
                .stream().findFirst().orElse(null);
    }
}
