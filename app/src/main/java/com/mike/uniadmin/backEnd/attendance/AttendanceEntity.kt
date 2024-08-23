package com.mike.uniadmin.backEnd.attendance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val courseId: String,
    val date: String,
    val status: String

){
    constructor() : this("", "", "","", "")

}