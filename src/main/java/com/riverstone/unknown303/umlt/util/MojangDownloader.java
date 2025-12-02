package com.riverstone.unknown303.umlt.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;

public final class MojangDownloader {
    private MojangDownloader() {}

    public static void downloadClientJar(File buildDir, String version, File out) throws IOException {
        String properVersion = getProperVersion(buildDir, version);
        File versionDataFile = new File(getVersionDataFolder(buildDir), properVersion + ".json");
        JsonObject versionData = HTTPUtils.downloadJson(getVersionDataUrl(buildDir, version),
                versionDataFile).getAsJsonObject();
        String clientURL = versionData.getAsJsonObject("downloads")
                .getAsJsonObject("client").getAsJsonPrimitive("url").getAsString();
        HTTPUtils.downloadFile(clientURL, out);
    }

    public static void downloadServerJar(File buildDir, String version, File out) throws IOException {
        String properVersion = getProperVersion(buildDir, version);
        File versionDataFile = new File(getVersionDataFolder(buildDir), properVersion + ".json");
        JsonObject versionData = HTTPUtils.downloadJson(getVersionDataUrl(buildDir, version),
                versionDataFile).getAsJsonObject();
        String serverURL = versionData.getAsJsonObject("downloads")
                .getAsJsonObject("server").getAsJsonPrimitive("url").getAsString();
        HTTPUtils.downloadFile(serverURL, out);
    }

    private static String getVersionDataUrl(File buildDir, String version) throws IOException {
        String properVersion = getProperVersion(buildDir, version);

        File manifestFile = new File(getVersionDataFolder(buildDir), "mc_version_manifest.json");
        JsonObject json =
                HTTPUtils.downloadJson(Constants.MC_VERSION_MANIFEST, manifestFile).getAsJsonObject();
        for (JsonElement data : json.getAsJsonArray("versions")) {
            JsonObject versionData = data.getAsJsonObject();
            return versionData.getAsJsonPrimitive("url").getAsString();
        }

        throw new IllegalArgumentException("Version " + properVersion + " not found in Mojang data!");
    }

    public static String getProperVersion(File buildDir, String version) throws IOException {
        File manifestFile = new File(getVersionDataFolder(buildDir), "mc_version_manifest.json");
        JsonObject manifest = HTTPUtils.downloadJson(Constants.MC_VERSION_MANIFEST, manifestFile)
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

    private static File getVersionDataFolder(File buildDir) {
        File dir = new File(buildDir, "version_data");
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }
}
