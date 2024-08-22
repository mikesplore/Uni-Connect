package com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moduleAnnouncements")
data class ModuleAnnouncement(
  @PrimaryKey val announcementID: String,
  val moduleID: String = "",
  val title: String = "",
  val content: String = "",
  val date: String = "",
  val author: String = "",
  val description: String = ""
){
  constructor(): this("", "", "", "", "", "", "")

}