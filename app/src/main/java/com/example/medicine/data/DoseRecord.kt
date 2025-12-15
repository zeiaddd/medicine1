package com.example.medicine.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


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


    @ColumnInfo(name = "medicine_id") val medicineId: Int,


    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis(),


    @ColumnInfo(name = "taken") val taken: Boolean
)