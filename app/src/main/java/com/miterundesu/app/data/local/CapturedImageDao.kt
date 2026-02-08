package com.miterundesu.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CapturedImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CapturedImageEntity)

    @Query("DELETE FROM captured_images WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM captured_images ORDER BY captured_at DESC")
    suspend fun getAll(): List<CapturedImageEntity>

    @Query("DELETE FROM captured_images WHERE expiration_date < :currentTimeMillis")
    suspend fun deleteExpired(currentTimeMillis: Long = System.currentTimeMillis())

    @Query("DELETE FROM captured_images")
    suspend fun deleteAll()
}
