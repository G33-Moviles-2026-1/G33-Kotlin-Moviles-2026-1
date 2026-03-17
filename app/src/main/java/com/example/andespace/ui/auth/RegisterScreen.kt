package com.example.andespace.ui.auth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.andespace.ui.components.CustomTextField
import com.example.andespace.ui.components.CustomYellowButton

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by authViewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CustomTextField(
            value = uiState.email,
            onValueChange = { authViewModel.onEmailChange(it) },
            placeholder = "Email",
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = uiState.password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            placeholder = "Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = uiState.repeatPassword,
            onValueChange = { authViewModel.onRepeatPasswordChange(it) },
            placeholder = "Repeat your Password",
            isPassword = true
        )

        Spacer(modifier = Modifier.height(24.dp))


        if (uiState.isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            CustomYellowButton("Save", {
                authViewModel.onRegisterClick(onSuccess = onRegisterSuccess)
            })
        }

        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}