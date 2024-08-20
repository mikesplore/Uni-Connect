package com.mike.uniadmin.backEnd.announcements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val date: String = "",
    val title: String = "",
    val description: String = "",
    val authorID: String = "",
    val authorName: String = "",
    val imageLink: String = ""
){
    constructor(): this("", "", "", "", "", "", "")
}

