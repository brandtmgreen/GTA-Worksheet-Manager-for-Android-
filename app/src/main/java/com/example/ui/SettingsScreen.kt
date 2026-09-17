package com.example.ui

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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val apiKey by viewModel.apiKey.collectAsState()
    val webhookUrl by viewModel.webhookUrl.collectAsState()
    val companyName by viewModel.companyName.collectAsState()
    val companyAddress by viewModel.companyAddress.collectAsState()
    val companyPhone by viewModel.companyPhone.collectAsState()
    val companyEmail by viewModel.companyEmail.collectAsState()
    val companyWebsite by viewModel.companyWebsite.collectAsState()
    val googleReviewUrl by viewModel.googleReviewUrl.collectAsState()
    val crmName by viewModel.crmName.collectAsState()

    var tempApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var tempWebhookUrl by remember(webhookUrl) { mutableStateOf(webhookUrl) }
    var tempCompanyName by remember(companyName) { mutableStateOf(companyName) }
    var tempCompanyAddress by remember(companyAddress) { mutableStateOf(companyAddress) }
    var tempCompanyPhone by remember(companyPhone) { mutableStateOf(companyPhone) }
    var tempCompanyEmail by remember(companyEmail) { mutableStateOf(companyEmail) }
    var tempCompanyWebsite by remember(companyWebsite) { mutableStateOf(companyWebsite) }
    var tempGoogleReviewUrl by remember(googleReviewUrl) { mutableStateOf(googleReviewUrl) }
    var tempCrmName by remember(crmName) { mutableStateOf(crmName) }

    var saveNotice by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("App & Integration Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Configure CRM Webhooks, OCR API keys & Brand profile", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // CRM & Webhook Linking Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Database & CRM Integration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Link your worksheet submissions automatically to your CRM (Salesforce, HubSpot, Jobber, ArborGold) or custom database webhook endpoint.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = tempWebhookUrl,
                            onValueChange = { tempWebhookUrl = it },
                            label = { Text("Webhook URL") },
                            placeholder = { Text("https://api.yourcrm.com/v1/worksheets/sync") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = tempCrmName,
                            onValueChange = { tempCrmName = it },
                            label = { Text("CRM / DMS System Name") },
                            placeholder = { Text("e.g. Jobber / Company Database") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Gemini API & OCR Configuration Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "AI & Optical Character Recognition",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "The app uses Gemini 3.5 Flash Multimodal Vision & local OCR heuristics to transcribe documents and auto-populate form fields. You can optionally provide a custom Gemini API Key.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = tempApiKey,
                            onValueChange = { tempApiKey = it },
                            label = { Text("Gemini API Key (Optional Override)") },
                            placeholder = { Text("AIzaSy...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) }
                        )
                    }
                }
            }

            // Company Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Company & Contact Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Default information included in exported emails, text messages, and invoice headers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = tempCompanyName,
                            onValueChange = { tempCompanyName = it },
                            label = { Text("Company Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tempCompanyAddress,
                            onValueChange = { tempCompanyAddress = it },
                            label = { Text("Business Address") },
                            placeholder = { Text("4314 Fay Drive, Columbus, GA 31907") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tempCompanyPhone,
                            onValueChange = { tempCompanyPhone = it },
                            label = { Text("Business Phone") },
                            placeholder = { Text("706-505-4266") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tempCompanyEmail,
                            onValueChange = { tempCompanyEmail = it },
                            label = { Text("Business Email") },
                            placeholder = { Text("mrgreen@greentreearboriculture.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tempCompanyWebsite,
                            onValueChange = { tempCompanyWebsite = it },
                            label = { Text("Website") },
                            placeholder = { Text("greentreearboriculture.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = tempGoogleReviewUrl,
                            onValueChange = { tempGoogleReviewUrl = it },
                            label = { Text("Google Review Page URL (for QR Code)") },
                            placeholder = { Text("https://g.page/r/Ccypclex1Yp1EBM/review") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) }
                        )
                    }
                }
            }

            // Save Action
            item {
                Button(
                    onClick = {
                        viewModel.saveApiKey(tempApiKey)
                        viewModel.saveWebhookUrl(tempWebhookUrl)
                        viewModel.saveCompanyProfile(
                            name = tempCompanyName,
                            address = tempCompanyAddress,
                            phone = tempCompanyPhone,
                            email = tempCompanyEmail,
                            website = tempCompanyWebsite,
                            reviewUrl = tempGoogleReviewUrl,
                            crm = tempCrmName
                        )
                        saveNotice = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Settings", fontWeight = FontWeight.Bold)
                }

                if (saveNotice) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Settings saved successfully!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
