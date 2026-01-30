package com.riverstone.unknown303.umlt.patcher;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public abstract class PatcherExtension {
    public abstract Property<String> getMinecraftVersion();
    public abstract DirectoryProperty getWorkingDirectory();
}
