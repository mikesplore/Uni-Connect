package com.mike.uniadmin.model.courses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses")
    suspend fun getCourses(): List<CourseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE courseCode = :courseCode")
    suspend fun deleteCourse(courseCode: String)

    @Query("SELECT * FROM courses WHERE courseCode = :courseCode")
    suspend fun getCourse(courseCode: String): CourseEntity?

}

@Dao
interface  CourseStateDao{
    @Query("SELECT * FROM courseStates")
    suspend fun getCourseStates(): List<CourseState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseStates(courseStates: List<CourseState>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseState(courseState: CourseState)
}
