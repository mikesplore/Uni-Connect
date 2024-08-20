package com.mike.uniadmin.backEnd.programs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProgramDao {

    @Query("SELECT * FROM programs")
    suspend fun getPrograms(): List<ProgramEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<ProgramEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: ProgramEntity)

    @Query("DELETE FROM programs WHERE programCode = :programCode")
    suspend fun deleteProgram(programCode: String)

    @Query("SELECT * FROM programs WHERE programCode = :programCode")
    suspend fun getProgram(programCode: String): ProgramEntity?

}

@Dao
interface  ProgramStateDao{
    @Query("SELECT * FROM programStates")
    suspend fun getProgramStates(): List<ProgramState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramStates(programStates: List<ProgramState>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgramState(programState: ProgramState)
}
