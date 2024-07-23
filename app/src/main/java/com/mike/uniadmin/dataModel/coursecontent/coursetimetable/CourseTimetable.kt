package com.mike.uniadmin.dataModel.coursecontent.coursetimetable

data class CourseTimetable(
    val timetableID: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val venue: String = "",
    val lecturer: String = "",
)