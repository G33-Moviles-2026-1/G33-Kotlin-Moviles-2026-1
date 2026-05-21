package com.example.andespace.ui.account

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.andespace.model.dto.UserStatus
import com.example.andespace.ui.theme.PrimaryYellow

private val statusLabels = mapOf(
    UserStatus.INCOGNITO to "No molestar",
    UserStatus.BUSY to "Ocupado",
    UserStatus.EXERCISING to "Haciendo ejercicio",
    UserStatus.FREE to "Libre",
    UserStatus.HANGING_OUT to "Saliendo",
    UserStatus.AT_HOME to "En casa",
    UserStatus.LUNCHING to "Almorzando"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusSelector(
    currentStatus: UserStatus,
    isLoading: Boolean,
    onStatusChange: (UserStatus) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Estado",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserStatus.entries.forEach { status ->
                val selected = status == currentStatus
                StatusChip(
                    label = statusLabels[status] ?: status.value,
                    selected = selected,
                    enabled = !isLoading,
                    onClick = { if (!selected) onStatusChange(status) }
                )
            }
        }

        if (currentStatus == UserStatus.INCOGNITO) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "En modo No molestar no recibirás notificaciones de amigos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) PrimaryYellow else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
