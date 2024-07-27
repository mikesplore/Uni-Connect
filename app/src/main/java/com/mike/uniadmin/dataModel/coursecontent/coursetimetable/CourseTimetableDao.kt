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

    @Query("SELECT * FROM courseTimetable WHERE courseID = :courseID")
    suspend fun getCourseTimetables(courseID: String): List<CourseTimetable>
}
