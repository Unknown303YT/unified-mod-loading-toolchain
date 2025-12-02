package com.riverstone.unknown303.umlt.tasks;

import com.riverstone.unknown303.umlt.UMLTTask;
import com.riverstone.unknown303.umlt.util.MojangDownloader;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public abstract class DownloadMinecraftTask extends UMLTTask {
    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void download() throws IOException {
        String version = getMinecraftVersion().get();
        File outDir = getOutputDir().getAsFile().get();
        MojangDownloader.downloadClientJar(getCacheDir(), version,
                new File(
                        new File(getCacheDir(), "mcJars"),
                        version + "-client.jar"));
        MojangDownloader.downloadServerJar(getCacheDir(), version,
                new File(
                        new File(getCacheDir(), "mcJars"),
                        version + "-server.jar"));
    }
}
