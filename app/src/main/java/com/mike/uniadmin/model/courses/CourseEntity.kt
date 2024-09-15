package com.mike.uniadmin.model.courses

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val courseCode: String,
    val courseName: String = "",
    var participants: List<String> = emptyList(),
    var courseImageLink: String = "",

){
    constructor(): this("", "", emptyList(), "")
}

@Entity(tableName = "courseStates")
data class CourseState(
    @PrimaryKey val courseID: String,
    val courseName: String = "",
    val state: Boolean = false
)
