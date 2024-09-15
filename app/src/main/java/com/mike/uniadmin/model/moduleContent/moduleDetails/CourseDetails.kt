package com.mike.uniadmin.model.moduleContent.moduleDetails

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moduleDetails")
data class ModuleDetail(
    @PrimaryKey val detailID: String,
    val moduleName: String = "",
    val moduleCode: String = "",
    val lecturer: String = "",
    val numberOfVisits: String = "",
    val moduleDepartment: String = "",
    val overview: String = "",
    val learningOutcomes: List<String> = emptyList(),
    val schedule: String = "",
    val requiredMaterials: String = ""
){
    constructor(): this("", "", "", "", "", "", "", emptyList(), "", "")
}