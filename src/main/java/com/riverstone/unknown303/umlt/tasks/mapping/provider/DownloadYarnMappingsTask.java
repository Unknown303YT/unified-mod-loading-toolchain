package com.riverstone.unknown303.umlt.tasks.mapping.provider;

import com.riverstone.unknown303.umlt.UMLTTask;
import com.riverstone.unknown303.umlt.core.util.MojangDownloader;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public abstract class DownloadYarnMappingsTask extends UMLTTask {
    private File outputFile;

    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    public File getOutputFile() {
        return outputFile;
    }

    @TaskAction
    public void download() throws IOException {
        String version = MojangDownloader.getProperVersion(getCacheDir(), getMinecraftVersion().get());
        File outDir = getOutputDir().getAsFile().get();

        File mappingsJar = new File(outDir, version + ".jar");

        // TODO: FIND URL FOR TINY

        outputFile = new File(outDir, version + ".tiny");
    }
}
