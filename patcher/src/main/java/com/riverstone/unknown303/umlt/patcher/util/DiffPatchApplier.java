package com.riverstone.unknown303.umlt.patcher.util;

import io.codechicken.diffpatch.util.Patch;
import io.codechicken.diffpatch.util.PatchFile;

import java.util.List;

public class DiffPatchApplier {
    public static List<String> applyPatch(List<String> source, List<String> patchLines) {
        PatchFile patchFile = PatchFile.fromLines("patch", patchLines, true);

        for (Patch patch : patchFile.patches) {
            patch.
        }
    }
}
