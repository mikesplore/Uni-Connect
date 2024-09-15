package com.mike.uniadmin.model.moduleContent.moduleDetails

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mike.uniadmin.model.modules.ModuleEntity

@Dao
interface ModuleDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModuleDetail(moduleDetail: ModuleDetail)

    @Query("SELECT * FROM moduleDetails WHERE moduleCode = :moduleId LIMIT 1")
    suspend fun getModuleDetail(moduleId: String): ModuleDetail?

    @Query("SELECT moduleName, moduleCode, moduleImageLink, visits FROM modules WHERE moduleCode = :moduleId")
    suspend fun getModuleDetailsByID(moduleId: String): ModuleEntity?

    @Query("DELETE FROM moduleDetails WHERE detailID = :detailID")
    suspend fun deleteModuleDetail(detailID: String)
}
