package com.riverstone.unknown303.umlt.tasks.mapping.provider;

import com.riverstone.unknown303.umlt.UMLTTask;
import com.riverstone.unknown303.umlt.util.Constants;
import com.riverstone.unknown303.umlt.util.HTTPUtils;
import com.riverstone.unknown303.umlt.util.MojangDownloader;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public abstract class GenerateTemplateMappingFileTask extends UMLTTask {
    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void generate() throws IOException {
        String version = MojangDownloader.getProperVersion(getCacheDir(), getMinecraftVersion().get());
        File outDir = getOutputDir().getAsFile().get();

        File intermediaryJar = new File(new File(outDir, "jars"),
                "intermediary-%s-v2.jar".formatted(version));
        HTTPUtils.downloadFile(Constants.INTERMEDIARY_LOCATION +
                "%s/intermediary-%s-v2.jar".formatted(version, version), intermediaryJar, true);

        MemoryMappingTree mappings = new MemoryMappingTree();

        JarInputStream jarInput = new JarInputStream(new FileInputStream(intermediaryJar));
        JarEntry jarEntry;
        while ((jarEntry = jarInput.getNextJarEntry()) != null) {
            if (jarEntry.getName().equals("mappings/mappings.tiny")) {
                MappingReader.read(new InputStreamReader(jarInput), mappings);
            }
        }
        jarInput.close();

        File output = new File(outDir, version + ".tiny");
        MappingWriter writer = MappingWriter.create(output.toPath(), MappingFormat.TINY_2_FILE);
        mappings.accept(writer);
    }
}
