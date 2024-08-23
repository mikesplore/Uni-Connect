package com.mike.uniadmin.announcements

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mike.uniadmin.backEnd.announcements.AnnouncementEntity
import com.mike.uniadmin.backEnd.announcements.AnnouncementViewModel
import com.mike.uniadmin.ui.theme.CommonComponents

@Composable
fun EditAnnouncement(
    context: Context,
    onComplete: () -> Unit,
    announcement: AnnouncementEntity,
    announcementViewModel: AnnouncementViewModel
) {
    // State variables to hold the title and description of the announcement being edited
    var title by remember { mutableStateOf(announcement.title) }
    var description by remember { mutableStateOf(announcement.description) }

    Column(
        modifier = Modifier
            .imePadding()
            .border(1.dp, CommonComponents.extraColor2(), RoundedCornerShape(10.dp))
            .fillMaxWidth(0.9f), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title of the editing section
        Row(
            modifier = Modifier.height(50.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Edit Announcement",
                style = CommonComponents.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Display the profile image and current date
        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile image or author's initial
            Box(
                modifier = Modifier
                    .border(1.dp, CommonComponents.textColor(), CircleShape)
                    .clip(CircleShape)
                    .background(CommonComponents.secondary(), CircleShape)
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (announcement.imageLink.isNotEmpty()) {
                    // Load the profile image if the link is available
                    AsyncImage(
                        model = announcement.imageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Show the initial of the author's name if no image is available
                    Text(
                        "${announcement.authorName[0]}",
                        style = CommonComponents.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            // Display the current date
            Text(CommonComponents.getCurrentDate(CommonComponents.getTimeStamp()), style = CommonComponents.descriptionTextStyle(context))
        }

        // Title input section
        Text("Enter announcement title", style = CommonComponents.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(10.dp))
        AnnouncementTextField(
            value = title, onValueChange = { newTitle ->
                title = newTitle
            }, singleLine = true, placeholder = "Title", context = context
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Description input section
        Text("Enter announcement description", style = CommonComponents.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(10.dp))
        AnnouncementTextField(
            value = description, onValueChange = { newDescription ->
                description = newDescription
            }, singleLine = false, placeholder = "Description", context = context
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Button to save the edited announcement
        Row(modifier = Modifier.fillMaxWidth(0.9f)) {
            Button(
                onClick = {
                    // Save the edited announcement through the ViewModel
                    announcementViewModel.saveAnnouncement(announcement.copy(
                        title = title,
                        description = description,
                        date = CommonComponents.getTimeStamp()
                    ), onComplete = { success ->
                        if (success) {
                            onComplete() // Call the onComplete callback if save is successful
                        }
                    })
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CommonComponents.extraColor2())
            ) {
                Text("Edit", style = CommonComponents.descriptionTextStyle(context))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}