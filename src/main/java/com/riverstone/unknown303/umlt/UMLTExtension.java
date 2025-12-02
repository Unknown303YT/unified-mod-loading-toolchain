package com.riverstone.unknown303.umlt;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.File;

public abstract class UMLTExtension {
    private final MappingsSpec mappings;
    private final DirectoryProperty patchDir;

    @Inject
    public UMLTExtension(ObjectFactory factory) {
        this.mappings = factory.newInstance(MappingsSpec.class);
        this.patchDir = factory.directoryProperty();

        this.getMinecraftVersion().convention("1.20.1");
        this.getPatchDir().convention(factory.directoryProperty().fileValue(new File("patches")));
    }

    public abstract Property<String> getMinecraftVersion();

    public MappingsSpec getMappings() {
        return mappings;
    }

    public DirectoryProperty getPatchDir() {
        return patchDir;
    }
}
