package com.keal.ham.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.drawable.toBitmap
import com.keal.ham.domain.InstalledApp
import com.keal.ham.data.CloneEntity

@Composable
private fun ApkSizeRow(label: String, bytes: Long, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyLarge,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
        Text(
            text = String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / 1048576.0),
            style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyLarge,
            fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun Confirm(s: HamState, name: String, onName: (String) -> Unit, vm: HamViewModel, m: Modifier = Modifier) {
    val a = s.selected ?: return
    val context = LocalContext.current
    val risky = remember(a.packageName) {
        runCatching {
            val json = org.json.JSONObject(context.assets.open("risk_apps.json").bufferedReader().use { it.readText() })
            val packages = json.getJSONArray("packages")
            (0 until packages.length()).any { packages.getString(it) == a.packageName }
        }.getOrDefault(false)
    }
    val totalApkBytes = a.apkBytes + a.splitApkBytes

    Column(
        m.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(25.dp))
        Image(a.icon.toBitmap(128, 128).asImageBitmap(), null, Modifier.size(90.dp))
        Text(a.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(a.packageName, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(22.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onName,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Clone name") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        ApkSizeRow("Version", 0L) // replaced below with version-specific row
        // APK payload sizes are the sizes of the already-installed APK files,
        // not the compressed download size shown by an app store.
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Version", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Text(a.version, style = MaterialTheme.typography.bodyLarge)
            }
            HorizontalDivider()
            ApkSizeRow("Base APK", a.apkBytes)
            if (a.splitApkBytes > 0L) {
                ApkSizeRow("Split APKs", a.splitApkBytes)
            }
            HorizontalDivider()
            ApkSizeRow("Total APK files", totalApkBytes, bold = true)
            Text(
                "These are installed APK file sizes, not the app-store download size or app data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (risky) {
            Text(
                "This app may not work in HAM or may restrict your account.",
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.create(name) },
            enabled = !s.busy,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (s.busy) CircularProgressIndicator(Modifier.size(18.dp)) else Text("Clone")
        }
    }
}
