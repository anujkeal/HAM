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

# Meta authentication and other embedded web flows can reject nonstandard
# WebView user agents. Preserve stock UA and let WebView observe real network state.
webview_file = Path("engine/blackbox/Bcore/src/main/java/top/niunaijun/blackbox/fake/service/WebViewProxy.java")
if not webview_file.is_file():
    sys.exit(f"BlackBox WebView proxy not found: {webview_file}")
webview = webview_file.read_text(encoding="utf-8")

ua_block = """                    String userAgent = settings.getUserAgentString();
                    if (userAgent != null && !userAgent.contains("BlackBox")) {
                        settings.setUserAgentString(userAgent + " BlackBox");
                    }
"""
if ua_block in webview:
    webview = webview.replace(ua_block, """                    // Preserve the stock Android System WebView user agent.
                    // Meta login/security flows may reject modified user agents.
""")
elif 'settings.setUserAgentString(userAgent + " BlackBox")' in webview:
    sys.exit("Unexpected WebView user-agent patch shape; refusing to modify.")

network_block = """                    try {
                        webView.setNetworkAvailable(true);
                    } catch (Exception e) {
                        
                    }

"""
if network_block in webview:
    webview = webview.replace(network_block, """                    // Let WebView observe the real Android network state.
""")
elif "webView.setNetworkAvailable(true)" in webview:
    sys.exit("Unexpected WebView network-state patch shape; refusing to modify.")

webview_file.write_text(webview, encoding="utf-8")
print(f"Patched {webview_file}: preserve stock UA and real network state.")

# Embedded Meta authentication uses cookies across its own WebView origins.
# Explicitly enable the platform cookie manager for the isolated guest WebView.
if "import android.webkit.CookieManager;" not in webview:
    webview = webview.replace(
        "import android.webkit.WebViewDatabase;",
        "import android.webkit.WebViewDatabase;" + chr(10) + "import android.webkit.CookieManager;"
    )
cookie_anchor = """                    WebSettings settings = webView.getSettings();
                    if (settings != null) {
"""
cookie_patch = """                    WebSettings settings = webView.getSettings();
                    if (settings != null) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.setAcceptCookie(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            cookieManager.setAcceptThirdPartyCookies(webView, true);
                        }
"""
if cookie_anchor in webview and "setAcceptThirdPartyCookies(webView, true)" not in webview:
    webview = webview.replace(cookie_anchor, cookie_patch, 1)
elif "setAcceptThirdPartyCookies(webView, true)" not in webview:
    sys.exit("WebView settings insertion point not found; refusing cookie patch.")

# Never write full authentication URLs (which contain temporary security tokens)
# to Logcat. Log only the host.
webview = webview.replace(
    'Slog.d(TAG, "WebView: loadUrl called with: " + url);',
    'Slog.d(TAG, "WebView: loadUrl host=" + (url == null ? "null" : android.net.Uri.parse(url).getHost()));'
)
webview_file.write_text(webview, encoding="utf-8")
print(f"Patched {webview_file}: WebView cookies enabled and auth URL logging redacted.")
