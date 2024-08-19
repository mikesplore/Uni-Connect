package com.mike.uniadmin.dataModel.programs

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "programs")
data class ProgramEntity(
    @PrimaryKey val programCode: String,
    val programName: String = "",
    var participants: Int = 0,
    var programImageLink: String = ""

){
    constructor(): this("", "", 0, "")
}

@Entity(tableName = "programCode")
data class Program(
    @PrimaryKey val id: String = "ProgramCode",
    val programCode: String = ""
)


@Entity(tableName = "programStates")
data class ProgramState(
    @PrimaryKey val programID: String,
    val programName: String = "",
    val state: Boolean = false
){
    constructor(): this("", "", false)
}
