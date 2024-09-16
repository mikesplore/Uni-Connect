package com.mike.uniadmin.model.courses

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val courseCode: String,
    val courseName: String,
    val academicYears: List<AcademicYear> // This will be serialized/deserialized using the converter
) {
    constructor() : this("", "", emptyList())
}


@Entity(tableName = "academicYears")
data class AcademicYear(
    @PrimaryKey val year: String,
    val semesters: List<String> // List of semesters
) {
    constructor() : this("", emptyList())
}


