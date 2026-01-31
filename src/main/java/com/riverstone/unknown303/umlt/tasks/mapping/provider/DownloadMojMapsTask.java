package com.riverstone.unknown303.umlt.tasks.mapping.provider;

import com.riverstone.unknown303.umlt.UMLTTask;
import com.riverstone.unknown303.umlt.util.MojangDownloader;
<<<<<<< HEAD
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.proguard.ProGuardFileReader;
=======
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
>>>>>>> 9e31babfb8b56b77eedc55438878e93c76cf3623
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public abstract class DownloadMojMapsTask extends UMLTTask {
    private File clientMappingFile;
    private File serverMappingFile;

    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    public File getClientMappingFile() {
        return clientMappingFile;
    }

    public File getServerMappingFile() {
        return serverMappingFile;
    }

    @TaskAction
    public void download() throws IOException {
        String version = MojangDownloader.getProperVersion(getCacheDir(), getMinecraftVersion().get());
        File outDir = getOutputDir().getAsFile().get();

<<<<<<< HEAD
        File clientTxt = new File(outDir, version + "-client.txt");
        File serverTxt = new File(outDir, version + "-server.txt");

        MojangDownloader.downloadClientMappings(getCacheDir(), version, clientTxt);
        MojangDownloader.downloadServerMappings(getCacheDir(), version, serverTxt);

        MemoryMappingTree clientMappings = new MemoryMappingTree();
        MemoryMappingTree serverMappings = new MemoryMappingTree();
        clientMappings.visitNamespaces("mojmaps", List.of("official"));
        serverMappings.visitNamespaces("mojmaps", List.of("official"));

        ProGuardFileReader.read(new FileReader(clientTxt),
                "mojmaps", "official", clientMappings);
        ProGuardFileReader.read(new FileReader(serverTxt),
                "mojmaps", "official", serverMappings);

        clientMappings.visitNamespaces("official", List.of("mojmaps"));

        clientMappingFile = new File(outDir, version + "-client.tiny");
        clientMappings.accept(MappingWriter.create(clientMappingFile.toPath(), MappingFormat.TINY_2_FILE));

        serverMappingFile = new File(outDir, version + "-server.tiny");
        serverMappings.accept(MappingWriter.create(serverMappingFile.toPath(), MappingFormat.TINY_2_FILE));
=======
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
>>>>>>> 9e31babfb8b56b77eedc55438878e93c76cf3623
    }
}
