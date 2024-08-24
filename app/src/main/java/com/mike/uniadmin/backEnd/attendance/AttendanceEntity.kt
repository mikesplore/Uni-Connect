package com.mike.uniadmin.backEnd.attendance

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val record: String,
    val studentId: String,
    val moduleId: String,
    val date: String,
) {
    constructor() : this("", "", "", "", "")
}
