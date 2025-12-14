package com.example.medicine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicine.data.AppDatabase
import com.example.medicine.data.MedicineRepository
// IMPORTS ARE SIMPLIFIED since MedicineViewModel is in this package
import com.example.medicine.ui.theme.MedicineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize DB and Repository (Dependencies)
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MedicineRepository(database.medicineDao())

        setContent {
            MedicineTheme {
                // 2. Get the ViewModel using the Factory (Lab 03)
                // Note: The factory and ViewModel are defined in this package.
                val viewModel: MedicineViewModel = viewModel(
                    factory = MedicineViewModelFactory(repository)
                )

                // 3. Render the UI (Lab 06)
                MedicineReminderScreen(viewModel)
            }
        }
    }
}

@Composable
fun MedicineReminderScreen(viewModel: MedicineViewModel) {
    // State variables for capturing user input (Compose State)
    var medicineName by remember { mutableStateOf("") }
    var dosesPerDay by remember { mutableStateOf("1") }
    val currentTime = System.currentTimeMillis()

    // Collect the data stream (Flow) from the ViewModel (Lab 05/06 integration)
    val allMedicines by viewModel.allMedicines.collectAsState(initial = emptyList())

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Add New Medicine", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            // Medicine Name Input
            OutlinedTextField(
                value = medicineName,
                onValueChange = { medicineName = it },
                label = { Text("Medicine Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Doses Per Day Input
            OutlinedTextField(
                value = dosesPerDay,
                onValueChange = { dosesPerDay = it },
                label = { Text("Doses Per Day") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (medicineName.isNotBlank()) {
                        viewModel.insertMedicine(
                            name = medicineName,
                            doses = dosesPerDay,
                            startDate = currentTime,
                            endDate = currentTime + 86400000L * 7
                        )
                        // Reset fields after saving
                        medicineName = ""
                        dosesPerDay = "1"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Medicine")
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Display Saved Medicines
            Text("Saved Medicines:", style = MaterialTheme.typography.titleMedium)

            // This is the output list, demonstrating the Flow integration
            Column {
                allMedicines.forEach { medicine ->
                    Text("-> ${medicine.name} (${medicine.dosesPerDay} times/day)", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}