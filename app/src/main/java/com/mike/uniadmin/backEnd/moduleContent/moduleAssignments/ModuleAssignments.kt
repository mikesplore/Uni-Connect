package com.mike.uniadmin.backEnd.moduleContent.moduleAssignments

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "moduleAssignments")
data class ModuleAssignment(
    @PrimaryKey  val assignmentID: String,
    val moduleCode: String = "",
    val dueDate: String = "",
    val title: String = "",
    val description: String = "",
    val publishedDate: String = "",
    val authorID: String = ""
){
    constructor(): this("", "", "", "", "","", "")

}