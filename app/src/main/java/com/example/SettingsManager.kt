package com.example

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val WEBHOOK_URL = stringPreferencesKey("webhook_url")
        val COMPANY_NAME = stringPreferencesKey("company_name")
        val COMPANY_ADDRESS = stringPreferencesKey("company_address")
        val COMPANY_PHONE = stringPreferencesKey("company_phone")
        val COMPANY_EMAIL = stringPreferencesKey("company_email")
        val COMPANY_WEBSITE = stringPreferencesKey("company_website")
        val GOOGLE_REVIEW_URL = stringPreferencesKey("google_review_url")
        val CRM_NAME = stringPreferencesKey("crm_name")
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { it[API_KEY] ?: "" }
    val webhookUrlFlow: Flow<String> = context.dataStore.data.map { it[WEBHOOK_URL] ?: "" }
    val companyNameFlow: Flow<String> = context.dataStore.data.map { it[COMPANY_NAME] ?: "GreenTree Arboriculture" }
    val companyAddressFlow: Flow<String> = context.dataStore.data.map { it[COMPANY_ADDRESS] ?: "4314 Fay Drive, Columbus, GA 31907" }
    val companyPhoneFlow: Flow<String> = context.dataStore.data.map { it[COMPANY_PHONE] ?: "706-505-4266" }
    val companyEmailFlow: Flow<String> = context.dataStore.data.map { it[COMPANY_EMAIL] ?: "mrgreen@greentreearboriculture.com" }
    val companyWebsiteFlow: Flow<String> = context.dataStore.data.map { it[COMPANY_WEBSITE] ?: "greentreearboriculture.com" }
    val googleReviewUrlFlow: Flow<String> = context.dataStore.data.map { it[GOOGLE_REVIEW_URL] ?: "https://g.page/r/Ccypclex1Yp1EBM/review" }
    val crmNameFlow: Flow<String> = context.dataStore.data.map { it[CRM_NAME] ?: "Standard CRM / Webhook" }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { it[API_KEY] = apiKey.trim() }
    }

    suspend fun saveWebhookUrl(webhookUrl: String) {
        context.dataStore.edit { it[WEBHOOK_URL] = webhookUrl.trim() }
    }

    suspend fun saveCompanyProfile(
        name: String,
        address: String = "4314 Fay Drive, Columbus, GA 31907",
        phone: String,
        email: String,
        website: String = "greentreearboriculture.com",
        reviewUrl: String = "https://g.page/r/Ccypclex1Yp1EBM/review",
        crm: String
    ) {
        context.dataStore.edit {
            it[COMPANY_NAME] = name.trim()
            it[COMPANY_ADDRESS] = address.trim()
            it[COMPANY_PHONE] = phone.trim()
            it[COMPANY_EMAIL] = email.trim()
            it[COMPANY_WEBSITE] = website.trim()
            it[GOOGLE_REVIEW_URL] = reviewUrl.trim()
            it[CRM_NAME] = crm.trim()
        }
    }
}
