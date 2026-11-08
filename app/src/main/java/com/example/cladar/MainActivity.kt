package com.example.cladar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.cladar.model.*
import com.example.cladar.ui.*
import com.example.cladar.ui.theme.*
import java.time.DayOfWeek

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val storage = remember { StorageManager(context) }

            var isDarkMode by remember { mutableStateOf(storage.loadDarkMode()) }
            var events by remember { mutableStateOf(storage.loadEvents()) }
            var activities by remember {
                mutableStateOf(
                    storage.loadActivities().ifEmpty {
                        listOf(
                            ActivityItem(dayOfWeek = DayOfWeek.MONDAY, title = "Analisi I", location = "Aula Volterra", startHour = 8, colorHex = PaletteSageGreen.toArgb()),
                            ActivityItem(dayOfWeek = DayOfWeek.MONDAY, title = "Algebra Lineare", location = "Aula Enriques", startHour = 10, colorHex = PaletteGoldAmber.toArgb()),
                            ActivityItem(dayOfWeek = DayOfWeek.MONDAY, title = "Fisica I", location = "Aula De Lollis", startHour = 14, colorHex = PaletteWarmOrange.toArgb())
                        )
                    }
                )
            }

            var selectedEventForModal by remember { mutableStateOf<EventItem?>(null) }
            var showSettingsDialog by remember { mutableStateOf(false) }

            val colorScheme = if (isDarkMode) {
                darkColorScheme(background = OLEDBlack, surface = OLEDSurface, primary = BurgundyTitle)
            } else {
                lightColorScheme(background = GridBackgroundLight, surface = Color.White, primary = BurgundyTitle)
            }

            MaterialTheme(colorScheme = colorScheme) {
                MainAppLayout(
                    isDarkMode = isDarkMode,
                    events = events,
                    activities = activities,
                    onEventsChange = { updated ->
                        events = updated
                        storage.saveEvents(updated)
                    },
                    onActivitiesChange = { updated ->
                        activities = updated
                        storage.saveActivities(updated)
                    },
                    onOpenSettings = { showSettingsDialog = true },
                    onEventClick = { selectedEventForModal = it }
                )

                selectedEventForModal?.let { ev ->
                    EventDetailModal(
                        event = ev,
                        isDarkMode = isDarkMode,
                        onDismiss = { selectedEventForModal = null },
                        onSaveNotes = { newNotes ->
                            events = events.map { if (it.id == ev.id) it.copy(notesMarkdown = newNotes) else it }
                            storage.saveEvents(events)
                        }
                    )
                }

                if (showSettingsDialog) {
                    AlertDialog(
                        onDismissRequest = { showSettingsDialog = false },
                        confirmButton = {
                            TextButton(onClick = { showSettingsDialog = false }) { Text("Close") }
                        },
                        title = { Text("Settings & Mode of Night") },
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("OLED Obsidian Dark Mode")
                                Switch(
                                    checked = isDarkMode,
                                    onCheckedChange = {
                                        isDarkMode = it
                                        storage.saveDarkMode(it)
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(
    isDarkMode: Boolean,
    events: List<EventItem>,
    activities: List<ActivityItem>,
    onEventsChange: (List<EventItem>) -> Unit,
    onActivitiesChange: (List<ActivityItem>) -> Unit,
    onOpenSettings: () -> Unit,
    onEventClick: (EventItem) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cladar", color = BurgundyTitle) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = if (isDarkMode) OLEDSurface else Color.White) {
                listOf("Thy Tasks", "Diurnal Log", "Almanack").forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                when (index) {
                                    0 -> Icons.Default.Check
                                    1 -> Icons.Default.Edit
                                    else -> Icons.Default.DateRange
                                },
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Thy Tasks & Toils") }
                1 -> DailyScreen(
                    isDarkMode = isDarkMode,
                    activities = activities,
                    events = events,
                    onEventClick = onEventClick,
                    onOpenEditModal = {}
                )
                2 -> CalendarScreen(
                    isDarkMode = isDarkMode,
                    events = events,
                    onAddEvent = { newEv ->
                        onEventsChange(events + newEv)
                    },
                    onEventClick = onEventClick
                )
            }
        }
    }
}
