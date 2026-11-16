package com.example.grabclip.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabclip.common.STATE_STOP_TIMEOUT_MILLIS
import com.example.grabclip.data.local.SettingsPreferences
import com.example.grabclip.data.remote.EngineUpdate
import com.example.grabclip.data.remote.ExtractionException
import com.example.grabclip.data.remote.VideoExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class SettingsLocalState(val isUpdating: Boolean = false, val engineVersion: String = "", val updateResult: UpdateResult? = null)

class SettingsViewModel(private val settings: SettingsPreferences, private val extractor: VideoExtractor) : ViewModel() {
    private val local: MutableStateFlow<SettingsLocalState> = MutableStateFlow(SettingsLocalState(engineVersion = extractor.version()))

    val state: StateFlow<SettingsUiState> = combine(local, settings.observeDeleteAfterSharing(), settings.observePreferredQuality()) { current, isDeleteAfterSharing, preferredQuality ->
        SettingsUiState(isDeleteAfterSharing = isDeleteAfterSharing, isUpdating = current.isUpdating, preferredQuality = preferredQuality, engineVersion = current.engineVersion, updateResult = current.updateResult)
    }.stateIn(initialValue = SettingsUiState(isDeleteAfterSharing = settings.deleteAfterSharing(), preferredQuality = settings.preferredQuality()), scope = viewModelScope, started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS))

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetDeleteAfterSharing -> settings.setDeleteAfterSharing(event.isEnabled)
            is SettingsEvent.SetPreferredQuality -> settings.setPreferredQuality(event.preference)
            SettingsEvent.UpdateDownloader -> updateDownloader()
        }
    }

    private fun updateDownloader() {
        if (local.value.isUpdating) return
        local.update { current -> current.copy(isUpdating = true, updateResult = null) }
        viewModelScope.launch {
            val result = try {
                when (extractor.update()) {
                    EngineUpdate.CURRENT -> UpdateResult.CURRENT
                    EngineUpdate.UPDATED -> UpdateResult.UPDATED
                }
            } catch (exception: ExtractionException) {
                UpdateResult.FAILED
            }
            local.update { current -> current.copy(isUpdating = false, engineVersion = extractor.version(), updateResult = result) }
        }
    }
}
