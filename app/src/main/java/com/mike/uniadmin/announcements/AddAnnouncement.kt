package com.mike.uniadmin.announcements

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.announcements.AnnouncementEntity
import com.mike.uniadmin.backEnd.announcements.AnnouncementViewModel
import com.mike.uniadmin.backEnd.notifications.NotificationEntity
import com.mike.uniadmin.backEnd.notifications.NotificationViewModel
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.notification.showNotification
import com.mike.uniadmin.ui.theme.CommonComponents

@Composable
fun AddAnnouncement(
    context: Context,
    onComplete: (Boolean) -> Unit,
    userViewModel: UserViewModel,
    announcementViewModel: AnnouncementViewModel,
    notificationViewModel: NotificationViewModel
) {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val user by userViewModel.user.observeAsState()
    var loading by remember { mutableStateOf(false) }

    //these will be filled automatically (if the data exists that is)
    val profileLink = user?.profileImageLink ?: ""
    val author = user?.firstName ?: ""
    val senderId = user?.id ?: ""

    //get the data of the signed in user
    LaunchedEffect(Unit) {
        val email = UniAdminPreferences.userEmail.value
        userViewModel.findUserByEmail(email){}

    }


    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .border(1.dp, CommonComponents.extraColor2(), RoundedCornerShape(10.dp))
            .fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            modifier = Modifier.height(50.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Add new Announcement",
                style = CommonComponents.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Profile picture
            Box(
                modifier = Modifier
                    .border(
                        1.dp, CommonComponents.textColor(), CircleShape
                    )
                    .clip(CircleShape)
                    .background(CommonComponents.secondary(), CircleShape)
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (user?.profileImageLink?.isNotEmpty() == true) {
                    AsyncImage(
                        model = user?.profileImageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "${user?.firstName?.get(0)}",
                        style = CommonComponents.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Text(CommonComponents.getCurrentDate(CommonComponents.getTimeStamp()), style = CommonComponents.descriptionTextStyle(context))
        }

        Text("Enter announcement title", style = CommonComponents.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(10.dp))

        AnnouncementTextField(
            value = title,
            onValueChange = { title = it },
            singleLine = true,
            placeholder = "Title",
            context = context
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("Enter announcement description", style = CommonComponents.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(10.dp))

        AnnouncementTextField(
            value = description,
            onValueChange = { description = it },
            singleLine = false,
            placeholder = "Description",
            context = context
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(0.9f)) {

            // save the announcement
            Button(
                onClick = {
                    loading = true
                    MyDatabase.generateAnnouncementID { id ->
                        val newAnnouncement = AnnouncementEntity(
                            id = id,
                            title = title,
                            description = description,
                            date = CommonComponents.getTimeStamp(),
                            authorName = author,
                            authorID = senderId,
                            imageLink = profileLink
                        )
                        if (title != "" && description != "") {
                            announcementViewModel.saveAnnouncement(newAnnouncement,
                                onComplete = { success ->
                                    if (success) {
                                        showNotification(context, title, description)
                                        notificationViewModel.writeNotification(
                                            notificationEntity = NotificationEntity(
                                                time = CommonComponents.getTimeStamp(),
                                                date = CommonComponents.getTimeStamp(),
                                                name = author,
                                                userId = senderId,
                                                description = description,
                                                id = id,
                                                category = "Announcements",
                                                title = title,
                                            )
                                        )
                                        notificationViewModel.fetchNotifications()
                                        title = ""
                                        description = ""
                                        loading = false
                                        onComplete(true)

                                    }
                                })
                        } else {
                            loading = false
                            Toast.makeText(

                                context, "Please enter title and description", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CommonComponents.extraColor2(),
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(color = CommonComponents.textColor(), strokeWidth = 1.dp, modifier = Modifier.size(20.dp))
                }else{
                    Text("Post", style = CommonComponents.descriptionTextStyle(context))
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun AnnouncementTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean,
    placeholder: String,
    context: Context
) {
    TextField(
        value = value,
        textStyle = CommonComponents.descriptionTextStyle(context),
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = CommonComponents.descriptionTextStyle(context)) },
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedTextColor = CommonComponents.textColor(),
            unfocusedTextColor = CommonComponents.textColor(),
            focusedContainerColor = CommonComponents.primary(),
            unfocusedContainerColor = CommonComponents.primary(),
            focusedIndicatorColor = CommonComponents.extraColor2(),
            unfocusedIndicatorColor = CommonComponents.textColor(),
            focusedPlaceholderColor = CommonComponents.textColor(),
            unfocusedPlaceholderColor = CommonComponents.textColor(),
            cursorColor = CommonComponents.textColor()
        ),
        modifier = modifier

    )

}