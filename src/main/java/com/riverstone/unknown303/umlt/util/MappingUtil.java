package com.riverstone.unknown303.umlt.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.tiny.Tiny2Util;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.tinyremapper.IMappingProvider;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class MappingUtil {
    private MappingUtil() {}

    /* MAPPING FORMAT CONVERSIONS */

    public static void parchmentJsonToMappingTree(JsonObject json, MappingTree tree) {
        List<String> destinationNamespaces = new ArrayList<>(tree.getDstNamespaces());
        if (!destinationNamespaces.contains("parchment")) {
            destinationNamespaces.add("parchment");
            tree.setDstNamespaces(destinationNamespaces);
        }

        int mojmapNamespace = tree.getNamespaceId("mojmap");
        int parchmentNamespace = tree.getNamespaceId("parchment");

        JsonArray classes = json.getAsJsonArray("classes");
        Util.forJson(classes, classData -> {
            String className = classData.getAsJsonPrimitive("name").getAsString();
            MappingTree.ClassMapping classMap = tree.getClass(className, mojmapNamespace);
            if (classMap == null)
                return;

            classMap.setDstName(className, parchmentNamespace);
            setJavaDoc(classData, classMap);

            if (classData.has("methods")) {
                JsonArray methods = classData.getAsJsonArray("methods");
                Util.forJson(methods, methodData -> {
                    String methodName = methodData.getAsJsonPrimitive("name").getAsString();
                    String descriptor = methodData.getAsJsonPrimitive("descriptor").getAsString();

                    MappingTree.MethodMapping methodMap =
                            classMap.getMethod(methodName, descriptor, mojmapNamespace);
                    if (methodMap == null)
                        return;

                    methodMap.setDstName(methodName, parchmentNamespace);
                    setJavaDoc(methodData, methodMap);

                    if (!methodData.has("parameters"))
                        return;

                    JsonArray arguments = methodData.getAsJsonArray("parameters");
                    Util.forJson(arguments, argumentData -> {
                        String paramName = argumentData.getAsJsonPrimitive("name").getAsString();
                        int index = argumentData.getAsJsonPrimitive("index").getAsInt();
                        MappingTree.MethodArgMapping argumentMap =
                                methodMap.getArg(index, index, "official");
                        if (argumentMap == null)
                            return;

                        argumentMap.setDstName(paramName, parchmentNamespace);
                        setJavaDoc(argumentData, argumentMap);
                    });
                });
            }

            if (classData.has("fields")) {
                JsonArray fields = classData.getAsJsonArray("fields");
                Util.forJson(fields, fieldData -> {
                    String fieldName = fieldData.getAsJsonPrimitive("name").getAsString();
                    String descriptor = fieldData.getAsJsonPrimitive("descriptor").getAsString();

                    MappingTree.FieldMapping fieldMap =
                            classMap.getField(fieldName, descriptor, mojmapNamespace);
                    if (fieldMap == null)
                        return;

                    fieldMap.setDstName(fieldName, parchmentNamespace);
                    setJavaDoc(fieldData, fieldMap);
                });
            }
        });
    }

    private static void setJavaDoc(JsonObject json, MappingTree.ElementMapping mapping) {
        if (!json.has("javadoc"))
            return;

        JsonArray javadoc = json.getAsJsonArray("javadoc");
        StringBuilder builder = new StringBuilder();

        for (JsonElement docLine : javadoc) {
            if (!builder.isEmpty())
                builder.append("\n");
            builder.append(docLine.getAsString());
        }

        mapping.setComment(builder.toString());
    }

    /* REMAPPING */

    public static IMappingProvider createMappingProvider(File mappingsFile) throws IOException {
        FileInputStream fileInput = new FileInputStream(mappingsFile);
        BufferedReader reader = new BufferedReader(Files.newBufferedReader(mappingsFile.toPath()));
        TinyUtils.createTinyMappingProvider(reader, );
    }
}
