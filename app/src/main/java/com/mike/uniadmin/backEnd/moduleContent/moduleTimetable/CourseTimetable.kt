package com.mike.uniadmin.backEnd.moduleContent.moduleTimetable

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moduleTimetable")
data class ModuleTimetable(
   @PrimaryKey val timetableID: String,
    val day: String = "",
    val moduleID: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val venue: String = "",
    val lecturer: String = "",
){
    constructor(): this("", "", "", "", "", "", "")
}
