package com.keal.ham.data
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.keal.ham.domain.InstalledApp
import com.keal.ham.engine.VirtualizationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
class CloneRepository(context: Context, private val engine: VirtualizationEngine) {
 private val app = context.applicationContext
 private val dao = HamDatabase.get(app).clones()
 val clones: Flow<List<CloneEntity>> = dao.observeClones()
 @Suppress("DEPRECATION")
 suspend fun installed(showSystem: Boolean): List<InstalledApp> = withContext(Dispatchers.IO) {
  val pm = app.packageManager
  pm.getInstalledApplications(PackageManager.GET_META_DATA).asSequence()
   .filter { it.packageName != app.packageName && pm.getLaunchIntentForPackage(it.packageName) != null }
   .filter { showSystem || it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
   .mapNotNull { info -> runCatching {
    val pi = pm.getPackageInfo(info.packageName, 0)
    val size = (listOf(info.sourceDir) + (info.splitSourceDirs?.toList() ?: emptyList())).filterNotNull().sumOf { File(it).length() }
    InstalledApp(info.packageName, pm.getApplicationLabel(info).toString(), pi.versionName ?: "Unknown", size, info.flags and ApplicationInfo.FLAG_SYSTEM != 0, pm.getApplicationIcon(info))
   }.getOrNull() }.sortedBy { it.label.lowercase() }.toList()
 }
 suspend fun create(appInfo: InstalledApp, name: String): Result<Unit> {
  val user = dao.maxUserId() + 1
  val result = engine.clone(appInfo, user)
  if (result.isSuccess) dao.insert(CloneEntity(packageName=appInfo.packageName, appName=appInfo.label, cloneName=name, virtualUserId=user))
  return result
 }
 suspend fun launch(c: CloneEntity): Result<Unit> {
  val result=engine.launch(c.packageName,c.virtualUserId)
  dao.status(c.id,if(result.isSuccess) "Works" else "Crashed")
  if(result.isSuccess) dao.launched(c.id,System.currentTimeMillis())
  return result
 }
 suspend fun rename(c:CloneEntity,n:String)=dao.rename(c.id,n)
 suspend fun clear(c:CloneEntity)=engine.clear(c.packageName,c.virtualUserId)
 suspend fun stop(c:CloneEntity)=engine.stop(c.packageName,c.virtualUserId)
 suspend fun delete(c:CloneEntity):Result<Unit> { val r=engine.delete(c.packageName,c.virtualUserId); if(r.isSuccess) dao.delete(c.id); return r }
}
