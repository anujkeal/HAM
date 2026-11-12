package com.keal.ham.engine

import android.content.Intent
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
            // APK files are ZIP containers already. Re-compressing them usually
            // saves almost nothing but consumes substantial CPU on large apps.
            ZipOutputStream(
                BufferedOutputStream(FileOutputStream(bundle!!), COPY_BUFFER_SIZE)
            ).use { zip ->
                zip.setLevel(Deflater.NO_COMPRESSION)
                apkFiles.forEachIndexed { index, apk ->
                    onProgress("Copying APK ${index + 1} of ${apkFiles.size}…")
                    val entryName = if (index == 0) "base.apk" else apk.name
                    zip.putNextEntry(ZipEntry(entryName))
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
            onProgress("Finishing clone…")
        }.onFailure {
            runCatching { core.deleteUser(userId) }
        }.also {
            bundle?.delete()
        }
    }

    override suspend fun launch(packageName: String, userId: Int) =
        withContext(Dispatchers.IO) {
            runCatching {
                val launched = runCatching { core.launchApk(packageName, userId) }
                    .getOrDefault(false)
                if (!launched) {
                    val context = BlackBoxCore.getContext()
                    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                        ?: error("No launcher activity is declared for $packageName.")
                    intent.setPackage(packageName)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    core.startActivity(intent, userId)
                }
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
        const val COPY_BUFFER_SIZE = 256 * 1024
    }
}