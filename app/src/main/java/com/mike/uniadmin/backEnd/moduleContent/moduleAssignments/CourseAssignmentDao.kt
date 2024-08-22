package com.mike.uniadmin.backEnd.moduleContent.moduleAssignments


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ModuleAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleAssignment(assignment: ModuleAssignment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleAssignments(moduleAssignments: List<ModuleAssignment>)

    @Query("DELETE FROM moduleAssignments WHERE assignmentID = :assignmentID")
    suspend fun deleteModuleAssignments(assignmentID: String)


    @Query("SELECT * FROM moduleAssignments WHERE moduleCode = :moduleID")
    suspend fun getModuleAssignments(moduleID: String): List<ModuleAssignment>

}