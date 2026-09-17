package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Worksheet
import com.example.util.QrCodeGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EstimateSheetCard(
    worksheet: Worksheet,
    fieldsMap: Map<String, String>,
    companyName: String = "GreenTree Arboriculture",
    companyAddress: String = "4314 Fay Drive Columbus, GA 31907",
    companyWebsite: String = "greentreearboriculture.com",
    companyEmail: String = "mrgreen@greentreearboriculture.com",
    companyPhone: String = "706-505-4266",
    googleReviewUrl: String = "https://g.page/r/Ccypclex1Yp1EBM/review",
    onCopyEstimate: () -> Unit,
    onShareSms: () -> Unit,
    onShareEmail: () -> Unit
) {
    val context = LocalContext.current

    val clientName = fieldsMap["client_name"] ?: worksheet.clientName.ifEmpty { "PersonJ" }
    val propertyAddress = fieldsMap["property_address"] ?: fieldsMap["site_address"] ?: "4314 Fay Drive, Columbus, GA 31907"
    val clientPhone = fieldsMap["client_phone"] ?: fieldsMap["phone"] ?: "706-505-4266"
    val clientEmail = fieldsMap["client_email"] ?: fieldsMap["email"] ?: "mrgreen@greentreearboriculture.com"
    val estimateNum = fieldsMap["estimate_number"] ?: fieldsMap["proposal_number"] ?: "04"
    val estimateDate = fieldsMap["estimate_date"] ?: fieldsMap["proposal_date"] ?: SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(worksheet.timestamp))
    val statusVal = fieldsMap["status"] ?: worksheet.status.ifEmpty { "Scheduled" }

    // Parse line items
    val lineItemsRaw = fieldsMap["line_items"] ?: fieldsMap["items_description"] ?: fieldsMap["scope_of_work"] ?: ""
    val parsedItems = remember(lineItemsRaw, worksheet.totalAmount) {
        val list = mutableListOf<EstimateLineItem>()
        if (lineItemsRaw.isNotBlank()) {
            val lines = lineItemsRaw.lines()
            var index = 1
            for (line in lines) {
                if (line.isBlank()) continue
                if (line.contains("|")) {
                    val parts = line.split("|").map { it.trim() }
                    list.add(
                        EstimateLineItem(
                            number = parts.getOrNull(0) ?: "$index",
                            item = parts.getOrNull(1) ?: "",
                            qtyHrs = parts.getOrNull(2) ?: "1",
                            amountRate = parts.getOrNull(3) ?: "",
                            price = parts.getOrNull(4) ?: ""
                        )
                    )
                } else {
                    list.add(
                        EstimateLineItem(
                            number = "$index",
                            item = line,
                            qtyHrs = "1",
                            amountRate = "--",
                            price = ""
                        )
                    )
                }
                index++
            }
        }
        if (list.isEmpty()) {
            list.add(
                EstimateLineItem(
                    number = "1",
                    item = "Crown Pruning & Canopy Elevation",
                    qtyHrs = "4.0 hrs",
                    amountRate = "$175.00/hr",
                    price = "$700.00"
                )
            )
            list.add(
                EstimateLineItem(
                    number = "2",
                    item = "Hazardous Limb Deadwooding & Rigging",
                    qtyHrs = "2.5 hrs",
                    amountRate = "$150.00/hr",
                    price = "$375.00"
                )
            )
            list.add(
                EstimateLineItem(
                    number = "3",
                    item = "Vermeer Brush Chipping & Wood Haulaway",
                    qtyHrs = "1.0 lot",
                    amountRate = "$175.00/lot",
                    price = "$175.00"
                )
            )
        }
        list
    }

    val totalFormatted = remember(worksheet.totalAmount, fieldsMap["amount_total"]) {
        if (worksheet.totalAmount > 0) {
            String.format(Locale.US, "%.2f", worksheet.totalAmount)
        } else {
            fieldsMap["amount_total"] ?: "1,250.00"
        }
    }

    // Generate QR Code bitmap
    val qrBitmap = remember(googleReviewUrl) {
        QrCodeGenerator.generateQrBitmap(googleReviewUrl, sizePx = 360)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Company Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        companyName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(companyAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(companyWebsite, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(companyEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(companyPhone, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        "ESTIMATE",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

            // Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (statusVal.lowercase()) {
                            "scheduled" -> MaterialTheme.colorScheme.tertiaryContainer
                            "accepted" -> MaterialTheme.colorScheme.primaryContainer
                            "completed" -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Text(
                            statusVal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bill To & Estimate Meta Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Bill To
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Bill To:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(clientName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    if (propertyAddress.isNotBlank()) {
                        Text(propertyAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (clientPhone.isNotBlank()) {
                        Text("Phone: $clientPhone", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (clientEmail.isNotBlank()) {
                        Text("Email: $clientEmail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Estimate # & Date
                Column(
                    modifier = Modifier.weight(0.8f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Estimate # : $estimateNum", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Estimate Date : $estimateDate", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Itemized Table Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#", modifier = Modifier.width(24.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("Item / Service", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("Qty/Hrs", modifier = Modifier.width(60.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        Text("Rate", modifier = Modifier.width(70.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                        Text("Price", modifier = Modifier.width(70.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Table Rows
                    parsedItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.number.ifEmpty { "${index + 1}" }, modifier = Modifier.width(24.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(item.item, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text(item.qtyHrs, modifier = Modifier.width(60.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                            Text(item.amountRate, modifier = Modifier.width(70.dp), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.End)
                            Text(item.price, modifier = Modifier.width(70.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
                        }
                        if (index < parsedItems.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)

                    // Total Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total : ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$$totalFormatted",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Review QR Code Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(googleReviewUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // ignore
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rendered QR Code
                    if (qrBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Google Review QR Code",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .fillMaxSize()
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Click or scan to give us a review on Google:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            googleReviewUrl,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Thank you! ⭐⭐⭐⭐⭐",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCopyEstimate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Estimate", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onShareSms,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Text SMS", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onShareEmail,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Email", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

data class EstimateLineItem(
    val number: String,
    val item: String,
    val qtyHrs: String,
    val amountRate: String,
    val price: String
)
