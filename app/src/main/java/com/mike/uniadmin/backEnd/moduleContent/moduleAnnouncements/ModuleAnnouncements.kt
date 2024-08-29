package com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moduleAnnouncements")
data class ModuleAnnouncement(
  @PrimaryKey val announcementID: String,
  val moduleID: String = "",
  val title: String = "",
  val date: String = "",
  val description: String = "",
  val authorID: String = "",
){
  constructor(): this("", "", "")

}

//fetched announcements will use this class
data class ModuleAnnouncementsWithAuthor(
  val announcementID: String,
  val moduleID: String = "",
  val title: String = "",
  val date: String = "",
  val authorID: String = "",
  val description: String = "",
  val profileImageLink: String = "",
  val authorName: String = ""
){
  constructor(): this("", "", "", "", "", "", "")
}