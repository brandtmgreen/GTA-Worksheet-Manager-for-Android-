package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.ContactsManager
import com.example.util.DeviceContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

sealed class OcrUiState {
    object Idle : OcrUiState()
    data class Processing(val stepMessage: String) : OcrUiState()
    data class Success(val result: OcrResult, val imageUri: Uri? = null, val bitmap: Bitmap? = null) : OcrUiState()
    data class Error(val message: String) : OcrUiState()
}

sealed class WebhookSyncState {
    object Idle : WebhookSyncState()
    object Syncing : WebhookSyncState()
    data class Success(val message: String) : WebhookSyncState()
    data class Error(val message: String) : WebhookSyncState()
}

class WorksheetViewModel(
    application: Application,
    private val settingsManager: SettingsManager
) : AndroidViewModel(application) {

    private val db = WorksheetDatabase.getDatabase(application)
    private val dao = db.worksheetDao()
    private val ocrService = OcrService()
    val exportManager = ExportManager(application)

    val worksheets: StateFlow<List<Worksheet>> = dao.getAllWorksheets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ocrState = MutableStateFlow<OcrUiState>(OcrUiState.Idle)
    val ocrState: StateFlow<OcrUiState> = _ocrState.asStateFlow()

    private val _webhookState = MutableStateFlow<WebhookSyncState>(WebhookSyncState.Idle)
    val webhookState: StateFlow<WebhookSyncState> = _webhookState.asStateFlow()

    // Active Worksheet in Editor
    private val _currentWorksheet = MutableStateFlow<Worksheet?>(null)
    val currentWorksheet: StateFlow<Worksheet?> = _currentWorksheet.asStateFlow()

    // Form fields state during editing
    private val _formFields = MutableStateFlow<Map<String, String>>(emptyMap())
    val formFields: StateFlow<Map<String, String>> = _formFields.asStateFlow()

    private val _selectedTemplate = MutableStateFlow<Template>(TemplateRepository.templates.first())
    val selectedTemplate: StateFlow<Template> = _selectedTemplate.asStateFlow()

    fun resetOcrState() {
        _ocrState.value = OcrUiState.Idle
    }

    fun resetWebhookState() {
        _webhookState.value = WebhookSyncState.Idle
    }

    fun startNewWorksheet(template: Template) {
        _selectedTemplate.value = template
        val initialMap = mutableMapOf<String, String>()
        template.fields.forEach { field ->
            initialMap[field.key] = field.defaultValue
        }
        _formFields.value = initialMap
        _currentWorksheet.value = Worksheet(
            title = "${template.defaultTitlePrefix} - ${System.currentTimeMillis() % 10000}",
            templateId = template.id,
            templateName = template.title,
            fieldsJson = JSONObject(initialMap as Map<*, *>).toString()
        )
    }

    fun loadWorksheetForEditing(worksheet: Worksheet) {
        _currentWorksheet.value = worksheet
        val template = TemplateRepository.templates.find { it.id == worksheet.templateId }
            ?: TemplateRepository.templates.first()
        _selectedTemplate.value = template

        val map = mutableMapOf<String, String>()
        try {
            val json = JSONObject(worksheet.fieldsJson)
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = json.optString(k)
            }
        } catch (e: Exception) {
            // Keep empty
        }
        _formFields.value = map
    }

    fun updateFormField(key: String, value: String) {
        val updated = _formFields.value.toMutableMap()
        updated[key] = value
        _formFields.value = updated
    }

    fun pasteTextIntoField(key: String, text: String, append: Boolean = false) {
        val updated = _formFields.value.toMutableMap()
        val existing = updated[key] ?: ""
        updated[key] = if (append && existing.isNotBlank()) "$existing\n$text" else text
        _formFields.value = updated
    }

    fun importContact(contact: DeviceContact) {
        val currentFields = _formFields.value.toMutableMap()
        val template = _selectedTemplate.value

        val nameKey = findMatchingFieldKey(template, listOf("client_name", "client", "customer_name", "bill_to", "property_owner", "inspector_name", "applicator_name", "operator_name")) ?: "client_name"
        val phoneKey = findMatchingFieldKey(template, listOf("client_phone", "phone", "contact_phone", "tel", "cell", "mobile")) ?: "client_phone"
        val emailKey = findMatchingFieldKey(template, listOf("client_email", "email", "billing_email", "mail")) ?: "client_email"
        val addressKey = findMatchingFieldKey(template, listOf("property_address", "site_address", "site_location", "address", "location"))

        if (contact.name.isNotBlank()) {
            currentFields[nameKey] = contact.name
        }
        if (contact.phone.isNotBlank()) {
            currentFields[phoneKey] = contact.phone
        }
        if (contact.email.isNotBlank()) {
            currentFields[emailKey] = contact.email
        }
        if (contact.address.isNotBlank() && addressKey != null) {
            currentFields[addressKey] = contact.address
        }

        _formFields.value = currentFields

        val cur = _currentWorksheet.value
        if (cur != null) {
            val updatedTitle = if (cur.clientName.isBlank() || cur.title.startsWith(template.defaultTitlePrefix)) {
                if (contact.name.isNotBlank()) "${template.defaultTitlePrefix} - ${contact.name}" else cur.title
            } else {
                cur.title
            }
            _currentWorksheet.value = cur.copy(
                clientName = contact.name.ifBlank { cur.clientName },
                title = updatedTitle,
                fieldsJson = JSONObject(currentFields as Map<*, *>).toString()
            )
        }
    }

    fun importContactFromUri(context: android.content.Context, uri: Uri, onComplete: (DeviceContact?) -> Unit = {}) {
        viewModelScope.launch {
            val contact = ContactsManager.getContactFromUri(context, uri)
            if (contact != null) {
                importContact(contact)
            }
            onComplete(contact)
        }
    }

    fun pasteAndAutoDetectContact(text: String): DeviceContact {
        val contact = ContactsManager.parseContactFromText(text)
        if (contact.name.isNotBlank() || contact.phone.isNotBlank() || contact.email.isNotBlank() || contact.address.isNotBlank()) {
            importContact(contact)
        }
        return contact
    }

    private fun findMatchingFieldKey(template: Template, candidates: List<String>): String? {
        for (c in candidates) {
            if (template.fields.any { it.key == c }) return c
        }
        for (field in template.fields) {
            for (c in candidates) {
                if (field.aliases.contains(c) || field.label.contains(c, ignoreCase = true)) {
                    return field.key
                }
            }
        }
        return null
    }

    fun updateCurrentWorksheetMetadata(
        title: String,
        status: String,
        notes: String
    ) {
        val cur = _currentWorksheet.value ?: return
        val fields = _formFields.value
        val client = fields["client_name"] ?: cur.clientName
        val totalStr = fields["amount_total"] ?: fields["total"] ?: "0.0"
        val total = totalStr.replace("$", "").replace(",", "").trim().toDoubleOrNull() ?: cur.totalAmount

        _currentWorksheet.value = cur.copy(
            title = title,
            status = status,
            notes = notes,
            clientName = client,
            totalAmount = total,
            fieldsJson = JSONObject(fields as Map<*, *>).toString()
        )
    }

    fun saveCurrentWorksheet(onSaved: (Worksheet) -> Unit = {}) {
        viewModelScope.launch {
            val cur = _currentWorksheet.value ?: return@launch
            val fields = _formFields.value
            val client = fields["client_name"] ?: cur.clientName
            val totalStr = fields["amount_total"] ?: fields["total"] ?: "0.0"
            val total = totalStr.replace("$", "").replace(",", "").trim().toDoubleOrNull() ?: cur.totalAmount

            val updated = cur.copy(
                clientName = client,
                totalAmount = total,
                fieldsJson = JSONObject(fields as Map<*, *>).toString(),
                timestamp = System.currentTimeMillis()
            )

            val id = if (updated.id == 0) {
                dao.insertWorksheet(updated).toInt()
            } else {
                dao.updateWorksheet(updated)
                updated.id
            }

            val finalSheet = updated.copy(id = id)
            _currentWorksheet.value = finalSheet
            onSaved(finalSheet)
        }
    }

    fun deleteWorksheet(worksheet: Worksheet) {
        viewModelScope.launch {
            dao.deleteWorksheet(worksheet)
        }
    }

    // OCR Scanning with Image from Uri
    fun processImageUri(uri: Uri, targetTemplateId: String? = null) {
        viewModelScope.launch {
            _ocrState.value = OcrUiState.Processing("Reading scanned image...")
            val bitmap = loadBitmapFromUri(uri)
            if (bitmap == null) {
                _ocrState.value = OcrUiState.Error("Could not load image file.")
                return@launch
            }

            _ocrState.value = OcrUiState.Processing("Running OCR & field extraction...")
            val customKey = settingsManager.apiKeyFlow.firstOrNull()
            val result = ocrService.processImage(bitmap, customKey, targetTemplateId)

            if (result.success) {
                _ocrState.value = OcrUiState.Success(result, imageUri = uri, bitmap = bitmap)
            } else {
                _ocrState.value = OcrUiState.Error(result.errorMessage ?: "OCR processing failed.")
            }
        }
    }

    // OCR Scanning using Sample Document
    fun processSampleDocument(sample: SampleDocument) {
        viewModelScope.launch {
            _ocrState.value = OcrUiState.Processing("Analyzing sample document layout...")
            kotlinx.coroutines.delay(600)
            _ocrState.value = OcrUiState.Processing("Extracting text and identifying fields...")
            kotlinx.coroutines.delay(500)

            val result = OcrResult(
                success = true,
                documentType = sample.templateId,
                documentTitle = sample.title,
                extractedText = sample.rawOcrText,
                extractedFields = sample.extractedFields,
                clientName = sample.clientName,
                totalAmount = sample.totalAmount,
                sourceEngine = "GreenTree High-Fidelity OCR Engine"
            )
            _ocrState.value = OcrUiState.Success(result)
        }
    }

    // Auto-populating text (e.g. from pasted Contact, Email, Text message, or OCR transcript)
    fun parseAndPopulateText(rawText: String, targetTemplateId: String? = null) {
        viewModelScope.launch {
            _ocrState.value = OcrUiState.Processing("Parsing text structure and form fields...")
            val result = ocrService.parseTextToForm(rawText, targetTemplateId)
            _ocrState.value = OcrUiState.Success(result)
        }
    }

    // Apply OCR Result to form and create/update worksheet
    fun applyOcrResultToWorksheet(result: OcrResult, imageUriString: String? = null) {
        val template = TemplateRepository.getTemplateById(result.documentType)
        _selectedTemplate.value = template

        val fieldsMap = mutableMapOf<String, String>()
        // Initialize template defaults
        template.fields.forEach { f ->
            fieldsMap[f.key] = f.defaultValue
        }
        // Overlay extracted OCR fields
        result.extractedFields.forEach { (k, v) ->
            if (v.isNotBlank()) {
                fieldsMap[k] = v
            }
        }

        _formFields.value = fieldsMap

        _currentWorksheet.value = Worksheet(
            id = 0,
            title = result.documentTitle.ifEmpty { "${template.defaultTitlePrefix} (Scanned)" },
            templateId = template.id,
            templateName = template.title,
            fieldsJson = JSONObject(fieldsMap as Map<*, *>).toString(),
            extractedText = result.extractedText,
            imageUri = imageUriString,
            status = "Completed",
            clientName = result.clientName,
            totalAmount = result.totalAmount,
            notes = "Auto-extracted via ${result.sourceEngine} on ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())}"
        )
    }

    // Push to CRM / Webhook
    fun pushCurrentToWebhook(worksheet: Worksheet) {
        viewModelScope.launch {
            _webhookState.value = WebhookSyncState.Syncing
            val webhookUrl = settingsManager.webhookUrlFlow.firstOrNull() ?: ""
            val apiKey = settingsManager.apiKeyFlow.firstOrNull() ?: ""
            val companyName = settingsManager.companyNameFlow.firstOrNull() ?: "GreenTree Arboriculture"

            if (webhookUrl.isBlank()) {
                _webhookState.value = WebhookSyncState.Error("Webhook URL is not configured. Please set it in Settings.")
                return@launch
            }

            val result = exportManager.pushToWebhook(webhookUrl, apiKey, worksheet, companyName)
            if (result.isSuccess) {
                val successMsg = result.getOrNull() ?: "Synced successfully!"
                _webhookState.value = WebhookSyncState.Success(successMsg)
                // Mark worksheet as Synced in DB
                val updated = worksheet.copy(status = "Synced")
                dao.updateWorksheet(updated)
                _currentWorksheet.value = updated
            } else {
                _webhookState.value = WebhookSyncState.Error(result.exceptionOrNull()?.message ?: "Sync failed.")
            }
        }
    }

    private suspend fun loadBitmapFromUri(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
}
