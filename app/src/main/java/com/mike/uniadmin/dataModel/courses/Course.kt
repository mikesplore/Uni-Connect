package com.mike.uniadmin.dataModel.courses

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val courseCode: String,
    val courseName: String? = null,
    var visits: Int? = null,
    var courseImageLink: String? = null

){
    constructor(): this(
        "",
        "",
        0,
        ""
    )
}


@Entity(tableName = "attendanceStates")
data class AttendanceState(
    @PrimaryKey val courseID: String,
    val courseName: String? = null,
    val state: Boolean = false
)
{
    constructor(): this(
        "",
        "",
        false
    )
}