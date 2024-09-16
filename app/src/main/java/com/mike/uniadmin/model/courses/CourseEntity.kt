package com.mike.uniadmin.model.courses

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val courseCode: String,
    val courseName: String,
    val academicYears: List<AcademicYear> // This will be serialized/deserialized using the converter
) {
    constructor() : this("", "", emptyList())
}


@Entity(tableName = "enrollments",
    foreignKeys = [ForeignKey(
        entity = Course::class,
        parentColumns = ["courseCode"],
        childColumns = ["courseCode"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["courseCode"])] // Add index for courseCode
)
data class Enrollment(
    @PrimaryKey(autoGenerate = true) val enrollmentId: Int = 0,
    val userId: String, // to identify which user is enrolled
    val courseCode: String,
    val enrollmentYear: String // e.g., 2024-2025
)

@Entity(tableName = "academicYears")
data class AcademicYear(
    @PrimaryKey val year: String,
    val semesters: List<String> // List of semesters
) {
    constructor() : this("", emptyList())
}

data class CourseWithEnrollment(
    val courseCode: String,
    val courseName: String,
    val enrollmentYear: String
) {
    constructor(course: Course, enrollment: Enrollment) : this(
        courseCode = course.courseCode,
        courseName = course.courseName,
        enrollmentYear = enrollment.enrollmentYear
    )
}

