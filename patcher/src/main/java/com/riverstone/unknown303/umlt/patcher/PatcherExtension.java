package com.riverstone.unknown303.umlt.patcher;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.File;

public abstract class PatcherExtension {
    private final DirectoryProperty patchDir;

    @Inject
    public PatcherExtension(ObjectFactory factory) {
        this.patchDir = factory.directoryProperty();

        this.getMinecraftVersion().convention("26.1");
        this.getPatchDir().convention(factory.directoryProperty().fileValue(new File("patches")));
    }

    public abstract Property<String> getMinecraftVersion();

    public DirectoryProperty getPatchDir() {
        return patchDir;
    }
}
