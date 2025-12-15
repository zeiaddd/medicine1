package com.example.medicine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medicine.data.AppDatabase
import com.example.medicine.data.Medicine
import com.example.medicine.data.MedicineRepository
import com.example.medicine.ui.theme.MedicineTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.Calendar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

private object Destinations {
    const val HOME_SCREEN = "home"
    const val DETAILS_SCREEN = "details"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MedicineRepository(database.medicineDao())

        setContent {
            MedicineTheme {
                val navController = rememberNavController()

                // Get the ViewModel
                val viewModel: MedicineViewModel = viewModel(
                    factory = MedicineViewModelFactory(repository)
                )

                NavHost(
                    navController = navController,
                    startDestination = Destinations.HOME_SCREEN
                ) {
                    composable(Destinations.HOME_SCREEN) {
                        MedicineReminderScreen(
                            viewModel = viewModel,
                            onNavigateToDetails = {
                                navController.navigate(Destinations.DETAILS_SCREEN)
                            }
                        )
                    }

                    composable(Destinations.DETAILS_SCREEN) {
                        MedicineDetailsScreen(
                            viewModel = viewModel,
                            onNavigateToHome = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}


fun Long.toDateString(): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(this))
}


fun stringToMillis(dateString: String): Long? {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        val date = format.parse(dateString)

        Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    } catch (e: Exception) {
        null
    }
}

// =========================================================================
//                   SCREEN 1: MEDICINE INPUT/HOME SCREEN
// =========================================================================

@Composable
fun MedicineReminderScreen(
    viewModel: MedicineViewModel,
    onNavigateToDetails: () -> Unit
) {
    var medicineName by remember { mutableStateOf("") }
    var dosesPerDay by remember { mutableStateOf("1") }

    val todayString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    var startDateString by remember { mutableStateOf(todayString) }

    val defaultEndDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
    var endDateString by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(defaultEndDate))) }

    val allMedicines by viewModel.allMedicines.collectAsState(initial = emptyList())

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header and View Reports Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Add New Medicine", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onNavigateToDetails) {
                    Text("View Reports")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Input Fields
            OutlinedTextField(value = medicineName, onValueChange = { medicineName = it }, label = { Text("Medicine Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = dosesPerDay, onValueChange = { dosesPerDay = it }, label = { Text("Doses Per Day") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = startDateString, onValueChange = { startDateString = it }, label = { Text("Start Date (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = endDateString, onValueChange = { endDateString = it }, label = { Text("End Date (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val startMillis = stringToMillis(startDateString)
                    val endMillis = stringToMillis(endDateString)

                    if (medicineName.isNotBlank() && startMillis != null && endMillis != null && endMillis >= startMillis) {
                        viewModel.insertMedicine(
                            name = medicineName,
                            doses = dosesPerDay,
                            startDate = startMillis,
                            endDate = endMillis
                        )
                        // Reset fields
                        medicineName = ""
                        dosesPerDay = "1"
                        startDateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                        endDateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Medicine")
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Display Saved Medicines (Now Scrollable via LazyColumn)
            Text("Saved Medicines:", style = MaterialTheme.typography.titleMedium)

            // START OF FIX: Using LazyColumn to make the list scrollable
            LazyColumn(
                modifier = Modifier.fillMaxSize(), // Allow the LazyColumn to take remaining height
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                items(allMedicines) { medicine ->
                    Text(
                        "-> ${medicine.name} (${medicine.dosesPerDay} times/day). Starts: ${medicine.startDate.toDateString()}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }

        }
    }
}


// =========================================================================
//                   DOSE TRACKING CARD
// =========================================================================

@Composable
fun DoseTrackerCard(medicine: Medicine, viewModel: MedicineViewModel) {

    val dosesTaken by viewModel.getTakenDoseCount(medicine.id).collectAsState(initial = 0)
    val allRecordedDoses by viewModel.getAllRecordedDoseCount(medicine.id).collectAsState(initial = 0)
    val recordsToday by viewModel.getRecordsTodayCountFlow(medicine.id).collectAsState(initial = 0)

    val totalDays = ((medicine.endDate - medicine.startDate) / TimeUnit.DAYS.toMillis(1)) + 1
    val totalDosesRequired = totalDays * medicine.dosesPerDay

    val isCourseFinished = System.currentTimeMillis() > medicine.endDate
    val allSlotsRecorded = allRecordedDoses >= totalDosesRequired
    val dailyLimitReached = recordsToday >= medicine.dosesPerDay

    val commitmentPercentage = if (totalDosesRequired > 0) (dosesTaken.toFloat() / totalDosesRequired) * 100 else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(medicine.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))


            Text("Doses per day: ${medicine.dosesPerDay}")
            Text("Duration: ${medicine.startDate.toDateString()} to ${medicine.endDate.toDateString()}")
            Spacer(modifier = Modifier.height(8.dp))


            Text("Total Doses Required: ${totalDosesRequired.toInt()}", style = MaterialTheme.typography.bodyMedium)
            Text("Doses Taken (Committed): $dosesTaken", style = MaterialTheme.typography.bodyMedium)
            Text("Doses Skipped (Recorded): ${allRecordedDoses - dosesTaken}", style = MaterialTheme.typography.bodyMedium)


            Text(
                "Commitment: ${String.format("%.1f", commitmentPercentage)}%",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.deleteMedicine(medicine.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
            ) {
                Text("DELETE MEDICINE & HISTORY", color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer after the new button

            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isCourseFinished -> {
                    Text("✅ Treatment finished on ${medicine.endDate.toDateString()}.",
                        color = Color.Gray, style = MaterialTheme.typography.titleSmall)
                }
                allSlotsRecorded -> {
                    Text("🛑 All ${totalDosesRequired.toInt()} doses recorded!",
                        color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall)
                }
                dailyLimitReached -> {
                    Text("😴 Daily dose limit (${medicine.dosesPerDay}) reached for today.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleSmall)
                }
                else -> {

                    Text("Today: ${recordsToday} of ${medicine.dosesPerDay} doses recorded.", style = MaterialTheme.typography.titleSmall)
                    Text("Remaining today: ${medicine.dosesPerDay - recordsToday}", style = MaterialTheme.typography.bodySmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        Button(
                            onClick = { viewModel.recordDose(medicine.id, taken = true) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        ) {
                            Text("TAKEN")
                        }

                        Button(
                            onClick = { viewModel.recordDose(medicine.id, taken = false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        ) {
                            Text("SKIPPED")
                        }
                    }
                }
            }
        }
    }
}


// =========================================================================
//                   SCREEN 2: MEDICINE DETAILS / REPORT SCREEN
// =========================================================================

@Composable
fun MedicineDetailsScreen(
    viewModel: MedicineViewModel,
    onNavigateToHome: () -> Unit
) {
    val allMedicines by viewModel.allMedicines.collectAsState(initial = emptyList())

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Medicine Usage Report", style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onNavigateToHome) {
                    Text("Add Medicine")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (allMedicines.isEmpty()) {
                Text("No medicines saved yet.", style = MaterialTheme.typography.bodyLarge)
            } else {
                // LazyColumn for scrolling efficiency (already here, no change needed)
                LazyColumn {
                    items(allMedicines) { medicine ->
                        DoseTrackerCard(medicine = medicine, viewModel = viewModel)
                    }
                }
            }
        }
    }
}