package com.mike.uniadmin.model.courses

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Query("SELECT * FROM courses")
    suspend fun getAllCourses(): List<Course>

    @Query("DELETE FROM courses WHERE courseCode = :courseCode")
    suspend fun deleteCourse(courseCode: String)
}


@Dao
interface AcademicYearDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicYear(academicYear: AcademicYear)

    @Query("SELECT * FROM academicYears")
    suspend fun getAllAcademicYears(): List<AcademicYear>

}