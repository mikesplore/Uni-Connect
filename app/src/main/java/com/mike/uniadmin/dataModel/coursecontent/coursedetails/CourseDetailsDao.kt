package com.mike.uniadmin.dataModel.coursecontent.coursedetails

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mike.uniadmin.dataModel.courses.CourseEntity

@Dao
interface CourseDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseDetail(courseDetail: CourseDetail)

    @Query("SELECT * FROM courseDetails WHERE courseCode = :courseId LIMIT 1")
    suspend fun getCourseDetail(courseId: String): CourseDetail?

    @Query("SELECT courseName, courseCode, courseImageLink, visits FROM courses WHERE courseCode = :courseId")
    suspend fun getCourseDetailsByID(courseId: String): CourseEntity?
}
