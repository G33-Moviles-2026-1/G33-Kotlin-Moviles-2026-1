package com.example.andespace.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.andespace.ui.theme.PrimaryYellow

@Composable
fun FriendPendingRequestRow(
    name: String,
    isOutgoing: Boolean,
    onDecline: (() -> Unit)? = null,
    onAccept: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (isOutgoing) {
            Text(
                text = "Pending",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            TextButton(onClick = { onCancel?.invoke() }) {
                Text("Cancel", color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)
            }
        } else {
            OutlinedButton(
                onClick = { onDecline?.invoke() },
                border = BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Decline", fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { onAccept?.invoke() },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryYellow),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Accept", color = Color.Black, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
