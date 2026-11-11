package com.keal.ham.engine
import com.keal.ham.domain.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.niunaijun.blackbox.BlackBoxCore
class BlackBoxEngine : VirtualizationEngine {
 private val core get() = BlackBoxCore.get()
 override suspend fun clone(app: InstalledApp, userId: Int) = withContext(Dispatchers.IO) {
  runCatching {
   core.createUser(userId)
   val result = core.installPackageAsUser(app.packageName, userId)
   check(result.success) { result.msg ?: "Engine could not install the selected app." }
  }.onFailure { runCatching { core.deleteUser(userId) } }
 }
 override suspend fun launch(packageName: String, userId: Int) = withContext(Dispatchers.IO) { runCatching { check(core.launchApk(packageName, userId)) { "The engine did not provide a launch activity." } } }
 override suspend fun clear(packageName: String, userId: Int) = withContext(Dispatchers.IO) { runCatching { core.clearPackage(packageName, userId) } }
 override suspend fun stop(packageName: String, userId: Int) = withContext(Dispatchers.IO) { runCatching { core.stopPackage(packageName, userId) } }
 override suspend fun delete(packageName: String, userId: Int) = withContext(Dispatchers.IO) { runCatching { core.uninstallPackageAsUser(packageName, userId); core.deleteUser(userId) } }
}
