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
interface EnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: Enrollment)

    @Query("SELECT * FROM enrollments WHERE userId = :userId")
    suspend fun getUserEnrollment(userId: String): Enrollment?

    @Query("DELETE FROM enrollments WHERE userId = :userId")
    suspend fun deleteEnrollment(userId: String)

    @Query("""
        SELECT c.courseCode, c.courseName, e.enrollmentYear
        FROM courses c
        INNER JOIN enrollments e ON c.courseCode = e.courseCode
        WHERE e.userId = :userId
    """)
    suspend fun getUserEnrolledCourse(userId: String): CourseWithEnrollment?

    @Query("SELECT * FROM enrollments")
    suspend fun getAllEnrollments(): List<Enrollment>
}



@Dao
interface AcademicYearDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicYear(academicYear: AcademicYear)

    @Query("SELECT * FROM academicYears")
    suspend fun getAllAcademicYears(): List<AcademicYear>

}