package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorksheetDao {
    @Query("SELECT * FROM worksheets ORDER BY timestamp DESC")
    fun getAllWorksheets(): Flow<List<Worksheet>>

    @Query("SELECT * FROM worksheets WHERE id = :id")
    suspend fun getWorksheetById(id: Int): Worksheet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorksheet(worksheet: Worksheet): Long

    @Update
    suspend fun updateWorksheet(worksheet: Worksheet)

    @Delete
    suspend fun deleteWorksheet(worksheet: Worksheet)

    @Query("DELETE FROM worksheets WHERE id = :id")
    suspend fun deleteById(id: Int)
}
