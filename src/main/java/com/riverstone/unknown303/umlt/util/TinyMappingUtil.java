package com.riverstone.unknown303.umlt.util;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;

import java.io.File;

public final class TinyMappingUtil {
    private TinyMappingUtil() {}

    public static void officialToTiny(File officialMapping, File outputFile) {
        MappingReader.read(officialMapping.toPath(), MappingFormat.); // TODO: Investigate https://github.com/FabricMC/fabric-loom/tree/dev/1.13/src/main/java/net/fabricmc/loom/configuration/providers/mappings/mojmap
    }
}
