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

    @Query("""
        SELECT mt.*, m.moduleName 
        FROM ModuleTimetable mt 
        INNER JOIN modules m ON mt.moduleID = m.moduleCode 
        ORDER BY 
            CASE 
                WHEN mt.day = 'Monday' THEN 1 
                WHEN mt.day = 'Tuesday' THEN 2 
                WHEN mt.day = 'Wednesday' THEN 3 
                WHEN mt.day = 'Thursday' THEN 4 
                WHEN mt.day = 'Friday' THEN 5 
                WHEN mt.day = 'Saturday' THEN 6 
                WHEN mt.day = 'Sunday' THEN 7 
            END, 
            mt.startTime
    """)
    fun findNextClass(): List<ModuleTimetable>


}
