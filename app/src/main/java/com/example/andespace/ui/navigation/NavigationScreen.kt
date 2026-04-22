package com.example.andespace.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationScreen(
    navigationViewModel: NavigationViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by navigationViewModel.uiState.collectAsState()
    
    var fromClassroom by remember { mutableStateOf("") }
    var toClassroom by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Where Are You?",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomNavigationTextField(
            value = fromClassroom,
            onValueChange = { fromClassroom = it },
            placeholder = "ML 340"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Where You Want to Go?",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomNavigationTextField(
            value = toClassroom,
            onValueChange = { toClassroom = it },
            placeholder = "C 404"
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Follow Me:",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .verticalScroll(rememberScrollState())
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFF9C4)) // Light yellow
                    .padding(24.dp),
                contentAlignment = if (uiState.isLoading) Alignment.Center else Alignment.TopStart
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.Black)
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else if (uiState.instructions.isEmpty()) {
                    Text(
                        text = "Enter classrooms and click Book to see the path.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray)
                    )
                } else {
                    Column {
                        uiState.instructions.forEach { step ->
                            Text(
                                text = "• $step",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                navigationViewModel.getInstructions(fromClassroom, toClassroom)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFEB3B),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "GO",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CustomNavigationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
        )
    }
}
