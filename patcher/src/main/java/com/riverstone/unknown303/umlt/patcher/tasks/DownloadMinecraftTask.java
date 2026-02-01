package com.riverstone.unknown303.umlt.patcher.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public abstract class DownloadMinecraftTask extends DefaultTask {
    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void download() {

    }
}
