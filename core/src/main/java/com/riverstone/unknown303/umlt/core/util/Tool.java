package com.riverstone.unknown303.umlt.core.util;

import java.io.File;
import java.util.function.Function;

public enum Tool {
    VINEFLOWER(Constants.VINEFLOWER_LOCATION, "vineflower",
            file -> new File(file, "vineflower"));
    private final String mavenUrl;
    private final String jarFileStart;
    private final Function<File, File> cacheDir;

    Tool(String mavenUrl, String jarFileStart, Function<File, File> cacheDir) {
        this.mavenUrl = mavenUrl;
        this.jarFileStart = jarFileStart;
        this.cacheDir = cacheDir;
    }

    public String getMavenUrl() {
        return mavenUrl;
    }

    public String getJarFileStart() {
        return jarFileStart;
    }

    public File getCacheDir(File buildDir) {
        return cacheDir.apply(buildDir);
    }
}
