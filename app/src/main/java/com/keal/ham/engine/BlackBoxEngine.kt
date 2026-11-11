package com.keal.ham.engine

import com.keal.ham.domain.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.niunaijun.blackbox.BlackBoxCore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BlackBoxEngine : VirtualizationEngine {
    private val core get() = BlackBoxCore.get()

    override suspend fun clone(app: InstalledApp, userId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
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
                ZipOutputStream(FileOutputStream(bundle!!)).use { zip ->
                    apkFiles.forEachIndexed { index, apk ->
                        val entryName = if (index == 0) "base.apk" else apk.name
                        zip.putNextEntry(ZipEntry(entryName))
                        FileInputStream(apk).use { input -> input.copyTo(zip) }
                        zip.closeEntry()
                    }
                }

                core.createUser(userId)
                val result = core.installPackageAsUser(bundle!!, userId)
                check(result.success) { result.msg ?: "Engine could not install the selected app." }
            }.onFailure {
                runCatching { core.deleteUser(userId) }
            }.also {
                bundle?.delete()
            }
        }

    override suspend fun launch(packageName: String, userId: Int) =
        withContext(Dispatchers.IO) {
            runCatching {
                check(core.launchApk(packageName, userId)) {
                    "The engine did not provide a launch activity."
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
}
