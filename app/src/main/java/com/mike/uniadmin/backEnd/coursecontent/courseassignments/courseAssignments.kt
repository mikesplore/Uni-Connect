package com.mike.uniadmin.backEnd.coursecontent.courseassignments

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "courseAssignments")
data class CourseAssignment(
    @PrimaryKey  val assignmentID: String,
    val courseCode: String = "",
    val dueDate: String = "",
    val title: String = "",
    val description: String = "",
    val publishedDate: String = "",
    val authorID: String = ""
){
    constructor(): this("", "", "", "", "","", "")

}