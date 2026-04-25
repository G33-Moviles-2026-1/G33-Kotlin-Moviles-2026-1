package com.example.andespace.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import com.example.andespace.data.location.FusedLocationSensor
import com.example.andespace.ui.components.CustomYellowButton
import com.example.andespace.ui.theme.PrimaryYellow

@Composable
fun NavigationScreen(
    navigationViewModel: NavigationViewModel, modifier: Modifier = Modifier
) {
    val uiState by navigationViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationSensor = remember(context) { FusedLocationSensor(context.applicationContext) }

    var fromClassroom by remember { mutableStateOf(uiState.fromClassroom ?: "") }
    var toClassroom by remember { mutableStateOf(uiState.toClassroom ?: "") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            navigationViewModel.useCurrentLocationAsFromClassroom(locationSensor)
        }
    }

    LaunchedEffect(uiState.fromClassroom) {
        uiState.fromClassroom?.let {
            fromClassroom = it
        }
    }

    LaunchedEffect(uiState.toClassroom) {
        uiState.toClassroom?.let {
            toClassroom = it
        }
    }

    // Pagination state
    var currentPage by remember { mutableIntStateOf(0) }
    val stepsPerPage = 3
    val totalPages =
        if (uiState.instructions.isEmpty()) 0 else (uiState.instructions.size + stepsPerPage - 1) / stepsPerPage

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
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp, fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomNavigationTextField(value = fromClassroom, onValueChange = {
            fromClassroom = it
            navigationViewModel.onFromClassroomChange(it)
        }, placeholder = "ML 340", trailingIcon = {
            IconButton(
                onClick = {
                    val alreadyGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (alreadyGranted) {
                        navigationViewModel.useCurrentLocationAsFromClassroom(locationSensor)
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }, enabled = !uiState.isLocating
            ) {
                if (uiState.isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Use current location",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        })

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Where Do You Want to Go?",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp, fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(12.dp))

        CustomNavigationTextField(
            value = toClassroom, onValueChange = { toClassroom = it }, placeholder = "C 404"
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Follow these steps:", style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
            )
            if (uiState.instructions.isNotEmpty() && !uiState.isLoading) {
                Text(
                    text = "Est. ${formatTotalTime(uiState.totalTimeSeconds)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        if (uiState.isFromCache) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cached route",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = if (uiState.isLoading) Alignment.Center else Alignment.TopCenter
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(color = PrimaryYellow)
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            } else if (uiState.instructions.isEmpty()) {
                Text(
                    text = "Enter classrooms and click the button to see the path.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.Gray),
                    textAlign = TextAlign.Center
                )
            } else {
                val pagedInstructions =
                    uiState.instructions.windowed(stepsPerPage, stepsPerPage, true)
                val currentSteps = pagedInstructions.getOrNull(currentPage) ?: emptyList()

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    currentSteps.forEachIndexed { index, step ->
                        StepItem(
                            number = currentPage * stepsPerPage + index + 1, text = step
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous",
                                tint = if (currentPage > 0) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }
                        Text(
                            text = "${currentPage + 1} / $totalPages",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = { if (currentPage < totalPages - 1) currentPage++ },
                            enabled = currentPage < totalPages - 1
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next",
                                tint = if (currentPage < totalPages - 1) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        CustomYellowButton(
            text = "Show me the way", onClick = {
                navigationViewModel.getInstructions(fromClassroom, toClassroom)
                currentPage = 0
            })
        Spacer(modifier = Modifier.height(30.dp))

    }
}


@Composable
fun StepItem(number: Int, text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, PrimaryYellow.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(PrimaryYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(), style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold, color = Color.Black
                    )
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CustomNavigationTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
                )
            }
            if (trailingIcon != null) {
                trailingIcon()
            }
        }
    }
}

private fun formatTotalTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return if (minutes > 0) {
        "${minutes}min ${remainingSeconds}s"
    } else {
        "${remainingSeconds}s"
    }
}
