package com.mike.uniadmin.model.modules

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey val moduleCode: String,
    val moduleName: String = "",
    var visits: Int = 0,
    var moduleImageLink: String = ""

){
    constructor(): this("", "", 0, "")
}


@Entity(tableName = "attendanceStates")
data class AttendanceState(
    @PrimaryKey val moduleID: String,
    val moduleName: String = "",
    val state: Boolean = false
){
    constructor(): this("", "", false)
}
