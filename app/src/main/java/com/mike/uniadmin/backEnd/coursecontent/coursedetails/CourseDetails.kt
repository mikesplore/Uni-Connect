package com.mike.uniadmin.backEnd.coursecontent.coursedetails

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courseDetails")
data class CourseDetail(
    @PrimaryKey val detailID: String,
    val courseName: String = "",
    val courseCode: String = "",
    val lecturer: String = "",
    val numberOfVisits: String = "",
    val courseDepartment: String = "",
    val overview: String = "",
    val learningOutcomes: List<String> = emptyList(),
    val schedule: String = "",
    val requiredMaterials: String = ""
){
    constructor(): this("", "", "", "", "", "", "", emptyList(), "", "")
}