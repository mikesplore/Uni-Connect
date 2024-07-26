package com.mike.uniadmin.dataModel.coursecontent.courseannouncements

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "courseAnnouncements")
data class CourseAnnouncement(
  @PrimaryKey  val announcementID: String,
    val date: String? = null,
    val title: String? = null,
    val description: String? = null,
    val author: String? = null
){
  constructor(): this(
    "",
    "",
    "",
    "",
    ""
  )
}