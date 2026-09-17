package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ExportManager(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun formatEstimateExport(
        worksheet: Worksheet,
        companyName: String = "GreenTree Arboriculture",
        companyAddress: String = "4314 Fay Drive Columbus, GA 31907",
        companyWebsite: String = "greentreearboriculture.com",
        companyEmail: String = "mrgreen@greentreearboriculture.com",
        companyPhone: String = "706-505-4266",
        googleReviewUrl: String = "https://g.page/r/Ccypclex1Yp1EBM/review"
    ): String {
        val fields = parseFieldsJson(worksheet.fieldsJson)
        val sb = StringBuilder()
        sb.append("$companyName\n")
        sb.append("$companyAddress\n")
        sb.append("$companyWebsite\n")
        sb.append("$companyEmail\n")
        sb.append("$companyPhone\n\n")

        sb.append("Estimate\n")
        val statusVal = fields["status"] ?: worksheet.status
        sb.append("Status: $statusVal\n\n")

        sb.append("Bill To:\n")
        val client = fields["client_name"] ?: worksheet.clientName.ifEmpty { "PersonJ" }
        sb.append("\t$client\n")
        fields["property_address"]?.takeIf { it.isNotBlank() }?.let { sb.append("\t$it\n") }
        fields["client_phone"]?.takeIf { it.isNotBlank() }?.let { sb.append("\tPhone: $it\n") }
        fields["client_email"]?.takeIf { it.isNotBlank() }?.let { sb.append("\tEmail: $it\n") }

        val estNum = fields["estimate_number"] ?: fields["proposal_number"] ?: "04"
        val estDate = fields["estimate_date"] ?: fields["proposal_date"] ?: SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(worksheet.timestamp))
        sb.append("\n\tEstimate #\t:\t$estNum\n")
        sb.append("\tEstimate Date\t:\t$estDate\n\n")

        sb.append("\t#\tItem\tQty/Hrs\tAmount/Rate\tPrice\n")
        sb.append("\t-----------------------------------------------------------------\n")

        val lineItemsRaw = fields["line_items"] ?: fields["items_description"] ?: fields["scope_of_work"] ?: ""
        if (lineItemsRaw.isNotBlank()) {
            val lines = lineItemsRaw.lines()
            var autoIdx = 1
            for (line in lines) {
                if (line.isBlank()) continue
                if (line.contains("|")) {
                    val parts = line.split("|").map { it.trim() }
                    val itemNum = parts.getOrNull(0) ?: "$autoIdx"
                    val desc = parts.getOrNull(1) ?: ""
                    val qty = parts.getOrNull(2) ?: "1"
                    val rate = parts.getOrNull(3) ?: ""
                    val price = parts.getOrNull(4) ?: ""
                    sb.append("\t$itemNum\t$desc\t$qty\t$rate\t$price\n")
                } else {
                    sb.append("\t$autoIdx\t$line\t\t\t\n")
                }
                autoIdx++
            }
        } else {
            sb.append("\t1\tTree Service & Arboriculture Services\t1 Lot\t--\t$${String.format(Locale.US, "%.2f", worksheet.totalAmount)}\n")
        }

        sb.append("\n\t-----------------------------------------------------------------\n")
        val totalDisplay = if (worksheet.totalAmount > 0) String.format(Locale.US, "%.2f", worksheet.totalAmount) else (fields["amount_total"] ?: "0.00")
        sb.append("\tTotal\t\t\t\t\t\t$$totalDisplay\n\n")

        sb.append("Click or scan to give us a review on Google: $googleReviewUrl\n")
        sb.append("Thank you!\n")

        return sb.toString()
    }

    fun formatForSms(
        worksheet: Worksheet,
        companyName: String = "GreenTree Arboriculture",
        googleReviewUrl: String = "https://g.page/r/Ccypclex1Yp1EBM/review"
    ): String {
        val fields = parseFieldsJson(worksheet.fieldsJson)
        val sb = StringBuilder()
        sb.append("📋 $companyName - ${worksheet.title}\n")
        sb.append("Status: ${worksheet.status}\n")
        if (worksheet.clientName.isNotEmpty()) {
            sb.append("Client: ${worksheet.clientName}\n")
        }
        for ((key, value) in fields) {
            if (value.isNotBlank() && key != "client_name") {
                val readableKey = key.replace("_", " ").capitalizeWords()
                sb.append("• $readableKey: $value\n")
            }
        }
        if (worksheet.totalAmount > 0) {
            sb.append("Total: \$${String.format(Locale.US, "%.2f", worksheet.totalAmount)}\n")
        }
        if (worksheet.notes.isNotBlank()) {
            sb.append("Notes: ${worksheet.notes}\n")
        }
        sb.append("\nReview us on Google: $googleReviewUrl\n")
        sb.append("Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(worksheet.timestamp))}")
        return sb.toString()
    }

    fun formatForEmail(
        worksheet: Worksheet,
        companyName: String = "GreenTree Arboriculture",
        companyAddress: String = "4314 Fay Drive Columbus, GA 31907",
        companyPhone: String = "706-505-4266",
        companyWebsite: String = "greentreearboriculture.com",
        googleReviewUrl: String = "https://g.page/r/Ccypclex1Yp1EBM/review"
    ): Pair<String, String> {
        val subject = "[$companyName] ${worksheet.title} - ${if (worksheet.clientName.isNotEmpty()) worksheet.clientName else "Worksheet Record"}"
        val fields = parseFieldsJson(worksheet.fieldsJson)
        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("       $companyName - ESTIMATE & RECORD\n")
        sb.append("       $companyAddress\n")
        sb.append("       Phone: $companyPhone | $companyWebsite\n")
        sb.append("=========================================\n\n")
        sb.append("Document: ${worksheet.title}\n")
        sb.append("Template: ${worksheet.templateName}\n")
        sb.append("Status:   ${fields["status"] ?: worksheet.status}\n")
        sb.append("Date:     ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(worksheet.timestamp))}\n")
        if (worksheet.clientName.isNotEmpty()) {
            sb.append("Client:   ${worksheet.clientName}\n")
        }
        sb.append("\n--- DETAILS & FORM FIELDS ---\n")
        for ((key, value) in fields) {
            if (value.isNotBlank()) {
                val readableKey = key.replace("_", " ").capitalizeWords()
                sb.append(String.format(Locale.US, "%-25s: %s\n", readableKey, value))
            }
        }
        if (worksheet.totalAmount > 0) {
            sb.append(String.format(Locale.US, "\nTOTAL AMOUNT             : $%.2f\n", worksheet.totalAmount))
        }
        if (worksheet.notes.isNotBlank()) {
            sb.append("\nSPECIAL NOTES & INSTRUCTIONS:\n${worksheet.notes}\n")
        }
        sb.append("\n-----------------------------------------\n")
        sb.append("Click or scan to give us a review on Google: $googleReviewUrl\n")
        sb.append("Thank you for choosing $companyName!\n")
        sb.append("=========================================\n")

        return Pair(subject, sb.toString())
    }

    fun formatForJson(worksheet: Worksheet, companyName: String = "GreenTree Arboriculture"): String {
        val json = JSONObject()
        json.put("app", "GreenTree Worksheet Manager")
        json.put("company", companyName)
        json.put("id", worksheet.id)
        json.put("title", worksheet.title)
        json.put("templateId", worksheet.templateId)
        json.put("templateName", worksheet.templateName)
        json.put("status", worksheet.status)
        json.put("clientName", worksheet.clientName)
        json.put("totalAmount", worksheet.totalAmount)
        json.put("timestamp", worksheet.timestamp)
        json.put("dateFormatted", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(worksheet.timestamp)))
        json.put("notes", worksheet.notes)
        json.put("fields", JSONObject(worksheet.fieldsJson))
        if (worksheet.extractedText.isNotEmpty()) {
            json.put("rawOcrText", worksheet.extractedText)
        }
        return json.toString(2)
    }

    fun formatForCsv(worksheet: Worksheet): String {
        val fields = parseFieldsJson(worksheet.fieldsJson)
        val header = mutableListOf("ID", "Title", "Template", "Status", "Client", "Total", "Date", "Notes")
        val values = mutableListOf(
            worksheet.id.toString(),
            escapeCsv(worksheet.title),
            escapeCsv(worksheet.templateName),
            escapeCsv(worksheet.status),
            escapeCsv(worksheet.clientName),
            worksheet.totalAmount.toString(),
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(worksheet.timestamp)),
            escapeCsv(worksheet.notes)
        )
        for ((k, v) in fields) {
            header.add(k)
            values.add(escapeCsv(v))
        }
        return header.joinToString(",") + "\n" + values.joinToString(",")
    }

    fun shareViaSms(phoneNumber: String = "", textContent: String) {
        try {
            val uri = if (phoneNumber.isNotBlank()) Uri.parse("smsto:$phoneNumber") else Uri.parse("smsto:")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", textContent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback generic send
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textContent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Send Worksheet SMS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun shareViaEmail(recipientEmail: String = "", subject: String, bodyText: String) {
        try {
            val uri = if (recipientEmail.isNotBlank()) Uri.parse("mailto:$recipientEmail") else Uri.parse("mailto:")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                if (recipientEmail.isNotBlank()) putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, bodyText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Send Worksheet Email").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard!", Toast.LENGTH_SHORT).show()
    }

    suspend fun pushToWebhook(
        webhookUrl: String,
        apiKey: String?,
        worksheet: Worksheet,
        companyName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (webhookUrl.isBlank()) {
            return@withContext Result.failure(Exception("Webhook URL is not configured in Settings."))
        }
        try {
            val payload = formatForJson(worksheet, companyName)
            val requestBuilder = Request.Builder()
                .url(webhookUrl.trim())
                .post(payload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "GreenTree-WorksheetManager/1.0")

            if (!apiKey.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${apiKey.trim()}")
                requestBuilder.addHeader("X-API-Key", apiKey.trim())
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (response.isSuccessful) {
                val respText = response.body?.string() ?: "OK"
                Result.success("Successfully pushed to CRM/Database (HTTP ${response.code})")
            } else {
                Result.failure(Exception("Server returned HTTP ${response.code}: ${response.message}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to push to webhook: ${e.message}"))
        }
    }

    private fun parseFieldsJson(jsonStr: String): Map<String, String> {
        return try {
            val json = JSONObject(jsonStr)
            val map = mutableMapOf<String, String>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = json.optString(k)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString() }
        }
    }
}
