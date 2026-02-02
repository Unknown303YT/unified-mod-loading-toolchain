package com.riverstone.unknown303.umlt.patcher.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.java.decompiler.api.Decompiler;
import org.jetbrains.java.decompiler.main.decompiler.DirectoryResultSaver;

import java.io.File;

public abstract class DecompileMinecraftTask extends PatcherTask {
    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<File> getClientJar();

    @Input
    public abstract Property<File> getServerJar();

    @OutputDirectory
    public abstract DirectoryProperty getClientOutputDir();

    @OutputDirectory
    public abstract DirectoryProperty getServerOutputDir();

    @TaskAction
    public void decompile() {
        decompileJar(getClientJar().get(), getClientOutputDir().getAsFile().get());
        decompileJar(getServerJar().get(), getServerOutputDir().getAsFile().get());
    }

    private void decompileJar(File inputJar, File outputDir) {
        Decompiler decompiler = Decompiler.builder()
                .option("ind", "    ")
                .option("lvt", "1")
                .option("asc", "1")
                .option("fdi", "1")
                .option("threads", "1")
                .option("optimize", "0")
                .option("decompile-generics", "1")
                .option("decompile-lambdas", "1")
                .option("decompile-inner", "1")
                .option("rename", "1")
                .option("remove-bridge", "0")
                .option("remove-synthetic", "0")
                .option("folder", "1")
                .inputs(inputJar)
                .output(new DirectoryResultSaver(outputDir))
                .build();
        decompiler.decompile();
    }
}
