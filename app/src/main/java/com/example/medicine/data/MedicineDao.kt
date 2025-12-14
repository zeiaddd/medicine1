package com.example.medicine.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medicine: Medicine)

    @Query("SELECT * FROM medicines ORDER BY name ASC")
    fun getAll(): Flow<List<Medicine>>

    // --- DOSE RECORDING ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseRecord(doseRecord: DoseRecord)

    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    suspend fun getMedicineById(medicineId: Int): Medicine?

    // --- FLOW FUNCTIONS FOR LIVE UI UPDATES ---

    @Query("SELECT COUNT(id) FROM dose_record WHERE medicine_id = :medicineId AND taken = 1")
    fun getTakenDoseCount(medicineId: Int): Flow<Int>

    @Query("SELECT COUNT(id) FROM dose_record WHERE medicine_id = :medicineId")
    fun getAllRecordedDoseCount(medicineId: Int): Flow<Int>

    /**
     * FIX: Queries the number of ALL recorded doses for the day, returning a Flow.
     * This is used by the UI (MainActivity.kt) for instantaneous count updates.
     */
    @Query("SELECT COUNT(id) FROM dose_record WHERE medicine_id = :medicineId AND timestamp >= :startOfDayMillis AND timestamp < :endOfDayMillis")
    fun getDailyDoseRecordCountFlow(medicineId: Int, startOfDayMillis: Long, endOfDayMillis: Long): Flow<Int>
}