package com.riverstone.unknown303.umlt.core.util;

import org.gradle.api.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class MavenDownloader {
    public static File getOrDownloadLatestVersion(File buildDir, Tool tool,
                                                  Logger logger, boolean release,
                                                  boolean shadow) throws IOException, ParserConfigurationException, SAXException {
        String latestVersion = fetchLatestVersion(buildDir, tool, logger, release);
        String url = "%s%s/%s".formatted(tool.getMavenUrl(), latestVersion,
                artifactFileName(tool, latestVersion, shadow));
        File artifactFile = new File(tool.getCacheDir(buildDir),
                artifactFileName(tool, latestVersion, shadow));

        HTTPUtils.downloadFile(url, artifactFile, logger);

        return artifactFile;
    }

    public static String fetchLatestVersion(File buildDir, Tool tool, Logger logger, boolean release) throws IOException, SAXException, ParserConfigurationException {
        File metadataFile = new File(tool.getCacheDir(buildDir), "maven-metadata.xml");
        HTTPUtils.downloadFile(tool.getMavenUrl() + "maven-metadata.xml",
                metadataFile, logger, true);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(metadataFile);

        NodeList nodes = doc.getElementsByTagName(release ? "release" : "latest");
        if (nodes.getLength() > 0)
            return nodes.item(0).getTextContent().trim();

        // Fallback to versions
        NodeList versionNodes = doc.getElementsByTagName("version");
        if (versionNodes.getLength() > 0)
            return versionNodes.item(versionNodes.getLength() - 1).getTextContent().trim();

        throw new IllegalStateException("No version information found in metadata.");
    }

    private static String artifactFileName(Tool tool, String version, boolean shadow) {
        return "%s-%s%s".formatted(tool.getJarFileStart(), version,
                shadow ? "-all.jar" : ".jar");
    }
}
