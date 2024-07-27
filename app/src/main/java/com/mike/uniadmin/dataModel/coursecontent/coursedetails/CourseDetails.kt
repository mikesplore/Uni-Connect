package com.mike.uniadmin.dataModel.coursecontent.coursedetails

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courseDetails")
data class CourseDetail(
    @PrimaryKey val detailID: String,
    val courseName: String? = null,
    val courseCode: String? = null,
    val lecturer: String? = null,
    val numberOfVisits: String? = null,
    val courseDepartment: String? = null,
    val overview: String? = null,
    val learningOutcomes: List<String> = emptyList(),
    val schedule: String? = null,
    val requiredMaterials: String? = null
){
    constructor(): this(
        "",
        null,
        null,
        null,
        null,
        null,
        null,
        emptyList(),
        null,
        null
    )

}