package com.keal.ham.engine

import android.util.Log
import com.keal.ham.domain.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.niunaijun.blackbox.BlackBoxCore
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BlackBoxEngine : VirtualizationEngine {
    private val core get() = BlackBoxCore.get()

    override suspend fun clone(
        app: InstalledApp,
        userId: Int,
        onProgress: (String) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var bundle: File? = null
        runCatching {
            val context = BlackBoxCore.getContext()
            val info = context.packageManager.getApplicationInfo(app.packageName, 0)
            val apkFiles = buildList {
                add(File(info.sourceDir))
                info.splitSourceDirs?.forEach { add(File(it)) }
            }.filter { it.isFile }

            check(apkFiles.isNotEmpty()) { "No APK files were found for ${app.packageName}." }

            bundle = File(context.cacheDir, "ham_clone_${userId}.apks")
            bundle!!.parentFile?.mkdirs()

            onProgress("Preparing ${apkFiles.size} APK files…")
            ZipOutputStream(
                BufferedOutputStream(FileOutputStream(bundle!!), COPY_BUFFER_SIZE)
            ).use { zip ->
                zip.setLevel(Deflater.NO_COMPRESSION)
                apkFiles.forEachIndexed { index, apk ->
                    onProgress("Copying APK ${index + 1} of ${apkFiles.size}…")
                    zip.putNextEntry(ZipEntry(if (index == 0) "base.apk" else apk.name))
                    BufferedInputStream(FileInputStream(apk), COPY_BUFFER_SIZE).use { input ->
                        input.copyTo(zip, COPY_BUFFER_SIZE)
                    }
                    zip.closeEntry()
                }
            }

            onProgress("Starting virtual installation…")
            core.createUser(userId)
            onProgress("Installing app in HAM…")
            val result = core.installPackageAsUser(bundle!!, userId)
            check(result.success) { result.msg ?: "Engine could not install the selected app." }
            check(core.isInstalled(app.packageName, userId)) {
                "BlackBox reported installation success, but ${app.packageName} is not installed in virtual user ${userId}."
            }
            onProgress("Finishing clone…")
        }.onFailure {
            Log.e(TAG, "Clone failed for ${app.packageName}, virtualUser=${userId}", it)
            runCatching { core.deleteUser(userId) }
        }.also {
            bundle?.delete()
        }
    }

    override suspend fun launch(packageName: String, userId: Int) =
        withContext(Dispatchers.IO) {
            runCatching {
                check(core.isInstalled(packageName, userId)) {
                    "The clone is missing from HAM's virtual user. Delete it and clone the app again."
                }

                // Resolve and launch only through BlackBox's virtual package manager.
                // Falling back to Android's host PackageManager returns the ORIGINAL
                // app's launcher Intent, which is not a valid virtual launch target.
                val launched = core.launchApk(packageName, userId)
                check(launched) {
                    "HAM could not find a launcher activity for $packageName inside the clone."
                }
                Log.i(TAG, "Virtual launch requested: package=$packageName user=$userId")
                Unit
            }.onFailure {
                Log.e(TAG, "Virtual launch failed: package=$packageName user=$userId", it)
            }
        }

    override suspend fun clear(packageName: String, userId: Int) =
        withContext(Dispatchers.IO) { runCatching { core.clearPackage(packageName, userId) } }

    override suspend fun stop(packageName: String, userId: Int) =
        withContext(Dispatchers.IO) { runCatching { core.stopPackage(packageName, userId) } }

    override suspend fun delete(packageName: String, userId: Int) =
        withContext(Dispatchers.IO) {
            runCatching {
                core.uninstallPackageAsUser(packageName, userId)
                core.deleteUser(userId)
            }
        }

    private companion object {
        const val TAG = "HAM-BlackBox"
        const val COPY_BUFFER_SIZE = 256 * 1024
    }
}