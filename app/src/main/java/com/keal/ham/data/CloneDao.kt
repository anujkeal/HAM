package com.keal.ham.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao interface CloneDao {
 @Query("SELECT * FROM clones ORDER BY createdAt DESC") fun observeClones(): Flow<List<CloneEntity>>
 @Query("SELECT COALESCE(MAX(virtualUserId), 0) FROM clones") suspend fun maxUserId(): Int
 @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(clone: CloneEntity): Long
 @Query("UPDATE clones SET cloneName = :name WHERE id = :id") suspend fun rename(id: Long, name: String)
 @Query("UPDATE clones SET status = :status WHERE id = :id") suspend fun status(id: Long, status: String)
 @Query("UPDATE clones SET lastLaunchedAt = :time WHERE id = :id") suspend fun launched(id: Long, time: Long)
 @Query("DELETE FROM clones WHERE id = :id") suspend fun delete(id: Long)
}
