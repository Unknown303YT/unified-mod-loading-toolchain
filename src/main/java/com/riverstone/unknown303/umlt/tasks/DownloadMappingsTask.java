package com.riverstone.unknown303.umlt.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;

public abstract class DownloadMappingsTask extends DefaultTask {
    @Input
    public abstract Property<String>
}
