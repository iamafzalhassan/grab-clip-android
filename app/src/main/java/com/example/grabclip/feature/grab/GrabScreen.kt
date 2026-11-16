package com.example.grabclip.feature.grab

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.grabclip.R
import com.example.grabclip.domain.download.DownloadedVideo
import com.example.grabclip.domain.link.VideoSource
import com.example.grabclip.domain.video.VideoError
import com.example.grabclip.domain.video.VideoQuality
import com.example.grabclip.platform.rememberClipboardReader
import com.example.grabclip.platform.rememberNotificationPermissionRequest
import com.example.grabclip.ui.components.control.AppIconButton
import com.example.grabclip.ui.components.control.DangerButton
import com.example.grabclip.ui.components.control.PrimaryButton
import com.example.grabclip.ui.components.control.QualityOption
import com.example.grabclip.ui.components.control.SecondaryButton
import com.example.grabclip.ui.components.display.AppCard
import com.example.grabclip.ui.components.display.AppTag
import com.example.grabclip.ui.components.display.AppTopBar
import com.example.grabclip.ui.components.display.AppTray
import com.example.grabclip.ui.components.display.GlowStrip
import com.example.grabclip.ui.components.display.IridescentGlow
import com.example.grabclip.ui.components.display.ProgressRing
import com.example.grabclip.ui.components.display.ScrollFog
import com.example.grabclip.ui.components.display.TwoToneHeading
import com.example.grabclip.ui.components.feedback.ActivityIndicator
import com.example.grabclip.ui.components.feedback.AppSnackbarHost
import com.example.grabclip.ui.components.feedback.rememberAppSnackbarState
import com.example.grabclip.ui.format.DurationFormat
import com.example.grabclip.ui.format.SizeFormat
import com.example.grabclip.ui.format.qualityLabel
import com.example.grabclip.ui.format.rememberSizeUnits
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing
import com.example.grabclip.ui.theme.AppTextStyles
import kotlinx.coroutines.launch

private const val PERCENT_SCALE: Float = 100f

private const val VIDEO_TITLE_MAX_LINES: Int = 3

@Composable
fun GrabScreen(onOpenSettings: () -> Unit, onShare: (DownloadedVideo) -> Boolean, viewModel: GrabViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = rememberClipboardReader()
    val isWindowFocused = LocalWindowInfo.current.isWindowFocused
    val requestNotificationPermission = rememberNotificationPermissionRequest()
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val snackbarState = rememberAppSnackbarState()

    LifecycleStartEffect(viewModel) {
        viewModel.onEvent(GrabEvent.Returned)
        onStopOrDispose {}
    }

    LaunchedEffect(isWindowFocused) {
        if (isWindowFocused) {
            viewModel.onEvent(GrabEvent.ClipboardRead(isManual = false, text = clipboard.readIfChanged()))
        }
    }

    LaunchedEffect(state.feedback) {
        val feedback = state.feedback ?: return@LaunchedEffect
        scope.launch {
            when (feedback) {
                GrabFeedback.DownloadCancelled -> snackbarState.showBrief(resources.getString(R.string.feedback_download_cancelled))
                is GrabFeedback.DownloadFailed -> snackbarState.showError(resources.getString(feedback.error.messageRes()))
                GrabFeedback.LinkNotFound -> snackbarState.showError(resources.getString(R.string.feedback_link_not_found))
                GrabFeedback.ShareFailed -> snackbarState.showError(resources.getString(R.string.feedback_share_failed))
                GrabFeedback.VideoDeleted -> snackbarState.showSuccess(resources.getString(R.string.feedback_video_deleted))
                GrabFeedback.VideoKept -> snackbarState.showSuccess(resources.getString(R.string.feedback_video_kept))
            }
        }
        viewModel.onEvent(GrabEvent.DismissFeedback)
    }

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase)) {
        IridescentGlow(isDrifting = state.phase == GrabPhase.FETCHING || state.phase == GrabPhase.DOWNLOADING)
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(title = stringResource(R.string.app_name)) {
                AppIconButton(contentDescription = stringResource(R.string.action_settings), icon = Icons.Outlined.Settings, onClick = onOpenSettings)
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppSpacing.screenPadding)) {
                    Spacer(modifier = Modifier.height(AppSpacing.xxl))
                    if (state.phase == GrabPhase.EMPTY) {
                        Box(modifier = Modifier.size(AppSpacing.emptyStateIcon).background(color = AppColors.surfaceCard, shape = CircleShape), contentAlignment = Alignment.Center) {
                            Icon(modifier = Modifier.size(AppSpacing.iconEmptyState), contentDescription = null, imageVector = Icons.Outlined.Link, tint = AppColors.textTertiary)
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.xl))
                    }
                    GrabHeading(phase = state.phase)
                    Spacer(modifier = Modifier.height(AppSpacing.xl))
                    GrabContent(onSelectQuality = { quality -> viewModel.onEvent(GrabEvent.SelectQuality(quality)) }, state = state)
                    Spacer(modifier = Modifier.height(AppSpacing.fadingEdge))
                }
                ScrollFog(modifier = Modifier.align(Alignment.BottomCenter))
            }
            Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = AppSpacing.screenPadding)) {
                if (state.phase != GrabPhase.FETCHING) {
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                    GrabActions(
                        onCancel = { viewModel.onEvent(GrabEvent.CancelDownload) },
                        onDiscard = { viewModel.onEvent(GrabEvent.DiscardVideo) },
                        onDownload = {
                            requestNotificationPermission()
                            viewModel.onEvent(GrabEvent.Download)
                        },
                        onPaste = { viewModel.onEvent(GrabEvent.ClipboardRead(isManual = true, text = clipboard.read())) },
                        onRetry = { viewModel.onEvent(GrabEvent.Retry) },
                        onShare = {
                            val video = state.video
                            if (video != null && !onShare(video)) {
                                viewModel.onEvent(GrabEvent.ShareFailed)
                            }
                        },
                        state = state,
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.lg))
                }
            }
        }
        AppSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter), state = snackbarState)
    }
}

@Composable
private fun GrabHeading(phase: GrabPhase, modifier: Modifier = Modifier) {
    when (phase) {
        GrabPhase.CHOOSING -> TwoToneHeading(modifier = modifier, primary = stringResource(R.string.heading_choosing_primary), secondary = stringResource(R.string.heading_choosing_secondary))
        GrabPhase.DOWNLOADING -> TwoToneHeading(modifier = modifier, primary = stringResource(R.string.heading_downloading_primary), secondary = stringResource(R.string.heading_downloading_secondary))
        GrabPhase.EMPTY -> TwoToneHeading(modifier = modifier, primary = stringResource(R.string.heading_empty_primary), secondary = stringResource(R.string.heading_empty_secondary))
        GrabPhase.FAILED -> TwoToneHeading(modifier = modifier, primary = stringResource(R.string.heading_failed_primary), secondary = stringResource(R.string.heading_failed_secondary))
        GrabPhase.FETCHING -> TwoToneHeading(modifier = modifier, primary = stringResource(R.string.heading_fetching_primary), secondary = stringResource(R.string.heading_fetching_secondary))
        GrabPhase.READY -> TwoToneHeading(modifier = modifier, primary = stringResource(R.string.heading_ready_primary), secondary = stringResource(R.string.heading_ready_secondary))
    }
}

@Composable
private fun GrabContent(onSelectQuality: (VideoQuality) -> Unit, state: GrabUiState, modifier: Modifier = Modifier) {
    val sizeUnits = rememberSizeUnits()
    val details = state.details
    val selected = state.selected
    val video = state.video

    Column(modifier = modifier.fillMaxWidth()) {
        when (state.phase) {
            GrabPhase.CHOOSING -> if (details != null) {
                VideoCard(durationSeconds = details.durationSeconds, title = details.title, source = details.link.source)
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                AppTray {
                    details.qualities.forEach { quality ->
                        QualityOption(isSelected = quality == selected, label = qualityLabel(quality), meta = SizeFormat.format(bytes = quality.sizeBytes, units = sizeUnits), onClick = { onSelectQuality(quality) })
                    }
                }
            }
            GrabPhase.DOWNLOADING -> {
                VideoCard(durationSeconds = details?.durationSeconds ?: 0, title = details?.title.orEmpty(), source = details?.link?.source)
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                ProgressRing(caption = if (selected == null) "" else downloadSummary(quality = selected), fraction = state.progress, label = stringResource(R.string.progress_percent, (state.progress * PERCENT_SCALE).toInt()))
            }
            GrabPhase.EMPTY -> Text(modifier = Modifier.fillMaxWidth(), style = AppTextStyles.body.copy(color = AppColors.textSecondary), text = stringResource(R.string.empty_message))
            GrabPhase.FAILED -> Text(modifier = Modifier.fillMaxWidth(), style = AppTextStyles.body, text = stringResource((state.error ?: VideoError.FAILED).messageRes()))
            GrabPhase.FETCHING -> AppTray {
                AppCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                        ActivityIndicator(color = AppColors.textSecondary)
                        Text(modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.listSecondary, text = state.link?.url.orEmpty())
                    }
                }
            }
            GrabPhase.READY -> if (video != null) {
                VideoCard(durationSeconds = details?.durationSeconds ?: 0, title = video.title, source = details?.link?.source)
                Spacer(modifier = Modifier.height(AppSpacing.lg))
                GlowStrip(text = readySummary(video = video))
            }
        }
    }
}

@Composable
private fun VideoCard(durationSeconds: Int, title: String, source: VideoSource?, modifier: Modifier = Modifier) {
    if (title.isBlank()) return

    AppTray(modifier = modifier) {
        AppCard {
            if (source != null) {
                AppTag(label = sourceLabel(source))
                Spacer(modifier = Modifier.height(AppSpacing.md))
            }
            Text(maxLines = VIDEO_TITLE_MAX_LINES, overflow = TextOverflow.Ellipsis, style = AppTextStyles.videoTitle, text = title)
            if (durationSeconds > 0) {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(maxLines = 1, style = AppTextStyles.listMeta, text = DurationFormat.format(durationSeconds))
            }
        }
    }
}

@Composable
private fun GrabActions(
    onCancel: () -> Unit,
    onDiscard: () -> Unit,
    onDownload: () -> Unit,
    onPaste: () -> Unit,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    state: GrabUiState,
    modifier: Modifier = Modifier,
) {
    val selected = state.selected
    val video = state.video

    when (state.phase) {
        GrabPhase.CHOOSING -> ActionRow(modifier = modifier) {
            PrimaryButton(modifier = Modifier.weight(1f), isEnabled = selected != null, label = stringResource(R.string.action_download), onClick = onDownload)
            SecondaryButton(modifier = Modifier.weight(1f), isEnabled = false, label = stringResource(R.string.action_share), onClick = onShare)
        }
        GrabPhase.DOWNLOADING -> GlowStrip(modifier = modifier, text = if (selected == null) stringResource(R.string.heading_downloading_primary) else downloadSummary(quality = selected)) {
            AppIconButton(contentDescription = stringResource(R.string.action_cancel_download), icon = Icons.Outlined.Close, onClick = onCancel)
        }
        GrabPhase.EMPTY -> PrimaryButton(modifier = modifier.fillMaxWidth(), label = stringResource(R.string.action_paste_link), onClick = onPaste)
        GrabPhase.FAILED -> ActionRow(modifier = modifier) {
            SecondaryButton(modifier = Modifier.weight(1f), label = stringResource(R.string.action_paste_link), onClick = onPaste)
            PrimaryButton(modifier = Modifier.weight(1f), label = stringResource(R.string.action_try_again), onClick = onRetry)
        }
        GrabPhase.FETCHING -> Unit
        GrabPhase.READY -> ActionRow(modifier = modifier) {
            if (video?.isKept == true) {
                SecondaryButton(modifier = Modifier.weight(1f), label = stringResource(R.string.action_done), onClick = onDiscard)
            } else {
                DangerButton(modifier = Modifier.weight(1f), label = stringResource(R.string.action_delete), onClick = onDiscard)
            }
            PrimaryButton(modifier = Modifier.weight(1f), label = stringResource(R.string.action_share), onClick = onShare)
        }
    }
}

@Composable
private fun ActionRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md), content = content)
}

@Composable
private fun downloadSummary(quality: VideoQuality): String {
    val size = SizeFormat.format(bytes = quality.sizeBytes, units = rememberSizeUnits())
    return if (size.isEmpty()) qualityLabel(quality) else stringResource(R.string.download_summary, qualityLabel(quality), size)
}

@Composable
private fun readySummary(video: DownloadedVideo): String {
    val size = SizeFormat.format(bytes = video.sizeBytes, units = rememberSizeUnits())
    return if (size.isEmpty()) stringResource(R.string.ready_summary_unsized, qualityLabel(video.quality)) else stringResource(R.string.ready_summary, qualityLabel(video.quality), size)
}

@Composable
private fun sourceLabel(source: VideoSource): String = when (source) {
    VideoSource.FACEBOOK -> stringResource(R.string.source_facebook)
    VideoSource.INSTAGRAM -> stringResource(R.string.source_instagram)
    VideoSource.YOUTUBE -> stringResource(R.string.source_youtube)
}

@StringRes
private fun VideoError.messageRes(): Int = when (this) {
    VideoError.FAILED -> R.string.error_failed
    VideoError.LOGIN_REQUIRED -> R.string.error_login_required
    VideoError.NO_SPACE -> R.string.error_no_space
    VideoError.OFFLINE -> R.string.error_offline
    VideoError.UNAVAILABLE -> R.string.error_unavailable
    VideoError.UNSUPPORTED -> R.string.error_unsupported
}
