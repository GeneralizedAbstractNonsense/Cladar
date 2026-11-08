package com.example.cladar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cladar.model.ActivityItem
import com.example.cladar.model.EventItem
import com.example.cladar.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DailyScreen(
    isDarkMode: Boolean,
    activities: List<ActivityItem>,
    events: List<EventItem>,
    onEventClick: (EventItem) -> Unit,
    onOpenEditModal: () -> Unit
) {
    var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfWeek) }
    val todayDateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val bgColor = if (isDarkMode) OLEDBlack else GridBackgroundLight
    val borderColor = if (isDarkMode) OLEDBorder else GridBorderLight
    val headerTextColor = if (isDarkMode) Color.White else BurgundyTitle

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "The Diurnal Chronicle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = headerTextColor
                )
                Text(
                    text = "Hark! View for ${selectedDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}",
                    fontSize = 12.sp,
                    color = headerTextColor.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = onOpenEditModal,
                modifier = Modifier
                    .size(36.dp)
                    .background(headerTextColor.copy(alpha = 0.1f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Amend Schedule",
                    tint = headerTextColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedDay.ordinal,
            edgePadding = 8.dp,
            containerColor = Color.Transparent,
            contentColor = BurgundyTitle
        ) {
            DayOfWeek.values().forEach { day ->
                Tab(
                    selected = selectedDay == day,
                    onClick = { selectedDay = day },
                    text = {
                        Text(
                            text = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                            fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        val dayActivities = activities.filter { it.dayOfWeek == selectedDay }
        val dayEvents = events.filter { it.date == todayDateStr }
        val locations = (dayActivities.map { it.location } + dayEvents.map { it.location })
            .distinct().take(3).ifEmpty { listOf("Chamber I", "Chamber II", "Chamber III") }

        TimetableGrid(
            locations = locations,
            activities = dayActivities,
            events = dayEvents,
            borderColor = borderColor,
            isDarkMode = isDarkMode,
            onEventClick = onEventClick
        )
    }
}

@Composable
fun TimetableGrid(
    locations: List<String>,
    activities: List<ActivityItem>,
    events: List<EventItem>,
    borderColor: Color,
    isDarkMode: Boolean,
    onEventClick: (EventItem) -> Unit
) {
    val hours = (8..19).toList()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isDarkMode) OLEDSurface else ColumnHeaderBg)
                .border(1.dp, borderColor)
        ) {
            Text(
                text = "Time",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(55.dp)
                    .padding(8.dp)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScrollState)
            ) {
                locations.forEach { loc ->
                    Text(
                        text = loc,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BurgundyTitle,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .width(110.dp)
                            .padding(8.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
        ) {
            hours.forEach { hour ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(0.5.dp, borderColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = String.format("%02d:00", hour),
                        fontSize = 10.sp,
                        color = if (isDarkMode) Color.LightGray else Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(55.dp)
                            .padding(top = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        locations.forEach { loc ->
                            // Controlla se c'è un evento dal calendario sovrapposto
                            val calEvent = events.find {
                                it.location == loc && it.time.startsWith(String.format("%02d", hour))
                            }
                            val act = activities.find { it.location == loc && it.startHour == hour }

                            val title = calEvent?.title ?: act?.title
                            val bgColor = calEvent?.let { Color(it.colorHex) }
                                ?: act?.let { Color(it.colorHex) }
                                ?: Color.Transparent

                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .fillMaxHeight()
                                    .padding(1.dp)
                                    .background(bgColor, RoundedCornerShape(4.dp))
                                    .clickable(enabled = calEvent != null) {
                                        calEvent?.let { onEventClick(it) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (title != null) {
                                    Text(
                                        text = title,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
