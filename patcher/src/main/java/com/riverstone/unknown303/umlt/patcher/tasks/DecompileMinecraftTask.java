package com.riverstone.unknown303.umlt.patcher.tasks;

import com.riverstone.unknown303.umlt.core.util.MavenDownloader;
import com.riverstone.unknown303.umlt.core.util.Tool;
import org.gradle.api.BuildCancelledException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class DecompileMinecraftTask extends PatcherTask {
    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<File> getClientJar();

    @Input
    public abstract Property<File> getServerJar();

    @Input
    public abstract Property<Integer> getMemoryAllocated();

    @OutputDirectory
    public abstract DirectoryProperty getClientOutputDir();

    @OutputDirectory
    public abstract DirectoryProperty getServerOutputDir();

    @TaskAction
    public void decompile() throws IOException, ParserConfigurationException, SAXException {
        File clientJar = getClientJar().get();
        File serverJar = getServerJar().get();

        File clientOutput = getClientOutputDir().getAsFile().get();
        File serverOutput = getServerOutputDir().getAsFile().get();

        File mcDecompiler = MavenDownloader.getOrDownloadLatestVersion(getGlobalCacheDir(),
                Tool.MC_DOWNLOADER, getLogger(), true, true);

        File realServerJar = serverJar;
        if (isBundlerJar(serverJar)) {
            getLogger().lifecycle("Detected Mojang bundler server jar. Extracting real server jar...");
            realServerJar = extractRealServerJar(
                    serverJar, new File(getGlobalCacheDir(), "mcJars"),
                    getMinecraftVersion().get()
            );
        }

        getLogger().lifecycle("Decompiling client jar...");
        runDecompiler(clientJar, clientOutput, mcDecompiler);
        getLogger().lifecycle("Decompiling server jar...");
        runDecompiler(realServerJar, serverOutput, mcDecompiler);
    }

    private void runDecompiler(File inputJar, File outputDir, File decompiler) throws IOException {
        getLogger().lifecycle("Decompiling file " + inputJar.getAbsolutePath() + " to folder " + outputDir.getAbsolutePath() + " with " + decompiler.getAbsolutePath());
        List<String> command = new ArrayList<>();
        command.add("%s%sbin%sjava".formatted(System.getProperty("java.home"),
                File.separator, File.separator));
        command.add("-Xmx" + getMemoryAllocated().get() + "m");
        command.add("-jar");
        command.add(decompiler.getAbsolutePath());

        command.add(inputJar.getAbsolutePath());
        command.add(outputDir.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.inheritIO();

        Process process = processBuilder.start();
        try {
            while (true) {
                try {
                    int exit = process.exitValue();
                    if (exit != 0) {
                        throw new RuntimeException("Runner JAR failed with exit code " + exit);
                    }
                    break;
                } catch (IllegalThreadStateException ignored) {
                    // Still running
                }

                // ⭐ Gradle cancellation signal
                if (Thread.currentThread().isInterrupted()) {
                    getLogger().warn("Task cancelled — terminating decompiler process...");
                    killProcessTree(process);
                    throw new TaskExecutionException(this, new BuildCancelledException("Decompile cancelled"));
                }
            }
        } catch (Exception e) {
            if (process.isAlive()) {
                killProcessTree(process);
            }
            throw new RuntimeException("Failed to run decompiler", e);
        }

        getLogger().lifecycle("File decompiled!");
    }

    private boolean isBundlerJar(File jar) {
        try (JarFile jarFile = new JarFile(jar)) {
            return jarFile.getEntry("net/minecraft/bundler/Main.class") != null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to inspect server jar", e);
        }
    }

    private File extractRealServerJar(File bundlerJar, File cacheDir, String mcVersion) {
        File extracted = new File(cacheDir, mcVersion + "-server-real.jar");

        if (extracted.exists()) {
            return extracted;
        }

        try (JarFile jarFile = new JarFile(bundlerJar)) {

            // Find the version folder dynamically
            String prefix = "META-INF/versions/";
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(prefix) && name.endsWith(".jar")) {
                    try (InputStream in = jarFile.getInputStream(entry);
                         FileOutputStream out = new FileOutputStream(extracted)) {

                        in.transferTo(out);
                    }
                    return extracted;
                }
            }

            throw new RuntimeException("Could not find embedded server.jar inside bundler");

        } catch (IOException e) {
            throw new RuntimeException("Failed to extract real server jar", e);
        }
    }

    private void killProcessTree(Process process) {
        try {
            process.descendants().forEach(ph -> {
                try { ph.destroyForcibly(); } catch (Exception ignored) {}
            });
            process.destroyForcibly();
        } catch (Exception ignored) {}
    }

}
