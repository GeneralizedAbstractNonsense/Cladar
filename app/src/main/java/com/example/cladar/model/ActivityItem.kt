package com.example.cladar.model

import androidx.compose.ui.graphics.Color
import com.example.cladar.ui.theme.*
import java.time.DayOfWeek

data class ActivityItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val dayOfWeek: DayOfWeek,
    val title: String,
    val location: String,
    val startHour: Int, // es. 8 per 08:00
    val durationHours: Int = 2,
    val categoryColor: Color = CatSageGreen
)
