package com.riverstone.unknown303.umlt.util;

import java.io.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public final class JarMerger {
    private JarMerger() {}

    public static void merge(File clientJar, File serverJar, File outputJar) throws IOException {
        JarOutputStream jarOutput = new JarOutputStream(new FileOutputStream(outputJar));
        Set<String> written = new HashSet<>();

        JarFile client = new JarFile(clientJar);
        Enumeration<JarEntry> entries = client.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory())
                continue;

            jarOutput.putNextEntry(new JarEntry(entry.getName()));
            InputStream input = client.getInputStream(entry);
            input.transferTo(jarOutput);
            input.close();
            jarOutput.closeEntry();

            written.add(entry.getName());
        }

        client.close();

        JarFile server = new JarFile(serverJar);
        entries = server.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory())
                continue;

            jarOutput.putNextEntry(new JarEntry(entry.getName()));
            InputStream input = server.getInputStream(entry);
            input.transferTo(jarOutput);
            input.close();
            jarOutput.closeEntry();

            written.add(entry.getName());
        }

        server.close();

        jarOutput.close();
    }
}
