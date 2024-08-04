package com.mike.uniadmin.dataModel.courses

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val courseCode: String,
    val courseName: String = "",
    var visits: Int = 0,
    var courseImageLink: String = ""

){
    constructor(): this("", "", 0, "")
}


@Entity(tableName = "attendanceStates")
data class AttendanceState(
    @PrimaryKey val courseID: String,
    val courseName: String = "",
    val state: Boolean = false
){
    constructor(): this("", "", false)
}
