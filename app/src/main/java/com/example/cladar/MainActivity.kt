package com.example.cladar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.cladar.model.ActivityItem
import com.example.cladar.ui.DailyScreen
import com.example.cladar.ui.theme.*
import java.time.DayOfWeek

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkMode by remember { mutableStateOf(false) }

            // Tema dinamico con supporto OLED (#000000 puro)
            val colorScheme = if (isDarkMode) {
                darkColorScheme(
                    background = OLEDBlack,
                    surface = OLEDSurface,
                    primary = BurgundyTitle
                )
            } else {
                lightColorScheme(
                    background = GridBackgroundLight,
                    surface = Color.White,
                    primary = BurgundyTitle
                )
            }

            MaterialTheme(colorScheme = colorScheme) {
                MainScreen(
                    isDarkMode = isDarkMode,
                    onToggleTheme = { isDarkMode = !isDarkMode }
                )
            }
        }
    }
}

@Composable
fun MainScreen(isDarkMode: Boolean, onToggleTheme: () -> Unit) {
    // 1 = Daily / Diurnal Log come landing page di default
    var selectedTab by remember { mutableIntStateOf(1) }
    
    // Dati attività condivisi
    var activities by remember {
        mutableStateOf(
            listOf(
                ActivityItem(dayOfWeek = DayOfWeek.MONDAY, title = "Analisi I", location = "Aula Volterra", startHour = 8, categoryColor = CatSageGreen),
                ActivityItem(dayOfWeek = DayOfWeek.MONDAY, title = "Algebra Lineare", location = "Aula Enriques", startHour = 10, categoryColor = CatGoldAmber),
                ActivityItem(dayOfWeek = DayOfWeek.MONDAY, title = "Fisica I", location = "Aula De Lollis", startHour = 14, categoryColor = CatWarmOrange)
            )
        )
    }

    val tabs = listOf("Thy Tasks", "Diurnal Log", "Almanack")
    val icons = listOf(
        Icons.Default.Check,
        Icons.Default.Edit,
        Icons.Default.DateRange
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (isDarkMode) OLEDSurface else Color.White
            ) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = title) },
                        label = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TasksScreenPlaceholder()
                1 -> DailyScreen(
                    isDarkMode = isDarkMode,
                    activities = activities,
                    onUpdateActivities = { updated -> activities = updated }
                )
                2 -> CalendarScreenPlaceholder()
            }
        }
    }
}

@Composable
fun TasksScreenPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Thy Toils & Tasks", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun CalendarScreenPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("The Grand Almanack of Time", style = MaterialTheme.typography.headlineMedium)
    }
}
