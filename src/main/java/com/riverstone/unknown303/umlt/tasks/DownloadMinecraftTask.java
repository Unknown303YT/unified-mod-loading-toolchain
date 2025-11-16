package com.riverstone.unknown303.umlt.tasks;

import com.riverstone.unknown303.umlt.HTTPUtils;
import com.riverstone.unknown303.umlt.ToolchainPlugin;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.impldep.com.google.gson.JsonElement;
import org.gradle.internal.impldep.com.google.gson.JsonObject;
import org.gradle.internal.impldep.com.google.gson.JsonParser;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public abstract class DownloadMinecraftTask extends DefaultTask {
    private final ExecOperations execOperations;

    @Inject
    public DownloadMinecraftTask(ExecOperations execOperations) {
        this.execOperations = execOperations;
    }

    @Input
    public abstract Property<String> getMinecraftVersion();

    @OutputFile
    public abstract RegularFileProperty getClientJar();

    @OutputFile
    public abstract RegularFileProperty getServerJar();

    @TaskAction
    public void run() throws IOException {
        String mcVersion = getMinecraftVersion().get();

        getLogger().lifecycle("Downloading Minecraft {}...", mcVersion);

        getLogger().debug("Creating folders...");

        File versionCache = new File(ToolchainPlugin.getPlugin(getProject()).getCacheDir(),
                getMinecraftVersion().get());
        versionCache.mkdirs();

        File clientJar = new File(versionCache, "client.jar");
        File serverJar = new File(versionCache, "server.jar");

        getClientJar().set(clientJar);
        getServerJar().set(serverJar);

        getLogger().info("Fetching Minecraft version data...");
        JsonObject manifest = fetchVersionManifest();
        JsonObject versionInfo = findVersionInfo(manifest, mcVersion);

        String clientUrl = versionInfo.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString();
        String serverUrl = versionInfo.getAsJsonObject("downloads").getAsJsonObject("server").get("url").getAsString();

        getLogger().lifecycle("Downloading client jar...");
        HTTPUtils.downloadFile(clientUrl, clientJar);

        getLogger().lifecycle("Downloading server jar...");
        HTTPUtils.downloadFile(serverUrl, serverJar);
    }

    private JsonObject fetchVersionManifest() throws IOException {
        File manifestCache = new File(ToolchainPlugin.getPlugin(getProject()).getCacheDir(),
                "version_manifest.json");
        HTTPUtils.downloadFile("https://launchermeta.mojang.com/mc/game/version_manifest.json", manifestCache);
        Reader reader = new FileReader(manifestCache);
        JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();
        return obj;
    }

    private JsonObject findVersionInfo(JsonObject manifest, String mcVersion) throws IOException {
        if (mcVersion.equalsIgnoreCase("latest")
                || mcVersion.equalsIgnoreCase("latest-release"))
            mcVersion = manifest.getAsJsonObject("latest")
                    .get("release").getAsString();
        else if (mcVersion.equalsIgnoreCase("latest-snapshot"))
            mcVersion = manifest.getAsJsonObject("latest")
                    .get("snapshot").getAsString();

        for (JsonElement jsonElement : manifest.getAsJsonArray("versions")) {
            JsonObject versionObject = jsonElement.getAsJsonObject();
            if (versionObject.get("id").getAsString().equalsIgnoreCase(mcVersion)) {
                String versionUrl = versionObject.get("url").getAsString();
                File versionCache = new File(ToolchainPlugin.getPlugin(getProject()).getCacheDir(),
                        mcVersion + ".json");
                HTTPUtils.downloadFile(versionUrl, versionCache);

                Reader reader = new FileReader(versionCache);
                JsonObject versionInfo = JsonParser.parseReader(reader).getAsJsonObject();
                reader.close();
                return versionInfo;
            }
        }

        throw new IOException("Minecraft version not found: " + mcVersion);
    }
}
