package com.keal.ham.domain

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val version: String,
    val apkBytes: Long,
    val system: Boolean,
    val icon: Drawable,
    val splitApkBytes: Long = 0L
)