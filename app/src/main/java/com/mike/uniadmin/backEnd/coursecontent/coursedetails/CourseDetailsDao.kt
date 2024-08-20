package com.mike.uniadmin.backEnd.coursecontent.coursedetails

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mike.uniadmin.backEnd.courses.CourseEntity

@Dao
interface CourseDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseDetail(courseDetail: CourseDetail)

    @Query("SELECT * FROM courseDetails WHERE courseCode = :courseId LIMIT 1")
    suspend fun getCourseDetail(courseId: String): CourseDetail?

    @Query("SELECT courseName, courseCode, courseImageLink, visits FROM courses WHERE courseCode = :courseId")
    suspend fun getCourseDetailsByID(courseId: String): CourseEntity?

    @Query("DELETE FROM courseDetails WHERE detailID = :detailID")
    suspend fun deleteCourseDetail(detailID: String)
}
