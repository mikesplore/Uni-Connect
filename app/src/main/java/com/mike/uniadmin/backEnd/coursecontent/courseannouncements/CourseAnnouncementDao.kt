package com.mike.uniadmin.backEnd.coursecontent.courseannouncements

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseAnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseAnnouncement(announcement: CourseAnnouncement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseAnnouncements(courseAnnouncements: List<CourseAnnouncement>)

    @Query("SELECT * FROM courseAnnouncements WHERE courseID = :courseID")
    suspend fun getCourseAnnouncements(courseID: String): List<CourseAnnouncement>

    @Query("DELETE FROM courseAnnouncements WHERE courseID = :courseID")
    suspend fun clearAnnouncements(courseID: String)

}
