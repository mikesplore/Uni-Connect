package com.mike.uniadmin.dataModel.coursecontent.coursetimetable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courseTimetable")
data class CourseTimetable(
   @PrimaryKey val timetableID: String,
    val day: String = "",
    val courseID: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val venue: String = "",
    val lecturer: String = "",
){
    constructor(): this("", "", "", "", "", "", "")
}
