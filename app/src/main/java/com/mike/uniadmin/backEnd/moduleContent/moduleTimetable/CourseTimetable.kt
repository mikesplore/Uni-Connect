package com.mike.uniadmin.backEnd.moduleContent.moduleTimetable

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "moduleTimetable")
data class ModuleTimetable(
   @PrimaryKey val timetableID: String,
    val day: String = "",
    val moduleID: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val venue: String = "",
    val lecturer: String = "",
    val moduleName: String = ""
){
    constructor(): this("", "", "", "", "", "", "","")
}

//helper functions for determining the upcoming timetable


fun getDayIndex(day: String): Int {
    return when (day.lowercase()) {
        "monday" -> 1
        "tuesday" -> 2
        "wednesday" -> 3
        "thursday" -> 4
        "friday" -> 5
        "saturday" -> 6
        "sunday" -> 7
        else -> 0 // Invalid day
    }
}


fun getUpcomingClass(timetables: List<ModuleTimetable>): ModuleTimetable? {
    val currentDayIndex = LocalDate.now().dayOfWeek.value // Monday = 1, Sunday = 7
    val currentTime = LocalTime.now()

    // Map timetables to day indices and filter out past classes
    val filteredTimetables = timetables.map { timetable ->
        timetable to getDayIndex(timetable.day)
    }.filter { (timetable, dayIndex) ->
        if (dayIndex > currentDayIndex) {
            true
        } else if (dayIndex == currentDayIndex) {
            // Only include classes that haven't started yet today
            val classTime = LocalTime.parse(timetable.startTime, DateTimeFormatter.ofPattern("HH:mm"))
            classTime.isAfter(currentTime)
        } else {
            false
        }
    }

    // Sort timetables by the closest upcoming day and start time
    val sortedTimetables = filteredTimetables.sortedWith(compareBy(
        { (_, dayIndex) -> if (dayIndex < currentDayIndex) dayIndex + 7 else dayIndex },
        { (timetable, _) -> LocalTime.parse(timetable.startTime, DateTimeFormatter.ofPattern("HH:mm")) }
    ))

    // Return the next upcoming class
    return sortedTimetables.firstOrNull()?.first
}

