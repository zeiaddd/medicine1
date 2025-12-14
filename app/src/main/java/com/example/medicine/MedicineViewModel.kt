package com.example.medicine // <--- TOP LEVEL PACKAGE

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medicine.data.Medicine
import com.example.medicine.data.MedicineRepository
import kotlinx.coroutines.launch

// This ViewModel handles the business logic and lifecycle scope (Lab 03 & 05)
class MedicineViewModel(private val repository: MedicineRepository) : ViewModel() {

    // Expose the list of medicines as a Flow from the Repository to the UI
    val allMedicines = repository.allMedicines

    /**
     * Inserts new medicine data into the repository.
     */
    fun insertMedicine(name: String, doses: String, startDate: Long, endDate: Long) {
        val dosesInt = doses.toIntOrNull() ?: 1

        // Use Coroutine Scope for database write operation (Lab 05 requirement)
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
}

// Minimal factory class to instantiate the ViewModel with the repository dependency (Best Practice)
class MedicineViewModelFactory(private val repository: MedicineRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicineViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}