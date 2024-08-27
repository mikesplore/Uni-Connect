package com.mike.uniadmin.backEnd.moduleContent.moduleTimetable


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ModuleTimetableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleTimetable(timetable: ModuleTimetable)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleTimetables(moduleTimetables: List<ModuleTimetable>)

    @Query("DELETE FROM moduleTimetable WHERE timetableID = :timetableID")
    suspend fun deleteModuleTimetable(timetableID: String)

    @Query("SELECT * FROM moduleTimetable WHERE moduleID = :moduleID")
    suspend fun getModuleTimetables(moduleID: String): List<ModuleTimetable>

    @Query("SELECT * FROM moduleTimetable")
    suspend fun getAllModuleTimetables(): List<ModuleTimetable>

    @Query(
        """
        SELECT mt.*, m.moduleName
        FROM moduleTimetable mt
        INNER JOIN modules m ON mt.moduleId = m.moduleCode
        WHERE mt.day = :day
    """
    )
    suspend fun getTimetableByDay(day: String): List<ModuleTimetable>


}
