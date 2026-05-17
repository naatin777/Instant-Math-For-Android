package com.naatin777.instantmath.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.naatin777.instantmath.data.CopyFormat
import com.naatin777.instantmath.data.SettingsRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val copyFormat = settingsRepository.copyFormatFlow.asLiveData()

    fun setCopyFormat(format: CopyFormat) {
        viewModelScope.launch {
            settingsRepository.setCopyFormat(format)
        }
    }
}
