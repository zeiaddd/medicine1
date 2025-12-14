package com.example.medicine.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlinx.coroutines.flow.first // <-- NEW REQUIRED IMPORT

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

    fun getTakenDoseCount(medicineId: Int): Flow<Int> {
        return medicineDao.getTakenDoseCount(medicineId)
    }

    fun getAllRecordedDoseCount(medicineId: Int): Flow<Int> {
        return medicineDao.getAllRecordedDoseCount(medicineId)
    }

    /**
     * FIX: Returns a LIVE Flow of the count of recorded doses for TODAY.
     * This is used by the Compose UI to update instantly.
     */
    fun getRecordsTodayCountFlow(medicineId: Int): Flow<Int> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()

        // Start of today (midnight)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        // End of today (just before tomorrow)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis

        // Use the new Flow function from the DAO
        return medicineDao.getDailyDoseRecordCountFlow(medicineId, startOfDay, endOfDay)
    }

    /**
     * FIX: Helper function for the ViewModel's transactional dose limit checking.
     * It uses the Flow function but converts it to a single value using .first()
     * to work correctly within the suspend recordDose block.
     */
    suspend fun getRecordsTodayCountSuspend(medicineId: Int): Int {
        // We use first() to get the current value immediately
        return getRecordsTodayCountFlow(medicineId).first()
    }
}