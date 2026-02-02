package com.riverstone.unknown303.umlt.patcher.tasks;

import com.riverstone.unknown303.umlt.core.util.MojangDownloader;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public abstract class DownloadMinecraftTask extends PatcherTask {
    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void download() throws IOException {
        String version = getMinecraftVersion().get();
        File outDir = getOutputDir().getAsFile().get();
        MojangDownloader.downloadClientJar(getGlobalCacheDir(), version,
                new File(outDir, version + "-client.jar"));
        MojangDownloader.downloadServerJar(getGlobalCacheDir(), version,
                new File(outDir, version + "-server.jar"));
    }
}
