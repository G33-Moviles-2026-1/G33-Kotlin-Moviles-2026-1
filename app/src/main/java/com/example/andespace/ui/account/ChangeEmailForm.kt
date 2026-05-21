package com.example.andespace.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.andespace.ui.components.CustomYellowButton

@Composable
fun ChangeEmailForm(
    currentEmail: String,
    isLoading: Boolean,
    cooldownDaysLeft: Int?,
    onSubmit: (newEmail: String, currentPassword: String) -> Unit
) {
    var newEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val isEmailValid = newEmail.trim().lowercase().endsWith("@uniandes.edu.co")
    val isBlocked = cooldownDaysLeft != null

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Change institutional email",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            label = { Text("New email (@uniandes.edu.co)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = newEmail.isNotEmpty() && !isEmailValid,
            supportingText = {
                if (newEmail.isNotEmpty() && !isEmailValid) {
                    Text("Email must end in @uniandes.edu.co.", color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            enabled = !isBlocked
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            label = { Text("Current password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            singleLine = true,
            enabled = !isBlocked
        )

        if (isBlocked) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You can change your email in $cooldownDaysLeft day(s).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        CustomYellowButton(
            text = if (isLoading) "Updating..." else "Change Email",
            onClick = { onSubmit(newEmail, currentPassword) },
            enabled = !isLoading && !isBlocked && isEmailValid && currentPassword.isNotEmpty()
        )
    }
}
