package com.mike.uniadmin.dataModel.coursecontent.courseannouncements

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courseAnnouncements")
data class CourseAnnouncement(
  @PrimaryKey val announcementID: String,
  val courseID: String? = null,
  val title: String? = null,
  val content: String? = null,
  val date: String? = null,
  val author: String? = null,
  val description: String? = null
)
{
  constructor(): this (
    "",
    null,
    null,
    null,
    null,
    null,
    null
  )

}