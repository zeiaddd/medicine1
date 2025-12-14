package com.example.medicine.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents a single instance of a dose being taken or skipped.
 */
@Entity(
    tableName = "dose_record",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicine_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DoseRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // The foreign key linking this record to a specific medicine
    @ColumnInfo(name = "medicine_id") val medicineId: Int,

    // Timestamp of when the user recorded the dose (usually 'now')
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),

    // True if taken, False if skipped/missed (for commitment tracking)
    @ColumnInfo(name = "taken") val taken: Boolean
)