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
import com.example.medicine.ui.theme.MedicineTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.navigation.compose.NavHost // Required for Navigation
import androidx.navigation.compose.composable // Required for Navigation
import androidx.navigation.compose.rememberNavController // Required for Navigation

// --- NAVIGATION DESTINATIONS ---
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
                // Initialize the NavController
                val navController = rememberNavController()

                // Get the ViewModel (it is shared across all screens via NavHost)
                val viewModel: MedicineViewModel = viewModel(
                    factory = MedicineViewModelFactory(repository)
                )

                // The NavHost defines the possible screens and the starting screen
                NavHost(
                    navController = navController,
                    startDestination = Destinations.HOME_SCREEN
                ) {
                    // 1. Home Screen (Adding Medicines)
                    composable(Destinations.HOME_SCREEN) {
                        MedicineReminderScreen(
                            viewModel = viewModel,
                            onNavigateToDetails = {
                                navController.navigate(Destinations.DETAILS_SCREEN)
                            }
                        )
                    }

                    // 2. Details Screen (Reporting/List)
                    composable(Destinations.DETAILS_SCREEN) {
                        MedicineDetailsScreen(
                            viewModel = viewModel,
                            onNavigateToHome = {
                                navController.popBackStack() // Go back to the previous screen
                            }
                        )
                    }
                }
            }
        }
    }
}

// Utility function to convert milliseconds to a human-readable date string (for display)
fun Long.toDateString(): String {
    return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(this))
}

// Utility function to convert date string (DD/MM/YYYY) to milliseconds (for saving)
fun stringToMillis(dateString: String): Long? {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return try {
        // Clear time components for consistent date comparison (start of the day)
        val date = format.parse(dateString)
        date?.time?.let {
            // Optional: Ensure time is set to midnight (start of day) for consistency
            // This is complex, so we'll rely on the SimpleDateFormat default behavior for now
            it
        }
    } catch (e: Exception) {
        null
    }
}

// =========================================================================
//                  SCREEN 1: MEDICINE INPUT/HOME SCREEN
// =========================================================================

@Composable
fun MedicineReminderScreen(
    viewModel: MedicineViewModel,
    onNavigateToDetails: () -> Unit // <-- NEW NAVIGATION CALLBACK
) {
    // State variables for capturing user input
    var medicineName by remember { mutableStateOf("") }
    var dosesPerDay by remember { mutableStateOf("1") }

    val todayString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    var startDateString by remember { mutableStateOf(todayString) }

    val defaultEndDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
    var endDateString by remember { mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(defaultEndDate))) }

    val allMedicines by viewModel.allMedicines.collectAsState(initial = emptyList())

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Add New Medicine", style = MaterialTheme.typography.headlineMedium)
                // --- NAVIGATION BUTTON ---
                Button(onClick = onNavigateToDetails) {
                    Text("View Reports")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ... (Input Fields for Name, Doses, Start Date, End Date) - Use the same code you verified
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

                    if (medicineName.isNotBlank() && startMillis != null && endMillis != null && endMillis > startMillis) {
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

            // Display Saved Medicines
            Text("Saved Medicines:", style = MaterialTheme.typography.titleMedium)

            Column {
                allMedicines.forEach { medicine ->
                    Text(
                        "-> ${medicine.name} (${medicine.dosesPerDay} times/day). Starts: ${medicine.startDate.toDateString()}",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}


// =========================================================================
//                  SCREEN 2: MEDICINE DETAILS / REPORT SCREEN
// =========================================================================

@Composable
fun MedicineDetailsScreen(
    viewModel: MedicineViewModel,
    onNavigateToHome: () -> Unit // <-- NAVIGATION CALLBACK
) {
    // Collect the full list of medicines
    val allMedicines by viewModel.allMedicines.collectAsState(initial = emptyList())

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Medicine Usage Report", style = MaterialTheme.typography.headlineMedium)
                // --- NAVIGATION BUTTON ---
                Button(onClick = onNavigateToHome) {
                    Text("Add Medicine")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (allMedicines.isEmpty()) {
                Text("No medicines saved yet.", style = MaterialTheme.typography.bodyLarge)
            } else {
                // Display the detailed report for each medicine
                allMedicines.forEach { medicine ->
                    val totalDays = (medicine.endDate - medicine.startDate) / TimeUnit.DAYS.toMillis(1)
                    val totalDoses = totalDays * medicine.dosesPerDay

                    // --- MOCKED/SIMULATED DATA for Doses Taken ---
                    // Since we don't have a tracking mechanism, we mock the 'doses taken'
                    // to demonstrate the percentage calculation. We assume 50% completion.
                    val dosesTaken = (totalDoses / 2).toInt()
                    val commitmentPercentage = if (totalDoses > 0) (dosesTaken.toFloat() / totalDoses) * 100 else 0f
                    // ------------------------------------------

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(medicine.name, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(4.dp))

                            // Basic Details
                            Text("Doses per day: ${medicine.dosesPerDay}")
                            Text("Duration: ${medicine.startDate.toDateString()} to ${medicine.endDate.toDateString()}")
                            Spacer(modifier = Modifier.height(8.dp))

                            // Commitment Report
                            Text("Total Doses Required: ${totalDoses.toInt()}", style = MaterialTheme.typography.bodyMedium)
                            Text("Doses Taken (Simulated): $dosesTaken", style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(8.dp))

                            // Percentage Display
                            Text(
                                "Commitment: ${String.format("%.1f", commitmentPercentage)}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}