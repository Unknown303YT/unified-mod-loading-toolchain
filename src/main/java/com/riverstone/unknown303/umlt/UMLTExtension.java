package com.riverstone.unknown303.umlt;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public class UMLTExtension {
    private String minecraftVersion = "1.20.1";
    private final MappingsConfig mappings;

    @Inject
    public UMLTExtension(ObjectFactory objects) {
        this.mappings = objects.newInstance(MappingsConfig.class);
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public MappingsConfig getMappings() {
        return mappings;
    }

    public void mappings(Action<? super MappingsConfig> action) {
        action.execute(mappings);
    }

    public static class MappingsConfig {
        private String type = "official";
        private String version = "latest";

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
