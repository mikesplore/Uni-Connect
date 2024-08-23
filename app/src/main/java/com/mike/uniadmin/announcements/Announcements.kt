package com.mike.uniadmin.announcements

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.getAnnouncementViewModel
import com.mike.uniadmin.getNotificationViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(context: Context) {

    var addAnnouncement by rememberSaveable { mutableStateOf(false) }
    val announcementViewModel = getAnnouncementViewModel(context)
    val userViewModel = getUserViewModel(context)
    val notificationViewModel = getNotificationViewModel(context)

    val announcements by announcementViewModel.announcements.observeAsState()
    var refresh by remember { mutableStateOf(true) }
    var editingAnnouncementId by remember { mutableStateOf<String?>(null) }
    val announcementsLoading by announcementViewModel.isLoading.observeAsState()
    val userTypes = UniAdminPreferences.userType.value

    // Fetch announcements on refresh
    LaunchedEffect(refresh) {
        announcementViewModel.fetchAnnouncements()
        refresh = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, actions = {
                // Add Announcement button
                if (userTypes == "admin"){
                IconButton(onClick = { addAnnouncement = !addAnnouncement }) {
                    Icon(Icons.Default.Add, "Add", tint = CC.textColor())
                }}

                // Refresh button
                IconButton(onClick = { refresh = !refresh }) {
                    if (refresh) {
                        CircularProgressIndicator(color = CC.extraColor2())
                    }
                    Icon(Icons.Default.Refresh, "Refresh", tint = CC.textColor())
                }
            }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary()))
        }, containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Announcements",
                    style = CC.titleTextStyle(context)
                        .copy(fontSize = 30.sp, fontWeight = FontWeight.Bold)
                )
            }

            Text(
                "Tap to expand the announcement",
                style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(start = 20.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Add Announcement form (conditionally visible)
            AnimatedVisibility(visible = addAnnouncement) {
                AddAnnouncement(
                    context,
                    onComplete = { addAnnouncement = false },
                    userViewModel,
                    announcementViewModel,
                    notificationViewModel
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            when {
                announcementsLoading == true -> {
                    // Loading indicator
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CC.textColor())
                    }
                }

                announcements.isNullOrEmpty() -> {
                    // No announcements message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No announcements available", style = CC.descriptionTextStyle(context))
                    }
                }

                else -> {
                    // Display announcements list
                    LazyColumn(modifier = Modifier.fillMaxWidth(0.9f)) {
                        announcements?.let { announcements ->
                            val sortedAnnouncements =
                                announcements.sortedByDescending { sortedAnn -> sortedAnn.id }
                            items(sortedAnnouncements) { announcement ->
                                val isEditing = editingAnnouncementId == announcement.id
                                AnnouncementCard(
                                    announcement = announcement,
                                    onEdit = {
                                        editingAnnouncementId =
                                            if (isEditing) null else announcement.id
                                    },
                                    onDelete = { id ->
                                        announcementViewModel.deleteAnnouncement(id) { success ->
                                            if (success) {
                                                refresh = !refresh
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "Failed to delete Announcement",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    },
                                    context = context,
                                    isEditing = isEditing,
                                    onEditComplete = { editingAnnouncementId = null },
                                    announcementViewModel = announcementViewModel
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


