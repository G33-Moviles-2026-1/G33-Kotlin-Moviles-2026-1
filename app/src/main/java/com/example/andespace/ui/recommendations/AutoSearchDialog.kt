package com.example.andespace.ui.recommendations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoSearchDialog(
    onDismiss: () -> Unit,
    onConfirm: (start: LocalTime, end: LocalTime) -> Unit
) {
    val availableSlots = remember {
        val slots = mutableListOf<Pair<LocalTime, LocalTime>>()
        var current = LocalTime.of(6, 30)
        val endOfDay = LocalTime.of(21, 30)
        val now = LocalTime.now()

        while (current.isBefore(endOfDay)) {
            val slotEnd = current.plusMinutes(80)

            if (current.isAfter(now) || (now.isAfter(current) && now.isBefore(slotEnd))) {
                slots.add(Pair(current, slotEnd))
            }
            current = current.plusMinutes(90)
        }
        slots
    }

    var selectedSlot by remember { mutableStateOf<Pair<LocalTime, LocalTime>?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto Search Today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedSlot?.let { "${it.first.format(formatter)} - ${it.second.format(formatter)}" } ?: "Select a time slot",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        if (availableSlots.isEmpty()) {
                            DropdownMenuItem(text = { Text("No slots left today") }, onClick = {})
                        }
                        availableSlots.forEach { slot ->
                            DropdownMenuItem(
                                text = { Text("${slot.first.format(formatter)} - ${slot.second.format(formatter)}") },
                                onClick = {
                                    selectedSlot = slot
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        selectedSlot?.let { onConfirm(it.first, it.second) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = selectedSlot != null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Find Rooms Today", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}