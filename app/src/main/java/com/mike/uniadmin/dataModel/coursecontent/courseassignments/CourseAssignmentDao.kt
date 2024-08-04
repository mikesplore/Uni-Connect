package com.mike.uniadmin.dataModel.coursecontent.courseassignments


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseAssignment(assignment: CourseAssignment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseAssignments(courseAssignments: List<CourseAssignment>)

    @Query("DELETE FROM courseAssignments WHERE assignmentID = :assignmentID")
    suspend fun deleteCourseAssignments(assignmentID: String)


    @Query("SELECT * FROM courseAssignments WHERE courseCode = :courseID")
    suspend fun getCourseAssignments(courseID: String): List<CourseAssignment>

}