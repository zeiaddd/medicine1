package com.example.medicine.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. UPDATED ENTITY LIST: Added DoseRecord::class.
// 2. UPDATED VERSION: Incremented version from 1 to 2 because the schema changed.
@Database(entities = [Medicine::class, DoseRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medicine_database"
                )
                    // 3. ADDED MIGRATION STRATEGY: Required when you change the version number.
                    // This tells Room to destroy and recreate the tables instead of crashing.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}