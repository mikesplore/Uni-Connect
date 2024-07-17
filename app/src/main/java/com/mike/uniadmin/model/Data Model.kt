package com.mike.uniadmin.model

import java.util.UUID



data class User(
    var id: String = "", // Use a mutable 'var'
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val profileImageLink: String = ""
)

data class GridItem(
    val title: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val link: String = "",
    var fileType: String = "image"
)

enum class Section {
    NOTES, PAST_PAPERS, RESOURCES
}

data class AccountDeletion(
    val id: String = "", val admissionNumber: String = "", val email: String = ""
)

data class Message(
    var id: String = "",
    var message: String = "",
    var senderName: String = "",
    var senderID: String = "",
    var time: String = "",
    var date: String = "",
    var recipientID: String = "",
    var profileImageLink: String = ""

)

data class Timetable(
    val id: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val unitName: String = "",
    val venue: String = "",
    val lecturer: String = "",
    val dayId: String = ""
)

data class Feedback(
    val id: String = "",
    val rating: Int = 0,
    val message: String = "",
    val sender: String = "",
    val admissionNumber: String = ""
)

data class Student(
    val id: String = "",
    val firstName: String
)

data class AttendanceRecord(
    val studentId: String = "",
    val dayOfWeek: String = "",
    val isPresent: Boolean = false,
    val lesson: String = ""
)

data class AttendanceState(
    val courseID: String = "",
    val courseName: String = "",
    val state: Boolean = false
)


data class Assignment(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val dueDate: String = "",
    val courseCode: String = ""
)

data class Day(
    val id: String = "",
    val name: String = ""
)

data class Announcement(
    val id: String = "",
    val date: String = "",
    val title: String = "",
    val description: String = "",
    val author: String = ""
)

data class Fcm(
    val id: String = "",
    val token: String = ""
)

data class Course(
    val courseCode: String = "",
    val courseName: String = "",
    var visits: Int = 0

)

data class ScreenTime(
    val id: String = "", val screenName: String = "", val time: Long = 0
)

data class Screens(
    val screenId: String = "",
    val screenName: String = "",

    )

data class Chat(
    var id: String = "",
    var message: String = "",
    var senderName: String = "",
    var senderID: String = "",
    var time: String = "",
    var date: String = "",
    var profileImageLink: String = ""

)

data class MyCode(
    val id: String = UUID.randomUUID().toString(),
    var code: Int = 0
)

data class UserPreferences(
    val studentID: String = "",
    val id: String = "",
    val profileImageLink: String = "",
    val biometrics: String = "disabled",
    val darkMode: String = "disabled",
    val notifications: String = "disabled"

)