package com.example.cladar.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.util.UUID

data class EventItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:mm
    val location: String,
    val colorHex: Int,
    val notesMarkdown: String = ""
)

data class ActivityItem(
    val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: DayOfWeek,
    val title: String,
    val location: String,
    val startHour: Int,
    val colorHex: Int
)

class StorageManager(context: Context) {
    private val prefs = context.getSharedPreferences("cladar_prefs", Context.MODE_PRIVATE)

    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
    }

    fun loadDarkMode(): Boolean = prefs.getBoolean("is_dark_mode", false)

    fun saveEvents(events: List<EventItem>) {
        val array = JSONArray()
        events.forEach { e ->
            val obj = JSONObject().apply {
                put("id", e.id)
                put("title", e.title)
                put("date", e.date)
                put("time", e.time)
                put("location", e.location)
                put("colorHex", e.colorHex)
                put("notesMarkdown", e.notesMarkdown)
            }
            array.put(obj)
        }
        prefs.edit().putString("events_json", array.toString()).apply()
    }

    fun loadEvents(): List<EventItem> {
        val jsonStr = prefs.getString("events_json", null) ?: return emptyList()
        val list = mutableListOf<EventItem>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                EventItem(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    date = obj.getString("date"),
                    time = obj.getString("time"),
                    location = obj.getString("location"),
                    colorHex = obj.getInt("colorHex"),
                    notesMarkdown = obj.optString("notesMarkdown", "")
                )
            )
        }
        return list
    }

    fun saveActivities(activities: List<ActivityItem>) {
        val array = JSONArray()
        activities.forEach { a ->
            val obj = JSONObject().apply {
                put("id", a.id)
                put("dayOfWeek", a.dayOfWeek.name)
                put("title", a.title)
                put("location", a.location)
                put("startHour", a.startHour)
                put("colorHex", a.colorHex)
            }
            array.put(obj)
        }
        prefs.edit().putString("activities_json", array.toString()).apply()
    }

    fun loadActivities(): List<ActivityItem> {
        val jsonStr = prefs.getString("activities_json", null) ?: return emptyList()
        val list = mutableListOf<ActivityItem>()
        val array = JSONArray(jsonStr)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ActivityItem(
                    id = obj.getString("id"),
                    dayOfWeek = DayOfWeek.valueOf(obj.getString("dayOfWeek")),
                    title = obj.getString("title"),
                    location = obj.getString("location"),
                    startHour = obj.getInt("startHour"),
                    colorHex = obj.getInt("colorHex")
                )
            )
        }
        return list
    }
}
