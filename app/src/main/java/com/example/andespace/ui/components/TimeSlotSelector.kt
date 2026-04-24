package com.example.andespace.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotSelector(
    selectedDate: LocalDate,
    selectedStartTime: LocalTime?,
    onDateChange: (LocalDate) -> Unit,
    onTimeChange: (start: LocalTime, end: LocalTime) -> Unit
) {
    val availableSlots = remember(selectedDate) {
        val slots = mutableListOf<Pair<LocalTime, LocalTime>>()
        var current = LocalTime.of(6, 0)
        val endOfDay = LocalTime.of(22, 0)
        val now = LocalTime.now()
        val isToday = selectedDate == LocalDate.now()

        while (current.isBefore(endOfDay)) {
            val next = current.plusMinutes(90)
            if (!isToday || current.isAfter(now)) {
                slots.add(Pair(current, next))
            }
            current = next
        }
        slots
    }

    var expanded by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("1. Select Date", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0..2).forEach { offset ->
                val date = LocalDate.now().plusDays(offset.toLong())
                FilterChip(
                    selected = selectedDate == date,
                    onClick = { onDateChange(date) },
                    label = { Text(if (offset == 0) "Today" else date.dayOfWeek.name.take(3)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("2. Select Time", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStartTime?.let { "${it.format(formatter)} - ${it.plusMinutes(90).format(formatter)}" } ?: "Select a slot",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (availableSlots.isEmpty()) {
                    DropdownMenuItem(text = { Text("No slots left today") }, onClick = {})
                }
                availableSlots.forEach { (start, end) ->
                    DropdownMenuItem(
                        text = { Text("${start.format(formatter)} - ${end.format(formatter)}") },
                        onClick = {
                            onTimeChange(start, end)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}