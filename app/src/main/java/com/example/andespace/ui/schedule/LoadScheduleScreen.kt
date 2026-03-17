package com.example.andespace.ui.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.andespace.R
import com.example.andespace.ui.components.CustomIconButton
import com.example.andespace.ui.components.IconPosition

@Composable
fun LoadScheduleScreen(
    onGoogleCalendarClick: () -> Unit = {},
    onIcsFileClick: () -> Unit = {},
    onLoadManuallyClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Upload your schedule",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Upload your university schedule to let AndeSpace find the best spots for you to stay around the campus!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        CustomIconButton(
            text = "Google Calendar",
            iconResId = R.drawable.ic_google,
            iconPosition = IconPosition.START,
            onClick = onGoogleCalendarClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomIconButton(
            text = ".ics File",
            iconResId = R.drawable.ic_file,
            iconPosition = IconPosition.START,
            onClick = onIcsFileClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomIconButton(
            text = "Load Manually",
            iconResId = R.drawable.ic_manual,
            iconPosition = IconPosition.START,
            onClick = onIcsFileClick
        )
    }
}