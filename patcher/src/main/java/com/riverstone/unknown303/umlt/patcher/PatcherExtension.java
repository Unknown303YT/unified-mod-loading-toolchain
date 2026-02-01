package com.riverstone.unknown303.umlt.patcher;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class PatcherExtension {
    public abstract Property<String> getMinecraftVersion();

    public abstract DirectoryProperty getPatchesDir();

    public abstract Property<Boolean> useGlobalCache();

    @Inject
    public PatcherExtension() {
        this.getMinecraftVersion().convention("26.1");
        this.useGlobalCache().convention(true);
    }
}
