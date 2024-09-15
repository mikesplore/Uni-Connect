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
    val id: String = "",
    val title: String = "",
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

    data object Announcements : Screen(
        Icons.Filled.AddAlert, Icons.Outlined.AddAlert, "Alerts"
    )

    data object Attendance : Screen(
        Icons.Filled.Book, Icons.Outlined.Book, "Attendance"
    )

}

//random color for the module contents
val randomColor = listOf(
    Color(0xFF164863),
    Color(0xFF427D9D),
    Color(0xFF009FBD),
    Color(0xFF008170),
    Color(0xFF205295),
    Color(0xFF2C74B3),

)



