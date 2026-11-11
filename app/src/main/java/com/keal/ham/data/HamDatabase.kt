package com.keal.ham.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(entities = [CloneEntity::class], version = 1, exportSchema = false)
abstract class HamDatabase : RoomDatabase() {
 abstract fun clones(): CloneDao
 companion object {
  @Volatile private var instance: HamDatabase? = null
  fun get(context: Context) = instance ?: synchronized(this) { instance ?: Room.databaseBuilder(context.applicationContext, HamDatabase::class.java, "ham.db").build().also { instance = it } }
 }
}
