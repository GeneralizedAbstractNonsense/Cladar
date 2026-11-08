package com.example.cladar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.cladar.ui.theme.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    isDarkMode: Boolean,
    activities: List<ActivityItem>,
    onUpdateActivities: (List<ActivityItem>) -> Unit
) {
    
    var selectedDay by remember { mutableStateOf(LocalDate.now().dayOfWeek) }
    var showEditModal by remember { mutableStateOf(false) }

    
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    text = "Hark! Schedule for ${selectedDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH)}",
                    fontSize = 12.sp,
                    color = headerTextColor.copy(alpha = 0.7f)
                )
            }

           
            IconButton(
                onClick = { showEditModal = true },
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

        Spacer(modifier = Modifier.height(4.dp))

       
        val dayActivities = activities.filter { it.dayOfWeek == selectedDay }
        val locations = dayActivities.map { it.location }.distinct().take(3).ifEmpty {
            listOf("Chamber I", "Chamber II", "Chamber III")
        }

        TimetableGrid(
            locations = locations,
            activities = dayActivities,
            borderColor = borderColor,
            isDarkMode = isDarkMode
        )
    }

    
    if (showEditModal) {
        EditWeeklyScheduleModal(
            activities = activities,
            onDismiss = { showEditModal = false },
            onSave = { updatedList ->
                onUpdateActivities(updatedList)
                showEditModal = false
            }
        )
    }
}

@Composable
fun TimetableGrid(
    locations: List<String>,
    activities: List<ActivityItem>,
    borderColor: Color,
    isDarkMode: Boolean
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
                        .border(0.5.dp, borderColor.copy(alpha = 0.4f))
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
                            val activity = activities.find {
                                it.location == loc && it.startHour == hour
                            }

                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .fillMaxHeight()
                                    .padding(1.dp)
                                    .background(
                                        activity?.categoryColor ?: Color.Transparent,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        if (activity != null) 1.dp else 0.dp,
                                        borderColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (activity != null) {
                                    Column(
                                        modifier = Modifier.padding(2.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = activity.title,
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
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWeeklyScheduleModal(
    activities: List<ActivityItem>,
    onDismiss: () -> Unit,
    onSave: (List<ActivityItem>) -> Unit
) {
    val editableList = remember { mutableStateListOf(*activities.toTypedArray()) }
    var selectedDayToEdit by remember { mutableStateOf(DayOfWeek.MONDAY) }


    var newTitle by remember { mutableStateOf("") }
    var newLocation by remember { mutableStateOf("") }
    var newHour by remember { mutableStateOf("9") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(editableList) }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Amend Thy Weekly Schedule", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                
                ScrollableTabRow(selectedTabIndex = selectedDayToEdit.ordinal) {
                    DayOfWeek.values().forEach { d ->
                        Tab(
                            selected = selectedDayToEdit == d,
                            onClick = { selectedDayToEdit = d },
                            text = { Text(d.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Task / Activity Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = newLocation,
                        onValueChange = { newLocation = it },
                        label = { Text("Location") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = newHour,
                        onValueChange = { newHour = it },
                        label = { Text("Start Hour (8-19)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = {
                        val h = newHour.toIntOrNull() ?: 9
                        if (newTitle.isNotBlank() && newLocation.isNotBlank()) {
                            editableList.add(
                                ActivityItem(
                                    dayOfWeek = selectedDayToEdit,
                                    title = newTitle,
                                    location = newLocation,
                                    startHour = h,
                                    categoryColor = listOf(CatSageGreen, CatGoldAmber, CatWarmOrange, CatTeal).random()
                                )
                            )
                            newTitle = ""
                            newLocation = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Entry")
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

               
                LazyColumn(modifier = Modifier.height(150.dp)) {
                    val currentItems = editableList.filter { it.dayOfWeek == selectedDayToEdit }
                    items(currentItems.size) { index ->
                        val item = currentItems[index]
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.startHour}:00 - ${item.title} (${item.location})",
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { editableList.remove(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    )
}
