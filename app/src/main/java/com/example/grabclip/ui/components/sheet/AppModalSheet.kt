package com.example.grabclip.ui.components.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppSpacing
import com.example.grabclip.ui.theme.AppTextStyles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalSheet(title: String, onDismiss: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.(sheet: AppSheetState) -> Unit) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheet = remember(scope, sheetState) { AppSheetState(scope = scope, sheetState = sheetState) }

    ModalBottomSheet(
        modifier = modifier,
        containerColor = AppColors.surfaceBase,
        dragHandle = { BottomSheetDefaults.DragHandle(color = AppColors.divider) },
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topEnd = AppSpacing.radiusTray, topStart = AppSpacing.radiusTray),
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = AppSpacing.xl, end = AppSpacing.screenPadding, start = AppSpacing.screenPadding)) {
            Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.sectionHeading, text = title)
            Spacer(modifier = Modifier.height(AppSpacing.xl))
            content(sheet)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Stable
class AppSheetState internal constructor(private val scope: CoroutineScope, private val sheetState: SheetState) {
    private var isClosing: Boolean = false

    fun closeThen(action: () -> Unit) {
        if (isClosing) return
        isClosing = true
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }
}
