package com.example.andespace.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.andespace.ui.components.CustomYellowButton

private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9._-]{3,25}$")

@Composable
fun ChangeUsernameForm(
    currentUsername: String,
    isLoading: Boolean,
    onSubmit: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf("") }

    val trimmed = newUsername.trim()
    val normalized = trimmed.lowercase()
    val isValidFormat = trimmed.isNotEmpty() && USERNAME_PATTERN.matches(trimmed)
    val isDifferent = normalized.isNotEmpty() && normalized != currentUsername.lowercase()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Change username",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newUsername,
            onValueChange = { newUsername = it },
            label = { Text("New username") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            isError = trimmed.isNotEmpty() && !isValidFormat,
            supportingText = {
                when {
                    trimmed.isNotEmpty() && !isValidFormat -> {
                        Text(
                            text = "3–25 characters: letters, numbers, . _ -",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    trimmed.isNotEmpty() && !isDifferent -> {
                        Text(
                            text = "Choose a different username than your current one.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomYellowButton(
            text = if (isLoading) "Updating..." else "Change username",
            onClick = { onSubmit(normalized) },
            enabled = !isLoading && isValidFormat && isDifferent
        )
    }
}
