package com.mike.uniadmin.settings

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.users.UserPreferencesEntity
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase.generateSharedPreferencesID
import com.mike.uniadmin.ui.theme.CommonComponents

@Composable
fun DarkMode(context: Context) {
    val icon = if (UniAdminPreferences.darkMode.value) Icons.Filled.ModeNight else Icons.Filled.WbSunny
    val iconDescription =
        if (UniAdminPreferences.darkMode.value) "Switch to Dark Mode" else "Switch to Light Mode"
    BoxWithConstraints {
        val columnWidth = maxWidth
        val iconSize = columnWidth * 0.10f

        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(iconSize),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .background(CommonComponents.secondary(), CircleShape)
                    .size(iconSize),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = CommonComponents.extraColor2()
                )
            }
            Text("Dark Mode", style = CommonComponents.descriptionTextStyle(context), fontSize = 20.sp)
            Switch(
                onCheckedChange = {
                    UniAdminPreferences.darkMode.value = it
                    UniAdminPreferences.saveDarkModePreference(it)

                },
                checked = UniAdminPreferences.darkMode.value,
                colors = switchColors(),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun Notifications(context: Context) {
    var isNotificationEnabled by remember { mutableStateOf(UniAdminPreferences.notificationsEnabled.value) }
    val icon =
        if (isNotificationEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff
    val iconDescription =
        if (isNotificationEnabled) "Enable Notifications" else "Disable Notifications"


    BoxWithConstraints {
        val rowWidth = maxWidth
        val rowHeight = rowWidth * 0.1f
        val iconSize = rowWidth * 0.10f

        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(rowHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .background(CommonComponents.secondary(), CircleShape)
                    .size(iconSize),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = CommonComponents.extraColor2()
                )
            }
            Text("Notifications", style = CommonComponents.descriptionTextStyle(context), fontSize = 20.sp)
            Switch(
                onCheckedChange = { notifications ->
                    if (!notifications) {
                        (context as MainActivity).checkAndRequestNotificationPermission()

                    }
                    isNotificationEnabled = notifications
                },
                checked = isNotificationEnabled,
                colors = switchColors(),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}