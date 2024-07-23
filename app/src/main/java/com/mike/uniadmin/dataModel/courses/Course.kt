package com.mike.uniadmin.dataModel.courses

data class Course(
    val courseCode: String = "",
    val courseName: String = "",
    var visits: Int = 0,
    var courseImageLink: String = ""

)
