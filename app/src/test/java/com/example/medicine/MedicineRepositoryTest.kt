package com.example.medicine.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class MedicineRepositoryTest {

    @Mock
    private lateinit var mockDao: MedicineDao

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var repository: MedicineRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // Fix: Stub the DAO to return an empty Flow.
        // If we don't do this, mockDao.getAllMedicines() returns null,
        // causing the repository.allMedicines property to be null.
        `when`(mockDao.getAllMedicines()).thenReturn(flowOf(emptyList()))

        repository = MedicineRepository(mockDao)
    }

    // Test Case: Verify that the Repository correctly delegates the insert call to the DAO
    @Test
    fun insert_callsDaoInsert() = testScope.runTest {
        // Arrange
        val medicine = Medicine(
            name = "Test Drug",
            dosesPerDay = 1,
            startDate = 1000L,
            endDate = 2000L
        )

        // Act
        repository.insert(medicine)

        // Assert (Verification using Mockito)
        verify(mockDao).insertMedicine(medicine)

        // Basic check for data retrieval property
        assertThat(repository.allMedicines).isNotNull()
    }
}
