package com.riverstone.unknown303.umlt.tasks;

import com.riverstone.unknown303.umlt.HTTPUtils;
import com.riverstone.unknown303.umlt.UMLTTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.com.google.gson.*;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public abstract class DownloadMappingsTask extends UMLTTask {
    @Input
    public abstract Property<String> getMappingType();

    @Input
    public abstract Property<String> getMappingVersion();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputFile
    public abstract RegularFileProperty getOutputMappings();

    @OutputFile
    public abstract RegularFileProperty getJavadocOutput();

    @TaskAction
    public void run() throws IOException {
        String type = getMappingType().get().toLowerCase();
        String version = getMappingVersion().get();

        File mappingsDir = new File(getCacheDir(),
                "mappings" + File.separator + type + File.separator + version);
        mappingsDir.mkdirs();

        File tinyFile = new File(mappingsDir, "mappings.tiny");
        getOutputMappings().set(tinyFile);

        File javadocFile = new File(mappingsDir, "javadocs.json");
        getJavadocOutput().set(javadocFile);

        if (tinyFile.exists()) {
            getLogger().lifecycle("Using cached mappings: {}", tinyFile);
            return;
        }

        Map<String, String> javadocs = new HashMap<>();
        List<String[]> tinyLines = null;

        tinyLines = switch (type) {
            case "official", "mojang", "mojmaps" -> official(mappingsDir, tinyFile);
            case "yarn" -> yarn(version, mappingsDir, tinyFile, javadocs);
            case "parchment", "parchmentmc" -> parchment(version, mappingsDir, tinyFile, javadocs);
            case "quilt" -> quilt(version, mappingsDir, tinyFile, javadocs);
            default -> throw new IllegalArgumentException("Mappings must be official, yarn, parchment, " +
                    "or quilt, got " + type);
        };

        JsonObject root = new JsonObject();
        for (Map.Entry<String, String> entry : javadocs.entrySet())
            root.addProperty(entry.getKey(), entry.getValue());
        FileWriter writer = new FileWriter(new File(mappingsDir, "mappings.json "));
        writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(root));
        writer.close();
    }

    private List<String[]> official(File mappingsDir, File tinyFile) throws IOException {
        File clientTxt = new File(mappingsDir, "client.txt");
        File serverTxt = new File(mappingsDir, "server.txt");

        JsonObject versionData = DownloadMinecraftTask.findVersionInfo(this,
                DownloadMinecraftTask.fetchVersionManifest(this), getMinecraftVersion().get());

        String clientUrl = versionData.getAsJsonObject("downloads")
                .getAsJsonObject("client_mappings").get("url").getAsString();
        String serverUrl = versionData.getAsJsonObject("downloads")
                .getAsJsonObject("server_mappings").get("url").getAsString();

        getLogger().lifecycle("Downloading official client mappings...");
        HTTPUtils.downloadFile(clientUrl, clientTxt);

        getLogger().lifecycle("Downloading official server mappings...");
        HTTPUtils.downloadFile(serverUrl, serverTxt);

        getLogger().lifecycle("Converting official mappings to Tiny...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tinyFile));
        writer.write("v1\tofficial\tnamed\n");

        // Merge CL, FD, MD from both files
        for (File txt : new File[]{clientTxt, serverTxt}) {
            BufferedReader reader = new BufferedReader(new FileReader(txt));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("CL:")) {
                    String[] parts = line.split(" ");
                    writer.write("c\t" + parts[1] + "\t" + parts[2] + "\n");
                } else if (line.startsWith("FD:")) {
                    String[] parts = line.split(" ");
                    writer.write("f\t" + parts[1] + " " + parts[2] + "\t" + parts[2] + "\n");
                } else if (line.startsWith("MD:")) {
                    String[] parts = line.split(" ");
                    writer.write("m\t" + parts[1] + " " + parts[2] + "\t" + parts[2] + "\n");
                }
            }
            reader.close();
        }
        writer.close();
    }

    private List<String[]> yarn(String version, File mappingsDir, File tinyFile, Map<String, String> javadocs) throws IOException {
        File tmpGz = new File(mappingsDir, tinyFile.getName() + ".gz");
        HTTPUtils.downloadFile("https://maven.fabricmc.net/net/fabricmc/yarn/%s/yarn-%s-tiny.gz"
                .formatted(version, version), tmpGz);

        getLogger().lifecycle("Decompressing {} → {}", tmpGz, tinyFile);
        GZIPInputStream gzipInput = new GZIPInputStream(new FileInputStream(tmpGz));
        FileOutputStream fileOutput = new FileOutputStream(tinyFile);

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = gzipInput.read(buffer)) != -1)
            fileOutput.write(buffer, 0, bytesRead);

        gzipInput.close();
        fileOutput.close();
        tmpGz.delete();
    }

    private List<String[]> parchment(String version, File mappingsDir, File tinyFile, Map<String, String> javadocs) throws IOException {
        File zipFile = new File(mappingsDir, "parchment.zip");
        String url = "https://maven.parchmentmc.org/org/parchmentmc/data/parchment-%s/%s/parchment-%s-%s.zip"
                .formatted(getMinecraftVersion().get(), version, getMinecraftVersion().get(), version);

        getLogger().lifecycle("Downloading Parchment mappings...");
        HTTPUtils.downloadFile(url, zipFile);

        ZipFile zip = new ZipFile(zipFile);
        ZipEntry mappingsEntry = zip.stream()
                .filter(e -> !e.isDirectory() && e.getName().endsWith(".json"))
                .findFirst()
                .orElseThrow(() -> new IOException("No JSON mapping found in Parchment zip"));

        File jsonMaps = new File(mappingsDir, "parchment.json");
        InputStream in = zip.getInputStream(mappingsEntry);
        Files.copy(in, jsonMaps.toPath());

        List<String[]> tinyOfficial = official(mappingsDir, tinyFile);
        FileReader reader = new FileReader(jsonMaps);
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();

        if (!json.has("classes"))
            return tinyOfficial;

        JsonArray classes = json.getAsJsonArray("classes");

        for (JsonElement element : classes) {
            JsonObject clazz = element.getAsJsonObject();
            String className = clazz.get("name").getAsString();

            if (clazz.has("javadoc")) {
                javadocs.put(className, clazz.get("javadoc").getAsString());
            }

            if (clazz.has("methods")) {
                for (JsonElement method : clazz.getAsJsonArray("methods")) {
                    JsonObject methodObject = method.getAsJsonObject();
                    if (methodObject.has("javadoc")) {
                        String methodName = methodObject.get("name").getAsString();
                        javadocs.put(className + "." + methodName, methodObject.get("javadoc").getAsString());
                    }
                }
            }

            if (clazz.has("fields")) {
                for (JsonElement field : clazz.getAsJsonArray("fields")) {
                    JsonObject fieldObject = field.getAsJsonObject();
                    if (fieldObject.has("javadoc")) {
                        String fieldName = fieldObject.get("name").getAsString();
                        javadocs.put(className + "." + fieldName, fieldObject.get("javadoc").getAsString());
                    }
                }
            }
        }

        return tinyOfficial;
    }

    private List<String[]> quilt(String version, File mappingsDir, File tinyFile, Map<String, String> javadocs) throws IOException {
        File tmpGz = new File(mappingsDir, tinyFile.getName() + ".gz");
        HTTPUtils.downloadFile("https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/%s/quilt-mappings-%s-tiny.gz"
                .formatted(version, version), tmpGz);

        getLogger().lifecycle("Decompressing {} → {}", tmpGz, tinyFile);
        GZIPInputStream gzipInput = new GZIPInputStream(new FileInputStream(tmpGz));
        FileOutputStream fileOutput = new FileOutputStream(tinyFile);

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = gzipInput.read(buffer)) != -1)
            fileOutput.write(buffer, 0, bytesRead);

        gzipInput.close();
        fileOutput.close();
        tmpGz.delete();
    }
}
