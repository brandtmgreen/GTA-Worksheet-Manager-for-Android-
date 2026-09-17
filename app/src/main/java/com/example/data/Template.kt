package com.example.data

data class TemplateField(
    val key: String,
    val label: String,
    val type: String = "text", // "text", "number", "currency", "date", "multiline", "phone", "email"
    val defaultValue: String = "",
    val placeholder: String = "",
    val aliases: List<String> = emptyList() // Used by OCR mapping to match detected keys
)

data class Template(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val defaultTitlePrefix: String,
    val fields: List<TemplateField>
)
