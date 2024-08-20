package com.mike.uniadmin.backEnd.programs

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "programs")
data class ProgramEntity(
    @PrimaryKey val programCode: String,
    val programName: String = "",
    var participants: List<String> = emptyList(),
    var programImageLink: String = ""

){
    constructor(): this("", "", emptyList(), "")
}


@Entity(tableName = "programStates")
data class ProgramState(
    @PrimaryKey val programID: String,
    val programName: String = "",
    val state: Boolean = false
){
    constructor(): this("", "", false)
}
