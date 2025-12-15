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


    fun deleteMedicine(medicineId: Int) {
        viewModelScope.launch {
            repository.deleteMedicineAndRecords(medicineId)
        }
    }
    // ---------------------------


    fun recordDose(medicineId: Int, taken: Boolean) {
        viewModelScope.launch {

            val medicine = repository.getMedicineById(medicineId)
            val dailyLimit = medicine?.dosesPerDay ?: 1


            val recordsTodayCount = repository.getRecordsTodayCountSuspend(medicineId)


            if (recordsTodayCount < dailyLimit) {
                val doseRecord = DoseRecord(medicineId = medicineId, taken = taken)
                repository.insertDoseRecord(doseRecord)

            }
        }
    }

    fun getTakenDoseCount(medicineId: Int): Flow<Int> {
        return repository.getTakenDoseCount(medicineId)
    }

    fun getAllRecordedDoseCount(medicineId: Int): Flow<Int> {
        return repository.getAllRecordedDoseCount(medicineId)
    }


    fun getRecordsTodayCountFlow(medicineId: Int): Flow<Int> {
        return repository.getRecordsTodayCountFlow(medicineId)
    }
}


class MedicineViewModelFactory(private val repository: MedicineRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}