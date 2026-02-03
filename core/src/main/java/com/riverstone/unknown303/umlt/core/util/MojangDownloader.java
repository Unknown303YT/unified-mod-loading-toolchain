package com.riverstone.unknown303.umlt.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.IOException;

public final class MojangDownloader {
    private MojangDownloader() {}

    public static void downloadClientJar(File buildDir, String version, File out, Logger logger) throws IOException {
        String properVersion = getProperVersion(buildDir, version, logger);
        File versionDataFile = new File(Util.createFolder(buildDir, "version_data"),
                properVersion + ".json");
        JsonObject versionData = HTTPUtils.downloadJson(getVersionDataUrl(buildDir, version, logger),
                versionDataFile, logger).getAsJsonObject();
        String clientURL = versionData.getAsJsonObject("downloads")
                .getAsJsonObject("client").getAsJsonPrimitive("url").getAsString();
        HTTPUtils.downloadFile(clientURL, out, logger);
    }

    public static void downloadServerJar(File buildDir, String version, File out, Logger logger) throws IOException {
        String properVersion = getProperVersion(buildDir, version, logger);
        File versionDataFile = new File(Util.createFolder(buildDir, "version_data"),
                properVersion + ".json");
        JsonObject versionData = HTTPUtils.downloadJson(getVersionDataUrl(buildDir, version, logger),
                versionDataFile, logger).getAsJsonObject();
        String serverURL = versionData.getAsJsonObject("downloads")
                .getAsJsonObject("server").getAsJsonPrimitive("url").getAsString();
        HTTPUtils.downloadFile(serverURL, out, logger);
    }

    public static String getVersionDataUrl(File buildDir, String version, Logger logger) throws IOException {
        String properVersion = getProperVersion(buildDir, version, logger);

        File manifestFile = new File(Util.createFolder(buildDir, "version_data"),
                "manifest.json");
        JsonObject json =
                HTTPUtils.downloadJson(Constants.MC_VERSION_MANIFEST, manifestFile, logger).getAsJsonObject();
        for (JsonElement data : json.getAsJsonArray("versions")) {
            JsonObject versionData = data.getAsJsonObject();
            return versionData.getAsJsonPrimitive("url").getAsString();
        }

        throw new IllegalArgumentException("Version " + properVersion + " not found in Mojang data!");
    }

    public static String getProperVersion(File buildDir, String version, Logger logger) throws IOException {
        File manifestFile = new File(Util.createFolder(buildDir, "version_data"),
                "manifest.json");
        JsonObject manifest = HTTPUtils.downloadJson(Constants.MC_VERSION_MANIFEST, manifestFile, logger)
                .getAsJsonObject();
        String properVersion = version;
        if (version.equalsIgnoreCase("latest")
                || version.equalsIgnoreCase("latest-release") ||
                version.equalsIgnoreCase("release"))
            properVersion = manifest.getAsJsonObject("latest")
                    .get("release").getAsString();
        else if (version.equalsIgnoreCase("latest-snapshot") ||
                version.equalsIgnoreCase("snapshot"))
            properVersion = manifest.getAsJsonObject("latest")
                    .get("snapshot").getAsString();
        return properVersion;
    }
}
