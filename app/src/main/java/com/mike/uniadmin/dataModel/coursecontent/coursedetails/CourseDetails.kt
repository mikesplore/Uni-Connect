package com.mike.uniadmin.dataModel.coursecontent.coursedetails

data class CourseDetails(
    val detailsID: String = "",
    val courseName: String = "",
    val courseCode: String = "",
    val lecturer: String = "",
    val numberOfVisits: String = "",
    val courseDepartment: String = "",
    val overview: String = "",
    val learningOutcomes: List<String> = emptyList(),
    val schedule: String = "",
    val requiredMaterials: String = ""
)