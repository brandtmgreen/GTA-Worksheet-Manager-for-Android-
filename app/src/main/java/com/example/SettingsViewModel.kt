package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settingsManager: SettingsManager) : ViewModel() {
    val apiKey: StateFlow<String> = settingsManager.apiKeyFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val webhookUrl: StateFlow<String> = settingsManager.webhookUrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val companyName: StateFlow<String> = settingsManager.companyNameFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "GreenTree Arboriculture")
    val companyAddress: StateFlow<String> = settingsManager.companyAddressFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "4314 Fay Drive, Columbus, GA 31907")
    val companyPhone: StateFlow<String> = settingsManager.companyPhoneFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "706-505-4266")
    val companyEmail: StateFlow<String> = settingsManager.companyEmailFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "mrgreen@greentreearboriculture.com")
    val companyWebsite: StateFlow<String> = settingsManager.companyWebsiteFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "greentreearboriculture.com")
    val googleReviewUrl: StateFlow<String> = settingsManager.googleReviewUrlFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "https://g.page/r/Ccypclex1Yp1EBM/review")
    val crmName: StateFlow<String> = settingsManager.crmNameFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Standard CRM / Webhook")

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch { settingsManager.saveApiKey(apiKey) }
    }

    fun saveWebhookUrl(webhookUrl: String) {
        viewModelScope.launch { settingsManager.saveWebhookUrl(webhookUrl) }
    }

    fun saveCompanyProfile(
        name: String,
        address: String = "4314 Fay Drive, Columbus, GA 31907",
        phone: String,
        email: String,
        website: String = "greentreearboriculture.com",
        reviewUrl: String = "https://g.page/r/Ccypclex1Yp1EBM/review",
        crm: String
    ) {
        viewModelScope.launch {
            settingsManager.saveCompanyProfile(
                name = name,
                address = address,
                phone = phone,
                email = email,
                website = website,
                reviewUrl = reviewUrl,
                crm = crm
            )
        }
    }
}
