package com.mike.uniadmin.attendance

import android.content.Context
import androidx.compose.runtime.Composable
import com.mike.uniadmin.UniAdminPreferences

@Composable
fun AttendanceScreen(context: Context) {
    val userTypes = UniAdminPreferences.userType.value
    if (userTypes == "admin") {
        ManageAttendanceScreen(context)
    } else {
        SignAttendance(context)
    }
}