package com.mike.uniadmin.dataModel.coursecontent.coursetimetable



import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CourseTimetableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseTimetable(timetable: CourseTimetable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourseTimetables(courseTimetables: List<CourseTimetable>)

    @Query("DELETE FROM courseTimetable WHERE timetableID = :timetableID")
    suspend fun deleteCourseTimetable(timetableID: String)

    @Query("SELECT * FROM courseTimetable WHERE courseID = :courseID")
    suspend fun getCourseTimetables(courseID: String): List<CourseTimetable>

    @Query("SELECT * FROM courseTimetable")
    suspend fun getAllCourseTimetables(): List<CourseTimetable>

    @Query("SELECT * FROM courseTimetable WHERE day = :day")
    suspend fun getCourseTimetablesByDay(day: String): List<CourseTimetable>?
}
