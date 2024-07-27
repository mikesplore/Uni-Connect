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




data class GridItem(
    val title: String = "",
    val description: String = "",
    val fileLink: String = "",
    val imageLink: String = "",
)

enum class Section {
    NOTES, PAST_PAPERS, RESOURCES,
}




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


data class AttendanceRecord(
    val studentId: String = "",
    val dayOfWeek: String = "",
    val isPresent: Boolean = false,
    val lesson: String = ""
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



data class MyCode(
    val id: String = UUID.randomUUID().toString(),
    var code: Int = 0
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

// Data structure employed here is Hierarchical🌲 data structure
//So the path to the courseAnnouncements data will be
//CourseContent -> CourseCode -> CourseAnnouncements  -> AnnouncementID -> Announcement Content

//the same logic applies to all other Sections of the Course
