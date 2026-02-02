package com.riverstone.unknown303.umlt.patcher.tasks;

import com.riverstone.unknown303.umlt.patcher.PatcherPlugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;

import java.io.File;

public class PatcherTask extends DefaultTask {
    @Internal
    public File getGlobalCacheDir() {
        return PatcherPlugin.getPlugin(getProject()).getGlobalCacheDir();
    }

    @Internal
    public File getLocalCacheDir() {
        return PatcherPlugin.getPlugin(getProject()).getGlobalCacheDir();
    }
}
