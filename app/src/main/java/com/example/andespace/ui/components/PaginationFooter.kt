package com.example.andespace.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PaginationFooter(
    currentPage: Int,
    totalPages: Int,
    isSearching: Boolean,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isSearching) {
            Text(
                text = "Loading page...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onPrevPage,
                enabled = !isSearching && currentPage > 1,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFFF2F2F2),
                    disabledContentColor = Color(0xFF9A9A9A)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text("Prev")
            }

            Text(
                text = "Page $currentPage of ${totalPages.coerceAtLeast(1)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = onNextPage,
                enabled = !isSearching && currentPage < totalPages,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFFF2F2F2),
                    disabledContentColor = Color(0xFF9A9A9A)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text("Next")
            }
        }
    }
}