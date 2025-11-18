package com.riverstone.unknown303.umlt.tasks;

import com.riverstone.unknown303.umlt.UMLTTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@CacheableTask
public abstract class DecompileTask extends UMLTTask {
    @InputFile
    public abstract RegularFileProperty getPatchedJar();

    @InputFile
    public abstract RegularFileProperty getTinyMappings();

    @OutputDirectory
    public abstract DirectoryProperty getOutputSrc();

    @TaskAction
    public void run() {
        File jar = getPatchedJar().getAsFile().get();
        File outDir = getOutputSrc().getAsFile().get();
        outDir.mkdirs();

        getLogger().lifecycle("Decompiling {} → {}", jar, outDir);
        Map<String, Object> options = new HashMap<>();
        options.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "true");
        options.put(IFernflowerPreferences.INDENT_STRING, "    ");

        Map<String, String> javadocMap = JavadocMapper.parseTinyComments(getTinyMappings().get().getAsFile());
    }
}
