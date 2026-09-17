package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Worksheet::class], version = 1, exportSchema = false)
abstract class WorksheetDatabase : RoomDatabase() {
    abstract fun worksheetDao(): WorksheetDao

    companion object {
        @Volatile
        private var INSTANCE: WorksheetDatabase? = null

        fun getDatabase(context: Context): WorksheetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorksheetDatabase::class.java,
                    "worksheet_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
