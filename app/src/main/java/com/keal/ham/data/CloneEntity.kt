package com.keal.ham.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "clones")
data class CloneEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val packageName: String, val appName: String, val cloneName: String, val virtualUserId: Int, val status: String = "Works", val createdAt: Long = System.currentTimeMillis(), val lastLaunchedAt: Long? = null)
