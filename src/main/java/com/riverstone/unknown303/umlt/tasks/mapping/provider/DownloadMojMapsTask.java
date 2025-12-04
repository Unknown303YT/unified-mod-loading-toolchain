package com.riverstone.unknown303.umlt.tasks.mapping.provider;

import com.riverstone.unknown303.umlt.UMLTTask;
import com.riverstone.unknown303.umlt.util.MojangDownloader;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public abstract class DownloadMojMapsTask extends UMLTTask {
    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void download() throws IOException {
        String version = MojangDownloader.getProperVersion(getCacheDir(), getMinecraftVersion().get());
        File outDir = getOutputDir().getAsFile().get();

        MojangDownloader.downloadClientMappings(getCacheDir(), version,
                new File(outDir, version + "-client.txt"));
        MojangDownloader.downloadServerMappings(getCacheDir(), version,
                new File(outDir, version + "-server.txt"));
    }
}
