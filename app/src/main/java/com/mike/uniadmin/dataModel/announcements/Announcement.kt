package com.mike.uniadmin.dataModel.announcements

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.mike.uniadmin.dataModel.users.UserEntity

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val date: String? = null,
    val title: String? = null,
    val description: String? = null,
    val authorID: String? = null,
    val authorName: String? = null,
    val imageLink: String? = null
){
    constructor(): this(
        "",
        null,
        null,
        null,
        null,
        null
    )
}

