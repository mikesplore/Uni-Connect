package com.mike.uniadmin.model.modules

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ModuleDao {

    @Query("SELECT * FROM modules")
    suspend fun getModules(): List<ModuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<ModuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: ModuleEntity)

    @Query("DELETE FROM modules WHERE moduleCode = :moduleCode")
    suspend fun deleteModule(moduleCode: String)

    @Query("SELECT * FROM modules WHERE moduleCode = :moduleCode")
    suspend fun getModule(moduleCode: String): ModuleEntity?


}

@Dao
interface  AttendanceStateDao{
    @Query("SELECT * FROM attendanceStates")
    suspend fun getAttendanceStates(): List<AttendanceState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceStates(attendanceStates: List<AttendanceState>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceState(attendanceState: AttendanceState)
}
