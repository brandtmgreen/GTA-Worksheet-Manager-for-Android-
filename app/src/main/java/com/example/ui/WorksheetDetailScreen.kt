package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.SettingsViewModel
import com.example.WebhookSyncState
import com.example.WorksheetViewModel
import com.example.data.Worksheet
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetDetailScreen(
    worksheetId: Int,
    viewModel: WorksheetViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Worksheet) -> Unit
) {
    val worksheets by viewModel.worksheets.collectAsState()
    val worksheet = worksheets.find { it.id == worksheetId } ?: viewModel.currentWorksheet.collectAsState().value
    val companyName by settingsViewModel.companyName.collectAsState()
    val companyAddress by settingsViewModel.companyAddress.collectAsState()
    val companyPhone by settingsViewModel.companyPhone.collectAsState()
    val companyEmail by settingsViewModel.companyEmail.collectAsState()
    val companyWebsite by settingsViewModel.companyWebsite.collectAsState()
    val googleReviewUrl by settingsViewModel.googleReviewUrl.collectAsState()
    val webhookUrl by settingsViewModel.webhookUrl.collectAsState()
    val webhookState by viewModel.webhookState.collectAsState()

    var showExportPreviewDialog by remember { mutableStateOf<String?>(null) } // "estimate", "sms", "email", "json", "csv"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(worksheet?.title ?: "Worksheet Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(worksheet?.templateName ?: "Worksheet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (worksheet != null) {
                        IconButton(onClick = { onNavigateToEdit(worksheet) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        if (worksheet == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Worksheet not found.")
            }
            return@Scaffold
        }

        val fieldsMap = remember(worksheet.fieldsJson) {
            val map = mutableMapOf<String, String>()
            try {
                val json = JSONObject(worksheet.fieldsJson)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = json.optString(k)
                }
            } catch (e: Exception) {
                // empty
            }
            map
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Official GreenTree Estimate Document & QR Code View
                EstimateSheetCard(
                    worksheet = worksheet,
                    fieldsMap = fieldsMap,
                    companyName = companyName,
                    companyAddress = companyAddress,
                    companyWebsite = companyWebsite,
                    companyEmail = companyEmail,
                    companyPhone = companyPhone,
                    googleReviewUrl = googleReviewUrl,
                    onCopyEstimate = {
                        val estText = viewModel.exportManager.formatEstimateExport(
                            worksheet = worksheet,
                            companyName = companyName,
                            companyAddress = companyAddress,
                            companyWebsite = companyWebsite,
                            companyEmail = companyEmail,
                            companyPhone = companyPhone,
                            googleReviewUrl = googleReviewUrl
                        )
                        viewModel.exportManager.copyToClipboard("Estimate Sheet", estText)
                    },
                    onShareSms = {
                        val text = viewModel.exportManager.formatForSms(
                            worksheet = worksheet,
                            companyName = companyName,
                            googleReviewUrl = googleReviewUrl
                        )
                        val phone = fieldsMap["client_phone"] ?: fieldsMap["phone"] ?: ""
                        viewModel.exportManager.shareViaSms(phone, text)
                    },
                    onShareEmail = {
                        val (subject, body) = viewModel.exportManager.formatForEmail(
                            worksheet = worksheet,
                            companyName = companyName,
                            companyAddress = companyAddress,
                            companyPhone = companyPhone,
                            companyWebsite = companyWebsite,
                            googleReviewUrl = googleReviewUrl
                        )
                        val email = fieldsMap["client_email"] ?: fieldsMap["email"] ?: ""
                        viewModel.exportManager.shareViaEmail(email, subject, body)
                    }
                )
            }

            // Export & Share Channels
            item {
                Text(
                    "Export & Share Channels",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // SMS Channel
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Send as Text / SMS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("Formatted SMS with Google Review link", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                val text = viewModel.exportManager.formatForSms(worksheet, companyName, googleReviewUrl)
                                val phone = fieldsMap["client_phone"] ?: fieldsMap["phone"] ?: ""
                                viewModel.exportManager.shareViaSms(phone, text)
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Send SMS", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // Email Channel
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Send as Email", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("Official GreenTree letterhead & review callout", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                val (subject, body) = viewModel.exportManager.formatForEmail(
                                    worksheet,
                                    companyName,
                                    companyAddress,
                                    companyPhone,
                                    companyWebsite,
                                    googleReviewUrl
                                )
                                val email = fieldsMap["client_email"] ?: fieldsMap["email"] ?: ""
                                viewModel.exportManager.shareViaEmail(email, subject, body)
                            }) {
                                Icon(Icons.Default.Send, contentDescription = "Send Email", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // CRM / Database Webhook Sync
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Push to CRM / Database", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (webhookUrl.isNotBlank()) "Syncs JSON to configured Webhook" else "Configure Webhook in Settings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                viewModel.pushCurrentToWebhook(worksheet)
                            }) {
                                Icon(Icons.Default.Sync, contentDescription = "Sync CRM", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    // DMS / Clipboard Exports
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val json = viewModel.exportManager.formatForJson(worksheet, companyName)
                                viewModel.exportManager.copyToClipboard("Worksheet JSON (DMS)", json)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy JSON")
                        }

                        OutlinedButton(
                            onClick = {
                                val csv = viewModel.exportManager.formatForCsv(worksheet)
                                viewModel.exportManager.copyToClipboard("Worksheet CSV", csv)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy CSV")
                        }
                    }
                }
            }

            // Webhook Sync Feedback
            item {
                when (val syncState = webhookState) {
                    is WebhookSyncState.Syncing -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pushing to CRM/Database endpoint...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    is WebhookSyncState.Success -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(syncState.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                    is WebhookSyncState.Error -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(syncState.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                    WebhookSyncState.Idle -> {}
                }
            }

            // Filled Form Fields Summary
            item {
                Text(
                    "Worksheet Fields",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    fieldsMap.forEach { (key, value) ->
                        if (value.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    key.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(0.4f)
                                )
                                Text(
                                    value,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Notes Section
            if (worksheet.notes.isNotBlank()) {
                item {
                    Text(
                        "Notes & Instructions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        worksheet.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }
            }

            // Original OCR Transcript if available
            if (worksheet.extractedText.isNotBlank()) {
                item {
                    Text(
                        "Original Scanned OCR Transcription",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        worksheet.extractedText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
