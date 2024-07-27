package com.mike.uniadmin.dataModel.coursecontent.courseassignments

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "courseAssignments")
data class CourseAssignment(
    @PrimaryKey  val assignmentID: String,
    val courseCode: String? = null,
    val dueDate: String? = null,
    val title: String? = null,
    val description: String? = null,
    val publishedDate: String? = null
){
    constructor(): this(
        "",
        "",
        "",
        "",
        ""
    )
}