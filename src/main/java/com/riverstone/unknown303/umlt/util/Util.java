package com.riverstone.unknown303.umlt.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class Util {
    public static File createFile(File parent, String child) throws IOException {
        File file = new File(parent, child);
        file.getParentFile().mkdirs();
        file.createNewFile();
        return file;
    }

    public static File createFolder(File parent, String child) {
        File file = new File(parent, child);
        file.mkdirs();
        return file;
    }

    public static void forJson(JsonArray array, Consumer<JsonObject> action) {
        array.forEach(json -> action.accept(json.getAsJsonObject()));
    }
}
