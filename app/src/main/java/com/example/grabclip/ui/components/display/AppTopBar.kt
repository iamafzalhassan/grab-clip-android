package com.example.grabclip.ui.components.display

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.grabclip.ui.theme.AppSpacing
import com.example.grabclip.ui.theme.AppTextStyles

@Composable
fun AppTopBar(title: String, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        modifier = modifier.fillMaxWidth().statusBarsPadding().heightIn(min = AppSpacing.topBarHeight).padding(horizontal = AppSpacing.screenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.screenTitle, text = title)
        actions()
    }
}
