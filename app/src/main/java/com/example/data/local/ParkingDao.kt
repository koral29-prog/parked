package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingDao {
    @Query("SELECT * FROM parking_sessions WHERE isActive = 1 ORDER BY timestamp DESC LIMIT 1")
    fun getActiveParking(): Flow<ParkingEntity?>

    @Query("SELECT * FROM parking_sessions ORDER BY timestamp DESC")
    fun getAllParkingHistory(): Flow<List<ParkingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParking(parking: ParkingEntity): Long

    @Update
    suspend fun updateParking(parking: ParkingEntity)

    @Query("UPDATE parking_sessions SET isActive = 0 WHERE isActive = 1")
    suspend fun clearActiveParking()

    @Query("DELETE FROM parking_sessions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM parking_sessions")
    suspend fun deleteAll()
}
