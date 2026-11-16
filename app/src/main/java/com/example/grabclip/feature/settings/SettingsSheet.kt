package com.example.grabclip.feature.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.grabclip.R
import com.example.grabclip.domain.video.QualityPreference
import com.example.grabclip.ui.components.control.AppChip
import com.example.grabclip.ui.components.control.AppSwitch
import com.example.grabclip.ui.components.control.PrimaryButton
import com.example.grabclip.ui.components.control.SecondaryButton
import com.example.grabclip.ui.components.display.AppCard
import com.example.grabclip.ui.components.display.AppTray
import com.example.grabclip.ui.components.sheet.AppModalSheet
import com.example.grabclip.ui.format.preferenceLabel
import com.example.grabclip.ui.theme.AppSpacing
import com.example.grabclip.ui.theme.AppTextStyles

@Composable
fun SettingsSheet(onDismiss: () -> Unit, viewModel: SettingsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.onEvent(SettingsEvent.Opened)
    }

    AppModalSheet(modifier = modifier, onDismiss = onDismiss, title = stringResource(R.string.settings_title)) { sheet ->
        AppTray {
            AppCard {
                SettingRow(
                    message = stringResource(if (state.isDeleteAfterSharing) R.string.settings_delete_message_on else R.string.settings_delete_message_off),
                    title = stringResource(R.string.settings_delete_title),
                ) {
                    AppSwitch(isChecked = state.isDeleteAfterSharing, onCheckedChange = { isEnabled -> viewModel.onEvent(SettingsEvent.SetDeleteAfterSharing(isEnabled)) })
                }
            }
            AppCard {
                SettingText(message = stringResource(R.string.settings_quality_message), title = stringResource(R.string.settings_quality_title))
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    QualityPreference.options.forEach { preference ->
                        AppChip(isSelected = preference == state.preferredQuality, label = preferenceLabel(preference), onClick = { viewModel.onEvent(SettingsEvent.SetPreferredQuality(preference)) })
                    }
                }
            }
            AppCard {
                SettingRow(message = downloaderMessage(state = state), title = stringResource(R.string.settings_downloader_title)) {
                    SecondaryButton(isLoading = state.isUpdating, label = stringResource(R.string.action_update), onClick = { viewModel.onEvent(SettingsEvent.UpdateDownloader) })
                }
            }
        }
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        PrimaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(R.string.action_done), onClick = { sheet.closeThen(onDismiss) })
    }
}

@Composable
private fun SettingRow(message: String, title: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
        SettingText(modifier = Modifier.weight(1f), message = message, title = title)
        trailing()
    }
}

@Composable
private fun SettingText(message: String, title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.listPrimary, text = title)
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.listSecondary, text = message)
    }
}

@Composable
private fun downloaderMessage(state: SettingsUiState): String = when (state.updateResult) {
    UpdateResult.CURRENT -> stringResource(R.string.settings_update_current)
    UpdateResult.FAILED -> stringResource(R.string.settings_update_failed)
    UpdateResult.UPDATED -> stringResource(R.string.settings_update_done)
    null -> if (state.engineVersion.isBlank()) stringResource(R.string.settings_downloader_version_unknown) else stringResource(R.string.settings_downloader_version, state.engineVersion)
}
