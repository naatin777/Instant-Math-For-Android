package com.naatin777.instantmath.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.naatin777.instantmath.data.CopyFormat
import com.naatin777.instantmath.data.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {
    val themeMode = repository.themeModeFlow.asLiveData()
    val isDynamicColorEnabled = repository.isDynamicColorEnabledFlow.asLiveData()
    val copyFormat = repository.copyFormatFlow.asLiveData()

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDynamicColorEnabled(enabled)
        }
    }

    fun setCopyFormat(format: CopyFormat) {
        viewModelScope.launch {
            repository.setCopyFormat(format)
        }
    }
}
