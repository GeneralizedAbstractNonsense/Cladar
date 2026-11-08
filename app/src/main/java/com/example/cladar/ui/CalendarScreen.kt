package com.example.cladar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cladar.model.EventItem
import com.example.cladar.ui.theme.*
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    isDarkMode: Boolean,
    events: List<EventItem>,
    onAddEvent: (EventItem) -> Unit,
    onEventClick: (EventItem) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAddModal by remember { mutableStateOf(false) }

    // Ticker per aggiornare il countdown in tempo reale ogni secondo
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = LocalDateTime.now()
        }
    }

    val bgColor = if (isDarkMode) OLEDBlack else GridBackgroundLight
    val surfaceColor = if (isDarkMode) OLEDSurface else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // --- PARTE SUPERIORE (50%): Mese & Registrazione ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
                .background(surfaceColor, RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "The Grand Almanack (${selectedDate.month.name} ${selectedDate.year})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BurgundyTitle
                )
                Button(
                    onClick = { showAddModal = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Mark Event", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Griglia Mese Semplificata (31 giorni)
            val daysInMonth = selectedDate.lengthOfMonth()
            val eventsByDate = events.groupBy { it.date }

            Column {
                val rows = (daysInMonth + 6) / 7
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (col in 1..7) {
                            val dayNum = row * 7 + col
                            if (dayNum <= daysInMonth) {
                                val dateStr = String.format("%04d-%02d-%02d", selectedDate.year, selectedDate.monthValue, dayNum)
                                val hasEvents = eventsByDate.containsKey(dateStr)
                                val isSelected = selectedDate.dayOfMonth == dayNum

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(
                                            if (isSelected) BurgundyTitle.copy(alpha = 0.2f) else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable {
                                            selectedDate = LocalDate.of(selectedDate.year, selectedDate.monthValue, dayNum)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isDarkMode) Color.White else Color.Black
                                    )
                                    if (hasEvents) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(PaletteWarmOrange, CircleShape)
                                                .align(Alignment.BottomCenter)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(34.dp))
                            }
                        }
                    }
                }
            }
        }

        Divider(color = BurgundyTitle.copy(alpha = 0.3f), thickness = 2.dp)

        // --- PARTE INFERIORE (50%): Countdown Eventi Futuri ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(
                text = "Time Remaining Until Events",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BurgundyTitle,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            val futureEvents = events.mapNotNull { event ->
                try {
                    val eventTime = LocalDateTime.of(
                        LocalDate.parse(event.date),
                        LocalTime.parse(event.time)
                    )
                    if (eventTime.isAfter(now)) Pair(event, eventTime) else null
                } catch (e: Exception) { null }
            }.sortedBy { it.second }

            if (futureEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No upcoming events marked in the Almanack", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(futureEvents) { (event, eventTime) ->
                        val duration = Duration.between(now, eventTime)
                        val days = duration.toDays()
                        val hours = duration.toHours() % 24
                        val mins = duration.toMinutes() % 60
                        val secs = duration.seconds % 60

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onEventClick(event) },
                            colors = CardDefaults.cardColors(containerColor = surfaceColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${event.date} at ${event.time} (${event.location})", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text(
                                    text = "${days}d ${hours}h ${mins}m ${secs}s",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(event.colorHex)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddModal) {
        AddEventModal(
            selectedDate = selectedDate,
            onDismiss = { showAddModal = false },
            onSave = { newEv ->
                onAddEvent(newEv)
                showAddModal = false
            }
        )
    }
}

// Modal di Creazione Evento con Color Picker esclusivo della Palette
@Composable
fun AddEventModal(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (EventItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("10:00") }
    var selectedColor by remember { mutableStateOf(EventColors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(
                        EventItem(
                            title = title,
                            date = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            time = time,
                            location = location.ifBlank { "Chamber I" },
                            colorHex = selectedColor.toArgb()
                        )
                    )
                }
            }) { Text("Record Event") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Mark New Event in Almanack", fontSize = 15.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") })
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (HH:mm)") })

                Text("Select Palette Color:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EventColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(color, CircleShape)
                                .border(
                                    if (selectedColor == color) 2.dp else 0.dp,
                                    Color.Black,
                                    CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        }
    )
}
