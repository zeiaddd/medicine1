package com.example.medicine.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlinx.coroutines.flow.first

class MedicineRepository(private val medicineDao: MedicineDao) {

    val allMedicines: Flow<List<Medicine>> = medicineDao.getAll()

    suspend fun insert(medicine: Medicine) {
        medicineDao.insert(medicine)
    }

    suspend fun insertDoseRecord(doseRecord: DoseRecord) {
        medicineDao.insertDoseRecord(doseRecord)
    }

    suspend fun getMedicineById(medicineId: Int): Medicine? {
        return medicineDao.getMedicineById(medicineId)
    }

    suspend fun deleteMedicineAndRecords(medicineId: Int) {
        medicineDao.deleteDoseRecordsByMedicineId(medicineId)
        medicineDao.deleteMedicineById(medicineId)
    }
    // ---------------------------

    fun getTakenDoseCount(medicineId: Int): Flow<Int> {
        return medicineDao.getTakenDoseCount(medicineId)
    }

    fun getAllRecordedDoseCount(medicineId: Int): Flow<Int> {
        return medicineDao.getAllRecordedDoseCount(medicineId)
    }


    fun getRecordsTodayCountFlow(medicineId: Int): Flow<Int> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis

        return medicineDao.getDailyDoseRecordCountFlow(medicineId, startOfDay, endOfDay)
    }

    suspend fun getRecordsTodayCountSuspend(medicineId: Int): Int {
        return getRecordsTodayCountFlow(medicineId).first()
    }
}