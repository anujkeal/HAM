package com.keal.ham.engine

import com.keal.ham.domain.InstalledApp

interface VirtualizationEngine {
    suspend fun clone(
        app: InstalledApp,
        userId: Int,
        onProgress: (String) -> Unit = {}
    ): Result<Unit>

    suspend fun launch(packageName: String, userId: Int): Result<Unit>
    suspend fun clear(packageName: String, userId: Int): Result<Unit>
    suspend fun stop(packageName: String, userId: Int): Result<Unit>
    suspend fun delete(packageName: String, userId: Int): Result<Unit>
}