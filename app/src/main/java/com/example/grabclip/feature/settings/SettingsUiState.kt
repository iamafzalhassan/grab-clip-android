package com.example.grabclip.feature.settings

import androidx.compose.runtime.Immutable
import com.example.grabclip.domain.video.QualityPreference

enum class UpdateResult { CURRENT, FAILED, UPDATED }

sealed interface SettingsEvent {
    data object Opened : SettingsEvent

    data class SetDeleteAfterSharing(val isEnabled: Boolean) : SettingsEvent

    data class SetPreferredQuality(val preference: Int) : SettingsEvent

    data object UpdateDownloader : SettingsEvent
}

@Immutable
data class SettingsUiState(
    val isDeleteAfterSharing: Boolean = true,
    val isUpdating: Boolean = false,
    val preferredQuality: Int = QualityPreference.DEFAULT,
    val engineVersion: String = "",
    val updateResult: UpdateResult? = null,
)
