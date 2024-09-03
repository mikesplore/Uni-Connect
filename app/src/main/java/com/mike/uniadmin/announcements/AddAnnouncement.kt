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
import com.mike.uniadmin.ui.theme.CommonComponents as CC

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
            .border(1.dp, CC.extraColor2(), RoundedCornerShape(10.dp))
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
                style = CC.titleTextStyle().copy(fontWeight = FontWeight.Bold)
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
                        1.dp, CC.textColor(), CircleShape
                    )
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
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
                        style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Text(CC.getDateFromTimeStamp(CC.getTimeStamp()), style = CC.descriptionTextStyle())
        }

        Text("Enter announcement title", style = CC.descriptionTextStyle())
        Spacer(modifier = Modifier.height(10.dp))

        AnnouncementTextField(
            value = title,
            onValueChange = { title = it },
            singleLine = true,
            placeholder = "Title",
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("Enter announcement description", style = CC.descriptionTextStyle())
        Spacer(modifier = Modifier.height(10.dp))

        AnnouncementTextField(
            value = description,
            onValueChange = { description = it },
            singleLine = false,
            placeholder = "Description",
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
                            date = CC.getTimeStamp(),
                            authorID = senderId,
                        )
                        if (title != "" && description != "") {
                            announcementViewModel.saveAnnouncement(newAnnouncement,
                                onComplete = { success ->
                                    if (success) {
                                        showNotification(context, title, description)
                                        notificationViewModel.writeNotification(
                                            notificationEntity = NotificationEntity(
                                                time = CC.getTimeStamp(),
                                                date = CC.getTimeStamp(),
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
                    containerColor = CC.extraColor2(),
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(color = CC.textColor(), strokeWidth = 1.dp, modifier = Modifier.size(20.dp))
                }else{
                    Text("Post", style = CC.descriptionTextStyle())
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
) {
    TextField(
        value = value,
        textStyle = CC.descriptionTextStyle(),
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = CC.descriptionTextStyle()) },
        singleLine = singleLine,
        colors = TextFieldDefaults.colors(
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedContainerColor = CC.primary(),
            unfocusedContainerColor = CC.primary(),
            focusedIndicatorColor = CC.extraColor2(),
            unfocusedIndicatorColor = CC.textColor(),
            focusedPlaceholderColor = CC.textColor(),
            unfocusedPlaceholderColor = CC.textColor(),
            cursorColor = CC.textColor()
        ),
        modifier = modifier

    )

}
