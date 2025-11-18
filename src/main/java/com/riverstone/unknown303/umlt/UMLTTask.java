package com.riverstone.unknown303.umlt;

import org.gradle.api.DefaultTask;

import java.io.File;

public abstract class UMLTTask extends DefaultTask {
    public File getCacheDir() {
        return ToolchainPlugin.getPlugin(getProject()).getCacheDir();
    }
}
