package com.example.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

data class DeviceContact(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = ""
)

object ContactsManager {

    /**
     * Extracts full contact information (Name, Phone, Email, and Address) from a contact URI
     * returned by the system Contact Picker (ActivityResultContracts.PickContact).
     */
    suspend fun getContactFromUri(context: Context, contactUri: Uri): DeviceContact? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            var contactId: String? = null
            var displayName: String? = null

            // 1. Query contact base record
            contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                    if (idIdx != -1) {
                        contactId = cursor.getString(idIdx)
                    }
                    val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        displayName = cursor.getString(nameIdx)
                    }
                }
            }

            // Fallback for lookup URI if _ID wasn't resolved directly
            if (contactId == null) {
                contactUri.lastPathSegment?.let { segment ->
                    contactId = segment
                }
            }

            var phoneNumber = ""
            var emailAddress = ""
            var postalAddress = ""

            if (contactId != null) {
                // 2. Query phone numbers
                try {
                    contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
                        ),
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        "${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC"
                    )?.use { phoneCursor ->
                        if (phoneCursor.moveToFirst()) {
                            val numIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            if (numIdx != -1) {
                                phoneNumber = phoneCursor.getString(numIdx) ?: ""
                            }
                        }
                    }
                } catch (e: Exception) {
                    // SecurityException or cursor failure
                }

                // 3. Query email addresses
                try {
                    contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Email.ADDRESS,
                            ContactsContract.CommonDataKinds.Email.IS_PRIMARY
                        ),
                        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        "${ContactsContract.CommonDataKinds.Email.IS_PRIMARY} DESC"
                    )?.use { emailCursor ->
                        if (emailCursor.moveToFirst()) {
                            val emailIdx = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                            if (emailIdx != -1) {
                                emailAddress = emailCursor.getString(emailIdx) ?: ""
                            }
                        }
                    }
                } catch (e: Exception) {
                    // SecurityException or cursor failure
                }

                // 4. Query physical / postal address
                try {
                    contentResolver.query(
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                            ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                            ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                            ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                            ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE
                        ),
                        "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID} = ?",
                        arrayOf(contactId),
                        null
                    )?.use { postalCursor ->
                        if (postalCursor.moveToFirst()) {
                            val formattedIdx = postalCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
                            if (formattedIdx != -1 && !postalCursor.getString(formattedIdx).isNullOrBlank()) {
                                postalAddress = postalCursor.getString(formattedIdx)
                            } else {
                                val streetIdx = postalCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)
                                val cityIdx = postalCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)
                                val regionIdx = postalCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)
                                val postIdx = postalCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)
                                val parts = listOfNotNull(
                                    if (streetIdx != -1) postalCursor.getString(streetIdx) else null,
                                    if (cityIdx != -1) postalCursor.getString(cityIdx) else null,
                                    if (regionIdx != -1) postalCursor.getString(regionIdx) else null,
                                    if (postIdx != -1) postalCursor.getString(postIdx) else null
                                ).filter { it.isNotBlank() }
                                postalAddress = parts.joinToString(", ")
                            }
                        }
                    }
                } catch (e: Exception) {
                    // SecurityException or cursor failure
                }
            }

            DeviceContact(
                id = contactId ?: "",
                name = displayName ?: "",
                phone = phoneNumber.trim(),
                email = emailAddress.trim(),
                address = postalAddress.trim()
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reads all device contacts from ContactsContract when READ_CONTACTS permission is granted.
     * Supports search filtering by name, phone, or email.
     */
    suspend fun getDeviceContacts(context: Context, searchQuery: String = ""): List<DeviceContact> = withContext(Dispatchers.IO) {
        val contactsMap = mutableMapOf<String, DeviceContact>()
        try {
            val contentResolver = context.contentResolver

            // 1. Read all phone contacts
            val phoneCursor: Cursor? = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            phoneCursor?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val id = if (idIdx != -1) cursor.getString(idIdx) ?: "" else ""
                    val name = if (nameIdx != -1) cursor.getString(nameIdx) ?: "" else ""
                    val num = if (numIdx != -1) cursor.getString(numIdx) ?: "" else ""
                    if (id.isNotEmpty()) {
                        val existing = contactsMap[id]
                        if (existing == null) {
                            contactsMap[id] = DeviceContact(id = id, name = name, phone = num)
                        } else if (existing.phone.isEmpty() && num.isNotEmpty()) {
                            contactsMap[id] = existing.copy(phone = num)
                        }
                    }
                }
            }

            // 2. Read emails to enrich
            val emailCursor: Cursor? = contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Email.ADDRESS
                ),
                null,
                null,
                null
            )

            emailCursor?.use { cursor ->
                val idIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME)
                val emailIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)

                while (cursor.moveToNext()) {
                    val id = if (idIdx != -1) cursor.getString(idIdx) ?: "" else ""
                    val name = if (nameIdx != -1) cursor.getString(nameIdx) ?: "" else ""
                    val email = if (emailIdx != -1) cursor.getString(emailIdx) ?: "" else ""
                    if (id.isNotEmpty()) {
                        val existing = contactsMap[id]
                        if (existing == null) {
                            contactsMap[id] = DeviceContact(id = id, name = name, email = email)
                        } else if (existing.email.isEmpty() && email.isNotEmpty()) {
                            contactsMap[id] = existing.copy(email = email)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // SecurityException if permission not granted
        }

        val allContacts = contactsMap.values.sortedBy { it.name.lowercase() }
        if (searchQuery.isBlank()) {
            allContacts
        } else {
            val q = searchQuery.trim().lowercase()
            allContacts.filter {
                it.name.lowercase().contains(q) ||
                it.phone.contains(q) ||
                it.email.lowercase().contains(q)
            }
        }
    }

    /**
     * Parses manually pasted contact information (e.g. from an email signature, SMS, or card)
     * without any fake or simulated data.
     */
    fun parseContactFromText(text: String): DeviceContact {
        if (text.isBlank()) return DeviceContact()

        var extractedName = ""
        var extractedPhone = ""
        var extractedEmail = ""
        var extractedAddress = ""

        // Email regex
        val emailMatcher = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matcher(text)
        if (emailMatcher.find()) {
            extractedEmail = emailMatcher.group()
        }

        // Phone regex
        val phoneMatcher = Pattern.compile("(\\+?\\d{1,3}[-.\\s]?)?(\\(?\\d{3}\\)?[-.\\s]?)?\\d{3}[-.\\s]?\\d{4}").matcher(text)
        if (phoneMatcher.find()) {
            extractedPhone = phoneMatcher.group()
        }

        // Address heuristic (lines containing Drive, Rd, St, Way, Lane, Ave, etc. or digit + street)
        val addressRegex = Pattern.compile("\\b\\d+\\s+([A-Za-z0-9.]+\\s+){1,4}(Street|St|Avenue|Ave|Road|Rd|Drive|Dr|Lane|Ln|Boulevard|Blvd|Way|Court|Ct|Circle|Cir)\\b.*", Pattern.CASE_INSENSITIVE)
        val lines = text.lines()
        for (line in lines) {
            val trimmed = line.trim()
            val addrMatcher = addressRegex.matcher(trimmed)
            if (addrMatcher.find() && extractedAddress.isEmpty()) {
                extractedAddress = trimmed
                continue
            }

            // Name detection heuristics
            if (extractedName.isEmpty()) {
                val lower = trimmed.lowercase()
                if (lower.startsWith("name:") || lower.startsWith("client:") || lower.startsWith("bill to:") || lower.startsWith("customer:")) {
                    extractedName = trimmed.substringAfter(":").trim()
                } else if (!trimmed.contains("@") && !trimmed.contains("http") && !trimmed.contains("www.") && trimmed.length in 3..40 && trimmed.none { it.isDigit() }) {
                    extractedName = trimmed
                }
            }
        }

        return DeviceContact(
            name = extractedName,
            phone = extractedPhone,
            email = extractedEmail,
            address = extractedAddress
        )
    }
}
