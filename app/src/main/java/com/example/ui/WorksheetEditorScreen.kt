package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.WorksheetViewModel
import com.example.data.TemplateField
import com.example.data.Worksheet
import com.example.util.DeviceContact
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetEditorScreen(
    viewModel: WorksheetViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToOcr: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentSheet by viewModel.currentWorksheet.collectAsState()
    val formFields by viewModel.formFields.collectAsState()
    val selectedTemplate by viewModel.selectedTemplate.collectAsState()

    var title by remember(currentSheet?.title) { mutableStateOf(currentSheet?.title ?: "New Worksheet") }
    var status by remember(currentSheet?.status) { mutableStateOf(currentSheet?.status ?: "Draft") }
    var notes by remember(currentSheet?.notes) { mutableStateOf(currentSheet?.notes ?: "") }
    var showCustomFieldDialog by remember { mutableStateOf(false) }
    var newFieldKey by remember { mutableStateOf("") }
    var newFieldValue by remember { mutableStateOf("") }

    // Contact & Paste Dialog States
    var showContactDialog by remember { mutableStateOf(false) }
    var showPasteOptionsDialog by remember { mutableStateOf(false) }
    var showAutoFillTextDialog by remember { mutableStateOf(false) }
    var autoFillText by remember { mutableStateOf("") }

    // Native System Contact Picker Launcher
    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            viewModel.importContactFromUri(context, contactUri) { contact ->
                coroutineScope.launch {
                    if (contact != null) {
                        val details = listOfNotNull(
                            if (contact.phone.isNotBlank()) "Phone" else null,
                            if (contact.email.isNotBlank()) "Email" else null,
                            if (contact.address.isNotBlank()) "Address" else null
                        ).joinToString(", ")
                        snackbarHostState.showSnackbar(
                            "Imported ${contact.name}${if (details.isNotBlank()) " ($details populated)" else ""}"
                        )
                    } else {
                        snackbarHostState.showSnackbar("Could not load contact details.")
                    }
                }
            }
        }
    }

    val statusOptions = listOf("Draft", "Completed", "Exported", "Synced")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentSheet?.id == 0) "New Worksheet" else "Edit Worksheet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedTemplate.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showContactDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Contacts, contentDescription = "Import from Contacts")
                    }
                    IconButton(
                        onClick = { showPasteOptionsDialog = true }
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste Clipboard Data")
                    }
                    IconButton(onClick = onNavigateToOcr) {
                        Icon(Icons.Default.DocumentScanner, contentDescription = "OCR Scan & Fill")
                    }
                    IconButton(onClick = { showAutoFillTextDialog = true }) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = "Auto Fill from Text")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.updateCurrentWorksheetMetadata(title, status, notes)
                            viewModel.saveCurrentWorksheet { saved ->
                                onNavigateToDetail(saved.id)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Export")
                    }

                    Button(
                        onClick = {
                            viewModel.updateCurrentWorksheetMetadata(title, status, notes)
                            viewModel.saveCurrentWorksheet {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Title and Metadata Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Worksheet Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                if (title.isNotEmpty()) {
                                    IconButton(onClick = { title = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Status", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            statusOptions.forEach { opt ->
                                FilterChip(
                                    selected = status == opt,
                                    onClick = { status = opt },
                                    label = { Text(opt) }
                                )
                            }
                        }
                    }
                }
            }

            // Quick Data Import & Paste Action Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showContactDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Contact", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showPasteOptionsDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paste Data", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalIconButton(
                            onClick = { showCustomFieldDialog = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Field", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Form Fields (${selectedTemplate.fields.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showCustomFieldDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Field")
                    }
                }
            }

            // Template Defined Fields
            items(selectedTemplate.fields) { field ->
                val currentValue = formFields[field.key] ?: ""
                val keyboardType = when (field.type) {
                    "number" -> KeyboardType.Number
                    "currency" -> KeyboardType.Decimal
                    "phone" -> KeyboardType.Phone
                    "email" -> KeyboardType.Email
                    else -> KeyboardType.Text
                }

                val isContactField = field.key.contains("client") || field.key.contains("customer") ||
                        field.key.contains("phone") || field.key.contains("email") || field.key.contains("address") ||
                        field.key.contains("bill_to") || field.key.contains("owner")

                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { viewModel.updateFormField(field.key, it) },
                    label = { Text(field.label) },
                    placeholder = { if (field.placeholder.isNotEmpty()) Text(field.placeholder) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = if (field.type == "multiline") 3 else 1,
                    maxLines = if (field.type == "multiline") 6 else 1,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            if (isContactField) {
                                IconButton(
                                    onClick = { showContactDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PersonSearch,
                                        contentDescription = "Select from Contacts",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    val clipText = clipboardManager.getText()?.text
                                    if (!clipText.isNullOrBlank()) {
                                        viewModel.pasteTextIntoField(field.key, clipText, append = field.type == "multiline")
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Pasted into ${field.label}")
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Clipboard is empty")
                                        }
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentPaste,
                                    contentDescription = "Paste from Clipboard",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            if (currentValue.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateFormField(field.key, "") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Clear Field",
                                        tint = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            }

            // Custom Dynamic Fields added by user or OCR
            items(formFields.keys.filter { key -> selectedTemplate.fields.none { it.key == key } }.toList()) { customKey ->
                val customVal = formFields[customKey] ?: ""
                OutlinedTextField(
                    value = customVal,
                    onValueChange = { viewModel.updateFormField(customKey, it) },
                    label = { Text(customKey.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clipText = clipboardManager.getText()?.text
                                    if (!clipText.isNullOrBlank()) {
                                        viewModel.pasteTextIntoField(customKey, clipText)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Pasted into $customKey")
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Clipboard is empty")
                                        }
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(18.dp))
                            }

                            if (customVal.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.updateFormField(customKey, "") },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                )
            }

            // Notes & Observations
            item {
                Text(
                    "Internal Notes & Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Crew Instructions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val clipText = clipboardManager.getText()?.text
                                    if (!clipText.isNullOrBlank()) {
                                        notes = if (notes.isBlank()) clipText else "$notes\n$clipText"
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Pasted into notes")
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Clipboard is empty")
                                        }
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste into notes", modifier = Modifier.size(18.dp))
                            }
                            if (notes.isNotEmpty()) {
                                IconButton(
                                    onClick = { notes = "" },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear notes", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // In-App Contact Picker Dialog
    if (showContactDialog) {
        ContactPickerDialog(
            onDismissRequest = { showContactDialog = false },
            onContactSelected = { contact ->
                viewModel.importContact(contact)
                coroutineScope.launch {
                    val details = listOfNotNull(
                        if (contact.phone.isNotBlank()) "Phone" else null,
                        if (contact.email.isNotBlank()) "Email" else null,
                        if (contact.address.isNotBlank()) "Address" else null
                    ).joinToString(", ")
                    snackbarHostState.showSnackbar(
                        "Imported ${contact.name}: ${if (details.isNotBlank()) details else "Name"} populated"
                    )
                }
            },
            onLaunchSystemPicker = {
                pickContactLauncher.launch(null)
            }
        )
    }

    // Paste Options / Clipboard Dialog
    if (showPasteOptionsDialog) {
        val clipboardText = clipboardManager.getText()?.text ?: ""
        var targetFieldKey by remember { mutableStateOf(selectedTemplate.fields.firstOrNull()?.key ?: "") }
        var isAppendMode by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPasteOptionsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paste Data into Fields")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (clipboardText.isBlank()) {
                        Text(
                            "Your clipboard is currently empty. Copy text, an address, or contact details, then paste here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "Clipboard Preview:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 100.dp)
                        ) {
                            Text(
                                text = clipboardText,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Smart Auto-Detect & Fill Button
                        Button(
                            onClick = {
                                val contact = viewModel.pasteAndAutoDetectContact(clipboardText)
                                coroutineScope.launch {
                                    if (contact.name.isNotBlank() || contact.phone.isNotBlank() || contact.email.isNotBlank()) {
                                        snackbarHostState.showSnackbar("Detected & imported: ${contact.name.ifEmpty { "Contact info" }}")
                                    } else {
                                        viewModel.parseAndPopulateText(clipboardText, selectedTemplate.id)
                                        snackbarHostState.showSnackbar("Extracted fields from text into form")
                                    }
                                }
                                showPasteOptionsDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto-Detect Contact & Fill Form")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Or Paste into a Specific Field:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Target Field Selector
                        selectedTemplate.fields.take(6).forEach { f ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable {
                                        viewModel.pasteTextIntoField(f.key, clipboardText, append = isAppendMode)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Pasted into ${f.label}")
                                        }
                                        showPasteOptionsDialog = false
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(f.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                    Text("Paste here →", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPasteOptionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Add Custom Field Dialog
    if (showCustomFieldDialog) {
        AlertDialog(
            onDismissRequest = { showCustomFieldDialog = false },
            title = { Text("Add Custom Field") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFieldKey,
                        onValueChange = { newFieldKey = it },
                        label = { Text("Field Label") },
                        placeholder = { Text("e.g. Permit Number, Gate Code") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newFieldValue,
                        onValueChange = { newFieldValue = it },
                        label = { Text("Initial Value") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFieldKey.isNotBlank()) {
                            val sanitizedKey = newFieldKey.lowercase().replace(" ", "_")
                            viewModel.updateFormField(sanitizedKey, newFieldValue)
                            newFieldKey = ""
                            newFieldValue = ""
                            showCustomFieldDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomFieldDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Auto-fill from text dialog
    if (showAutoFillTextDialog) {
        AlertDialog(
            onDismissRequest = { showAutoFillTextDialog = false },
            title = { Text("Auto-Fill from Text / Message") },
            text = {
                Column {
                    Text(
                        "Paste a text message, email or contact notes. The smart parser will extract client, dates, amounts, and addresses into this form.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = autoFillText,
                        onValueChange = { autoFillText = it },
                        label = { Text("Paste message/contact here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (autoFillText.isNotBlank()) {
                            viewModel.parseAndPopulateText(autoFillText, selectedTemplate.id)
                            showAutoFillTextDialog = false
                        }
                    }
                ) {
                    Text("Auto-Fill")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutoFillTextDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
