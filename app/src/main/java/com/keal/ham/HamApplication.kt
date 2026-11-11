package com.keal.ham
import android.app.Application
import android.content.Context
import android.util.Log
import top.niunaijun.blackbox.BlackBoxCore
import top.niunaijun.blackbox.app.configuration.ClientConfiguration
class HamApplication : Application() {
 override fun attachBaseContext(base: Context) {
  super.attachBaseContext(base)
  runCatching {
   BlackBoxCore.get().closeCodeInit()
   BlackBoxCore.get().doAttachBaseContext(base, object : ClientConfiguration() {
    override fun getHostPackageName(): String = base.packageName
    override fun isEnableDaemonService(): Boolean = true
    override fun isEnableLauncherActivity(): Boolean = true
    override fun isUseVpnNetwork(): Boolean = false
   })
  }.onFailure { Log.e("HAM", "Engine attach failed", it) }
 }
 override fun onCreate() {
  super.onCreate()
  runCatching { BlackBoxCore.get().doCreate() }.onFailure { Log.e("HAM", "Engine startup failed", it) }
 }
}
