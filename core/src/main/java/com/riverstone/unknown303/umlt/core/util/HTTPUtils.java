package com.riverstone.unknown303.umlt.core.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPUtils {
    private HTTPUtils() {}

    /**
     * Downloads a file from the String provided to the File.
     * @param urlString The URL to download from.
     * @param outputFile The location to download to.
     * @throws IOException In case of an error in the transfer or in security.
     * @see HTTPUtils#downloadFile(String, File, boolean)
     */
    public static void downloadFile(String urlString, File outputFile) throws IOException {
        downloadFile(urlString, outputFile, false);
    }

    /**
     * Downloads a file from the String provided to the File.
     * @param urlString The URL to download from.
     * @param outputFile The location to download to.
     * @param ignoreExists Whether to download even if the file is already present.
     * @throws IOException In case of an error in the transfer or in security.
     */
    public static void downloadFile(String urlString, File outputFile, boolean ignoreExists) throws IOException {
        if (outputFile.exists() && !ignoreExists)
            return;

        outputFile.getParentFile().mkdirs();

        URL url = new URL(urlString);
        HttpURLConnection connection = openConnectionWithRedirects(url);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            throw new IOException("Failed to download " + urlString + ", Code: " + connection.getResponseCode());

        InputStream input = connection.getInputStream();
        FileOutputStream fileOutput = new FileOutputStream(outputFile);

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1)
            fileOutput.write(buffer, 0, bytesRead);

        input.close();
        fileOutput.close();
    }

    /**
     * Opens an HTTPS connection with redirects configured to also require HTTPS.
     * @param url The URL to connect to.
     * @return An {@link HttpURLConnection} ready to connect to the provided URL.
     * @throws IOException In case of an insecure link being used or an invalid redirect.
     */
    private static HttpURLConnection openConnectionWithRedirects(URL url) throws IOException {
        if (!url.getProtocol().equalsIgnoreCase("https"))
            throw new IOException(("Insecure download blocked: " +
                    "URL must use HTTPS, but uses %s. URL: %s")
                    .formatted(url.getProtocol(), url));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "UMLT-Downloader");

        int status = connection.getResponseCode();
        if (status >= 300 && status <= 399) {
            String redirectUrl = connection.getHeaderField("Location");
            if (redirectUrl == null)
                throw new IOException("URL %s sent a redirect without a location!"
                        .formatted(url));
            return openConnectionWithRedirects(new URL(redirectUrl));
        }

        return connection;
    }

    public static JsonElement downloadJson(String urlString, File outputFile) throws IOException {
        downloadFile(urlString, outputFile, true);
        FileReader reader = new FileReader(outputFile);
        JsonElement json = JsonParser.parseReader(reader);
        reader.close();
        return json;
    }
}
