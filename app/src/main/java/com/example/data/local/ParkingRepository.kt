package com.example.data.local

import kotlinx.coroutines.flow.Flow

class ParkingRepository(private val parkingDao: ParkingDao) {

    val activeParking: Flow<ParkingEntity?> = parkingDao.getActiveParking()
    val allHistory: Flow<List<ParkingEntity>> = parkingDao.getAllParkingHistory()

    suspend fun saveNewParking(parking: ParkingEntity): Long {
        // Clear any prior active parking to maintain single active parking session
        parkingDao.clearActiveParking()
        return parkingDao.insertParking(parking.copy(isActive = true))
    }

    suspend fun updateParking(parking: ParkingEntity) {
        parkingDao.updateParking(parking)
    }

    suspend fun clearActiveParking() {
        parkingDao.clearActiveParking()
    }

    suspend fun deleteParking(id: Int) {
        parkingDao.deleteById(id)
    }

    suspend fun deleteAll() {
        parkingDao.deleteAll()
    }
}
