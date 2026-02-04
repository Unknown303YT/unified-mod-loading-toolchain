package com.riverstone.unknown303.umlt.patcher.tasks;

import com.riverstone.unknown303.umlt.core.util.MavenDownloader;
import com.riverstone.unknown303.umlt.core.util.Tool;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
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

        clientOutput.delete();
        serverOutput.delete();

        File vineflowerCLI = MavenDownloader.getOrDownloadLatestVersion(getGlobalCacheDir(),
                Tool.VINEFLOWER, getLogger(), true, false);

        File realServerJar = serverJar;
        if (isBundlerJar(serverJar)) {
            getLogger().lifecycle("Detected Mojang bundler server jar. Extracting real server jar...");
            realServerJar = extractRealServerJar(
                    serverJar, new File(getGlobalCacheDir(), "mcJars"),
                    getMinecraftVersion().get()
            );
        }

        getLogger().lifecycle("Decompiling client jar...");
        runDecompiler(clientJar, clientOutput, vineflowerCLI);
        getLogger().lifecycle("Decompiling server jar...");
        runDecompiler(realServerJar, serverOutput, vineflowerCLI);
    }

    private void runDecompiler(File inputJar, File outputDir, File decompiler) throws IOException {
        getLogger().lifecycle("Decompiling file " + inputJar.getAbsolutePath() + " to folder " + outputDir.getAbsolutePath() + " with " + decompiler.getAbsolutePath());

        String javaExec = new File(
                System.getProperty("java.home"),
                "bin" + File.separator + "java"
        ).getAbsolutePath();

        List<String> command = new ArrayList<>();
        command.add(javaExec);
        command.add("-Xmx" + getMemoryAllocated().get() + "m");
        command.add("-jar");
        command.add(decompiler.getAbsolutePath());

        // CLI Options
        command.add("--ind=4");
        command.add("--lvt=1");
        command.add("--fdi=1");
        command.add("--threads=1");
        command.add("--optimize=0");
        command.add("--decompile-generics=1");
        command.add("--decompile-lambdas=1");
        command.add("--decompile-inner=1");
        command.add("--rename=1");
        command.add("--remove-bridge=0");
        command.add("--remove-synthetic=0");
        command.add("--outputdir=1");

        // Inputs and Outputs
        command.add(inputJar.getAbsolutePath());
        command.add(outputDir.getAbsolutePath());

        getLogger().lifecycle("COMMAND: " + String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = null;

        try {
            process = processBuilder.start();

            Thread outputThread = outputThread(process);

            int exit = process.waitFor();

            if (exit != 0)
                throw new RuntimeException("Vineflower CLI failed with exit code " + exit);
        } catch (InterruptedException e) {
            // Task cancelled
            if (process.isAlive())
                killProcessTree(process);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Decompile cancelled", e);
        } catch (Exception e) {
            if (process != null && process.isAlive())
                killProcessTree(process);
            throw new RuntimeException("Failed to run Vineflower CLI", e);
        }

        getLogger().lifecycle("File decompiled!");
    }

    private @NotNull Thread outputThread(Process process) {
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    getLogger().info("[Vineflower] " + line);
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading line", e);
            }
        });
        outputThread.setDaemon(true);
        outputThread.start();
        return outputThread;
    }

    private boolean isBundlerJar(File jar) {
        try (JarFile jarFile = new JarFile(jar)) {
            return jarFile.getEntry("net/minecraft/bundler/Main.class") != null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to inspect server jar", e);
        }
    }

    private File extractRealServerJar(File bundlerJar, File cacheDir, String mcVersion) {
        File extracted = new File(cacheDir + File.separator + mcVersion, "server-real-" + mcVersion.replace('.', '_') + ".jar");

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
                try {
                    ph.destroyForcibly();
                } catch (Exception ignored) {}
            });
            process.destroyForcibly();
        } catch (Exception ignored) {}
    }
}