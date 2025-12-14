package com.example.medicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medicine.data.DoseRecord
import com.example.medicine.data.Medicine
import com.example.medicine.data.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MedicineViewModel(private val repository: MedicineRepository) : ViewModel() {

    val allMedicines = repository.allMedicines

    // NOTE: The doseRecordedTrigger SharedFlow is REMOVED.

    fun insertMedicine(name: String, doses: String, startDate: Long, endDate: Long) {
        val dosesInt = doses.toIntOrNull() ?: 1

        viewModelScope.launch {
            val newMedicine = Medicine(
                name = name,
                dosesPerDay = dosesInt,
                startDate = startDate,
                endDate = endDate
            )
            repository.insert(newMedicine)
        }
    }

    // --- NEW DELETE FUNCTION ---
    /**
     * Deletes a medicine and all associated records in a background thread.
     * Deletion updates the UI automatically because allMedicine is a Flow.
     */
    fun deleteMedicine(medicineId: Int) {
        viewModelScope.launch {
            repository.deleteMedicineAndRecords(medicineId)
        }
    }
    // ---------------------------

    /**
     * Records a dose (taken or skipped). The limit is dynamically set by the medicine's dosesPerDay field.
     */
    fun recordDose(medicineId: Int, taken: Boolean) {
        viewModelScope.launch {
            // --- 1. Get Medicine Details and Daily Limit ---
            val medicine = repository.getMedicineById(medicineId)
            val dailyLimit = medicine?.dosesPerDay ?: 1

            // --- 2. Check Current Records for Today (FIXED LINE) ---
            // Use the new suspend function for the transactional limit check.
            val recordsTodayCount = repository.getRecordsTodayCountSuspend(medicineId)

            // --- 3. Enforce Limit and Insert Dose ---
            if (recordsTodayCount < dailyLimit) {
                val doseRecord = DoseRecord(medicineId = medicineId, taken = taken)
                repository.insertDoseRecord(doseRecord)
                // NOTE: Database insertion automatically updates the Flow in the UI.
            }
        }
    }

    fun getTakenDoseCount(medicineId: Int): Flow<Int> {
        return repository.getTakenDoseCount(medicineId)
    }

    fun getAllRecordedDoseCount(medicineId: Int): Flow<Int> {
        return repository.getAllRecordedDoseCount(medicineId)
    }

    /**
     * FIX: Returns a LIVE Flow of the count of recorded doses for TODAY.
     * The UI (MainActivity.kt) observes this Flow for instantaneous updates.
     */
    fun getRecordsTodayCountFlow(medicineId: Int): Flow<Int> {
        return repository.getRecordsTodayCountFlow(medicineId)
    }
}

// Minimal factory class to instantiate the ViewModel
class MedicineViewModelFactory(private val repository: MedicineRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}