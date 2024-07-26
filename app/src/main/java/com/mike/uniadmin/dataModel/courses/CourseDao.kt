package com.mike.uniadmin.dataModel.courses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseDao{

    @Query("SELECT * FROM courses")
    suspend fun getCourses(): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(courses: List<CourseEntity>)

    @Query("DELETE FROM courses WHERE courseCode = :courseCode")
    suspend fun deleteCourse(courseCode: String)

    @Query("SELECT * FROM courses WHERE courseCode = :courseCode")
    suspend fun getCourse(courseCode: String): CourseEntity?


}