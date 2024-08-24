package com.mike.uniadmin.helperFunctions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Book
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

data class Feedback(
    val id: String = "",
    val rating: Int = 0,
    val message: String = "",
    val sender: String = "",
    val admissionNumber: String = ""
)

data class Attendance(
    val id: String = "",
    val date: String = "",
    val status: String = "",
    val studentId: String = ""
)


data class Fcm(
    val id: String = "",
    val token: String = "",
    val userId: String = ""
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

//random color for the module contents
val randomColor = listOf(
    Color(0xFF00A9FF),
    Color(0xFFE68369),
    Color(0xFF009FBD),
    Color(0xFFA34343),
    Color(0xFF83A2FF),
    Color(0xFF399918),
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFF4CAF50), // Green
    Color(0xFFFF5722), // Deep Orange
    Color(0xFF795548), // Brown
    Color(0xFF607D8B), // Blue Grey
    Color(0xFFE91E63), // Pink
)



