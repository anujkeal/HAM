#!/usr/bin/env python3
"""Patch the pinned BlackBox engine to retain every split from an installed package.

HAM builds its .apks bundle from PackageManager.sourceDir + splitSourceDirs.
Those paths are already the exact APK set selected for this device, including
installed feature-module splits. The upstream heuristic kept only ABI, density,
and language splits, silently dropping feature modules used by large apps.
"""
from pathlib import Path
import re
import sys

engine_file = Path("engine/blackbox/Bcore/src/main/java/top/niunaijun/blackbox/core/system/pm/BPackageManagerService.java")
if not engine_file.is_file():
    sys.exit(f"BlackBox source not found: {engine_file}")

source = engine_file.read_text(encoding="utf-8")
pattern = re.compile(
    r"    private static List<File> selectSplitApks\(List<File> apks, File baseApk, boolean is64Bit\) \{.*?\n    \}\n\n    private static String stripApkExtension",
    re.DOTALL,
)
replacement = """    private static List<File> selectSplitApks(List<File> apks, File baseApk, boolean is64Bit) {
        if (apks == null || apks.isEmpty() || baseApk == null) {
            return Collections.emptyList();
        }

        // HAM creates this archive from the host PackageManager's installed
        // sourceDir and splitSourceDirs. Android has already selected the
        // device-compatible ABI, density and locale splits. Keep every
        // remaining split: feature modules can be essential to large apps
        // (for example Instagram) and are not necessarily config.* splits.
        ArrayList<File> result = new ArrayList<>();
        for (File split : apks) {
            if (split != null && split.isFile() && !split.equals(baseApk)) {
                result.add(split);
            }
        }
        Slog.i(TAG, "HAM split import: retaining " + result.size() + " installed splits");
        return result;
    }

    private static String stripApkExtension"""
patched, count = pattern.subn(replacement, source, count=1)
if count != 1:
    sys.exit("Expected upstream split-selection method was not found exactly once; refusing to patch.")
engine_file.write_text(patched, encoding="utf-8")
print(f"Patched {engine_file}: all installed splits will be retained.")
