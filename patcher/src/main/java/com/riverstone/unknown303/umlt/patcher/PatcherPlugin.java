package com.riverstone.unknown303.umlt.patcher;

import com.riverstone.unknown303.umlt.core.util.Util;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

public class PatcherPlugin implements Plugin<Project> {
    private File cacheDir;

    @Override
    public void apply(Project project) {
        PatcherExtension extension =
                project.getExtensions().create("patcher", PatcherExtension.class, project);

        cacheDir = extension.useGlobalCache().get() ?
                Util.createFolder(project.getGradle().getGradleUserHomeDir(),
                        "caches" + File.separator + "umlt") :
                Util.createFolder(project.getLayout().getBuildDirectory().getAsFile().get(), "umlt");
    }
}
