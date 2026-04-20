package com.example.andespace.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class IconPosition {
    START, END
}

@Composable
fun CustomIconButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    @DrawableRes iconResId: Int,
    iconPosition: IconPosition = IconPosition.START,
    textPosition: Alignment = Alignment.Center,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        // Soften the border if disabled
        border = BorderStroke(1.dp, if (enabled) Color(0xFFE0E0E0) else Color(0xFFE0E0E0).copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val dynamicColor = LocalContentColor.current

            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$text icon",
                modifier = Modifier
                    .size(24.dp)
                    .align(
                        if (iconPosition == IconPosition.START) Alignment.CenterStart
                        else Alignment.CenterEnd
                    ),
                colorFilter = ColorFilter.tint(dynamicColor)
            )

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(textPosition),
                color = dynamicColor
            )
        }
    }
}