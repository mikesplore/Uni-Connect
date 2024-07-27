package com.mike.uniadmin.dataModel.coursecontent.coursetimetable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courseTimetable")
data class CourseTimetable(
   @PrimaryKey val timetableID: String,
    val day: String? = null,
    val courseID: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val venue: String? = null,
    val lecturer: String? = null,
)
{
    constructor(): this(
        "",
        null,
        null,
        null,
        null
    )
}