package com.riverstone.unknown303.umlt.patcher;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class PatcherPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        PatcherExtension extension = project.getExtensions()
                .create("mcPatcher", PatcherExtension.class);
    }
}
