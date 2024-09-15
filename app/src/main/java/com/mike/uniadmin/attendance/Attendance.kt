package com.mike.uniadmin.attendance

import android.content.Context
import androidx.compose.runtime.Composable
import com.mike.uniadmin.UniConnectPreferences

@Composable
fun AttendanceScreen(context: Context) {
    val userTypes = UniConnectPreferences.userType.value
    if (userTypes == "admin") {
        ManageAttendanceScreen(context)
    } else {
        SignAttendance(context)
    }
}