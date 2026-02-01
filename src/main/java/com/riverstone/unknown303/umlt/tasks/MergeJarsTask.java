package com.riverstone.unknown303.umlt.tasks;

import com.riverstone.unknown303.umlt.UMLTTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;

public abstract class MergeJarsTask extends UMLTTask {
    @InputFile
    public abstract RegularFileProperty getClientJar();

    @InputFile
    public abstract RegularFileProperty getServerJar();

    @OutputFile
    public abstract RegularFileProperty getOutputJar();

    @TaskAction
    public void merge() throws IOException {
        File client = getClientJar().getAsFile().get();
        File server = getServerJar().getAsFile().get();
        File output = getOutputJar().getAsFile().get();
        output.getParentFile().mkdirs();
        JarMerger.merge(client, server, output);
    }
}
