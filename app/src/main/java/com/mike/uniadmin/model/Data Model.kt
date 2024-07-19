package com.mike.uniadmin.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    NOTES, PAST_PAPERS, RESOURCES,
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

//these are the Bottom icons of the Bottom Nav

sealed class Screen(
    val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val name: String
) {
    data object Home : Screen(
        Icons.Filled.Home, Icons.Outlined.Home, "Home"
    )

    data object Timetable :
        Screen(Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday, "Events")

    data object Assignments : Screen(
        Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment, "Work"
    )

    data object Announcements : Screen(
        Icons.Filled.AddAlert, Icons.Outlined.AddAlert, "Alerts"
    )

    data object Attendance : Screen(
        Icons.Filled.Book, Icons.Outlined.Book, "Attendance"
    )

}

//random color for the course contents
val randomColor = listOf(
    Color(0xff00A9FF),
    Color(0xffE68369),
    Color(0xff009FBD),
    Color(0xffA34343),
    Color(0xff83A2FF),
    Color(0xff399918)

)


data class CourseAnnouncement(
    val announcementID: String = "",
    val date: String = "",
    val title: String = "",
    val description: String = "",
    val author: String = ""
)

data class CourseAssignment(
    val assignmentID: String = "",
    val title: String = "",
    val description: String = "",
    val publishedDate: String = "",
    val dueDate: String = ""
)

data class CourseTimetable(
    val timetableID: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val venue: String = "",
    val lecturer: String = "",
)

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




//upon course click, we will get the course code that
// will navigate us to the Course Screen
//using this course code, we will fetch the course details
//in the Courses Node(Course name and visits)
//using the same code, we will search the course content in the
// courseData Node (all the fields in the CourseAnnouncement)
//then display them in the UI

//defining our Node
//root directory will be CourseData
//under CourseData, we will have the course code as the key
//under this node we will have the course data contents as defined
// in our data class (CourseAnnouncement)
//CourseData -> Root Node
//CourseCode -> Key
//CourseAnnouncements -> Sub Node
//AnnouncementID ->Key
//Title
//Description
//Date
//Author

//the same logic applies to all other Sections of the Course
