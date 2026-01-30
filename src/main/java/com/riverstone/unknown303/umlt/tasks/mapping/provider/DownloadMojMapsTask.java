package com.riverstone.unknown303.umlt.tasks.mapping.provider;

import com.riverstone.unknown303.umlt.UMLTTask;
import com.riverstone.unknown303.umlt.util.MojangDownloader;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class DownloadMojMapsTask extends UMLTTask {
    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @TaskAction
    public void download() throws IOException {
        String version = MojangDownloader.getProperVersion(getCacheDir(), getMinecraftVersion().get());
        File outDir = getOutputDir().getAsFile().get();

        File clientText = new File(outDir, version + "-client.txt");
        File serverText = new File(outDir, version + "-server.txt");

        MojangDownloader.downloadClientMappings(getCacheDir(), version, clientText);
        MojangDownloader.downloadServerMappings(getCacheDir(), version, serverText);

        MemoryMappingTree mappings = new MemoryMappingTree();

        MappingReader.read(clientText.toPath(), MappingFormat.PROGUARD_FILE, mappings);
        MappingReader.read(serverText.toPath(), MappingFormat.PROGUARD_FILE, mappings);

        MemoryMappingTree properMaps = new MemoryMappingTree();

        properMaps.setSrcNamespace("official");
        properMaps.setDstNamespaces(List.of("mojmaps"));

        int officialNamespace = properMaps.getNamespaceId("official");
        int mojmapsNamespace = properMaps.getNamespaceId("mojmaps");

        for (MappingTree.ClassMapping clazz : mappings.getClasses()) {
            properMaps.addClass(clazz);
        }
    }
}
