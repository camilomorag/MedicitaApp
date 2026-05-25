package com.example.medicitaapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MedicineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medicines: List<MedicineEntity>)

    @Query("SELECT * FROM medicines ORDER BY producto ASC")
    suspend fun getAllMedicines(): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE producto LIKE '%' || :query || '%' ORDER BY producto ASC")
    suspend fun searchMedicines(query: String): List<MedicineEntity>

    @Query("SELECT * FROM medicines WHERE id = :medicineId LIMIT 1")
    suspend fun getMedicineById(medicineId: Int): MedicineEntity?

    @Query("DELETE FROM medicines")
    suspend fun deleteAll()
}