package com.example.medicine.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.inOrder
import org.mockito.MockitoAnnotations
import org.mockito.Mockito.`when` as whenever
import org.mockito.kotlin.check
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import java.util.Calendar

@ExperimentalCoroutinesApi
class MedicineRepositoryTest {

    @Mock
    private lateinit var mockDao: MedicineDao

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var repository: MedicineRepository

    // Sample data
    private val testMedicine = Medicine(
        id = 1,
        name = "Test Drug",
        dosesPerDay = 2,
        startDate = 1000L,
        endDate = 2000L
    )

    private val expectedDoseCount = 5
    private val MEDICINE_ID = 1

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)


        whenever(mockDao.getAll()).thenReturn(flowOf<List<Medicine>>(emptyList()))

        repository = MedicineRepository(mockDao)
    }

    // --- 1. CRUD: INSERT TEST ---
    @Test
    fun insertMedicine_callsDaoInsert() = testScope.runTest {
        // Act
        repository.insert(testMedicine)
        // Assert
        verify(mockDao).insert(testMedicine)
    }

    // --- 2. CRUD: READ ALL TEST ---
    @Test
    fun getAllMedicines_returnsDaoFlow() = testScope.runTest {
        // Arrange
        val expectedList = listOf(testMedicine)
        whenever(mockDao.getAll()).thenReturn(flowOf(expectedList))

        repository = MedicineRepository(mockDao)

        assertThat(repository.allMedicines.first()).isEqualTo(expectedList)
    }

    // --- 3. DOSE RECORD INSERT TEST ---
    @Test
    fun recordDose_callsDaoInsertDoseRecord() = testScope.runTest {
        val takenStatus = true

        repository.insertDoseRecord(DoseRecord(medicineId = MEDICINE_ID, taken = takenStatus))

        verify(mockDao).insertDoseRecord(
            check { doseRecord ->
                assertThat(doseRecord.medicineId).isEqualTo(MEDICINE_ID)
                assertThat(doseRecord.taken).isEqualTo(takenStatus)
            }
        )
    }

    @Test
    fun getTakenDoseCount_returnsDaoFlow() = testScope.runTest {
        whenever(mockDao.getTakenDoseCount(MEDICINE_ID)).thenReturn(flowOf(expectedDoseCount))
        assertThat(repository.getTakenDoseCount(MEDICINE_ID).first()).isEqualTo(expectedDoseCount)
    }

    @Test
    fun getAllRecordedDoseCount_returnsDaoFlow() = testScope.runTest {
        whenever(mockDao.getAllRecordedDoseCount(MEDICINE_ID)).thenReturn(flowOf(expectedDoseCount))
        assertThat(repository.getAllRecordedDoseCount(MEDICINE_ID).first()).isEqualTo(expectedDoseCount)
    }

    @Test
    fun deleteMedicineAndRecords_callsDaoDeleteInCorrectOrder() = testScope.runTest {
        // Arrange
        val medicineId = 1
        val inOrder = inOrder(mockDao)

        // Act
        repository.deleteMedicineAndRecords(medicineId)

        inOrder.verify(mockDao).deleteDoseRecordsByMedicineId(medicineId)

        inOrder.verify(mockDao).deleteMedicineById(medicineId)
    }
    // --- 7. DAILY COUNT FLOW TEST (SAFE VERSION) ---
    @Test
    fun getRecordsTodayCountFlow_returnsValueFromDao() = testScope.runTest {

        // ARRANGE
        whenever(
            mockDao.getDailyDoseRecordCountFlow(
                eq(MEDICINE_ID),
                any(),
                any()
            )
        ).thenReturn(flowOf(3))

        // ACT
        val result = repository.getRecordsTodayCountFlow(MEDICINE_ID).first()

        // ASSERT
        assertThat(result).isEqualTo(3)

        verify(mockDao).getDailyDoseRecordCountFlow(
            eq(MEDICINE_ID),
            any(),
            any()
        )
    }

}