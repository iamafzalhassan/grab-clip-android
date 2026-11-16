package com.example.grabclip.ui.components.display

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.grabclip.ui.theme.AppColors
import com.example.grabclip.ui.theme.AppTextStyles

@Composable
fun TwoToneHeading(primary: String, secondary: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.display, text = primary)
        Text(maxLines = 1, overflow = TextOverflow.Ellipsis, style = AppTextStyles.display.copy(color = AppColors.textTertiary), text = secondary)
    }
}
