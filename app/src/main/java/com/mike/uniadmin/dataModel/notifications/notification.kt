package com.mike.uniadmin.dataModel.notifications

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String? = null,
    val description: String? = null,
    val date: String? = null,
    val time: String? = null
){
    constructor():this(
        "",
        null,
        null,
        null,
        null
    )
}

