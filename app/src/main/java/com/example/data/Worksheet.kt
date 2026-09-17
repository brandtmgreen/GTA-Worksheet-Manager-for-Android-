package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "worksheets")
data class Worksheet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val templateId: String = "custom",
    val templateName: String = "General Worksheet",
    val fieldsJson: String = "{}",
    val extractedText: String = "",
    val imageUri: String? = null,
    val status: String = "Draft", // Draft, Completed, Exported, Synced
    val clientName: String = "",
    val totalAmount: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
