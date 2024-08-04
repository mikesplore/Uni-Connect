package com.mike.uniadmin.dataModel.notifications

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String = "",
    val name:String = "",
    val userId: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = ""
){
    constructor(): this("", "", "", "", "", "", "")
}
