package com.example.cladar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Thy Toils", "Diurnal Log", "Almanack")
    val icons = listOf(
        Icons.Default.Check,
        Icons.Default.Edit,
        Icons.Default.Home
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
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
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (selectedTab) {
                0 -> TasksScreen()
                1 -> DailyScreen()
                2 -> CalendarScreen()
            }
        }
    }
}

@Composable
fun TasksScreen() {
    Text(text = "Hark! Here Lie Thy Toils", style = MaterialTheme.typography.headlineMedium)
}

@Composable
fun DailyScreen() {
    Text(text = "The Diurnal Chronicle", style = MaterialTheme.typography.headlineMedium)
}

@Composable
fun CalendarScreen() {
    Text(text = "The Grand Almanack of Time", style = MaterialTheme.typography.headlineMedium)
}
