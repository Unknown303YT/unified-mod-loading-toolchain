package com.riverstone.unknown303.umlt;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public abstract class MappingsSpec {

    @Inject
    public MappingsSpec(ObjectFactory factory) {
        getKind().convention("official");
    }

    public abstract Property<String> getKind();
    public abstract Property<String> getParchmentVersion();
    public abstract Property<Integer> getYarnBuild();
    public abstract Property<Integer> getQuiltBuild();
}
