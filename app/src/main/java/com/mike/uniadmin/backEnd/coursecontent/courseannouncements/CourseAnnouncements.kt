package com.mike.uniadmin.backEnd.coursecontent.courseannouncements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courseAnnouncements")
data class CourseAnnouncement(
  @PrimaryKey val announcementID: String,
  val courseID: String = "",
  val title: String = "",
  val content: String = "",
  val date: String = "",
  val author: String = "",
  val description: String = ""
){
  constructor(): this("", "", "", "", "", "", "")

}