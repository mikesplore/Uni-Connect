package com.mike.uniadmin.announcements

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mike.uniadmin.backEnd.announcements.AnnouncementEntity
import com.mike.uniadmin.backEnd.announcements.AnnouncementViewModel
import com.mike.uniadmin.backEnd.announcements.AnnouncementViewModelFactory
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.backEnd.notifications.NotificationEntity
import com.mike.uniadmin.backEnd.notifications.NotificationViewModel
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.getAnnouncementViewModel
import com.mike.uniadmin.getNotificationViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.notification.showNotification

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

    // Fetch announcements on refresh
    LaunchedEffect(refresh) {
        announcementViewModel.fetchAnnouncements()
        refresh = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, actions = {
                // Add Announcement button
                IconButton(onClick = { addAnnouncement = !addAnnouncement }) {
                    Icon(Icons.Default.Add, "Add", tint = CC.textColor())
                }

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
                                AnnouncementCard(announcement = announcement,
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
    val signedInUser by userViewModel.signedInUser.observeAsState()
    val user by userViewModel.user.observeAsState()
    var loading by remember { mutableStateOf(false) }

    //these will be filled automatically (if the data exists that is)
    val profileLink = user?.profileImageLink ?: ""
    val author = user?.firstName ?: ""
    val senderId = user?.id ?: ""

    //get the data of the signed in user
    LaunchedEffect(Unit) {
        userViewModel.getSignedInUser()
        signedInUser?.let {
            it.email.let { email -> userViewModel.findUserByEmail(email) {} }
        }

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
                style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
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
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Text(CC.getCurrentDate(CC.getTimeStamp()), style = CC.descriptionTextStyle(context))
        }

        Text("Enter announcement title", style = CC.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(10.dp))

        AnnouncementTextField(
            value = title,
            onValueChange = { title = it },
            singleLine = true,
            placeholder = "Title",
            context = context
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("Enter announcement description", style = CC.descriptionTextStyle(context))
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
                            date = CC.getTimeStamp(),
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
                Text("Post", style = CC.descriptionTextStyle(context))
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
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = CC.descriptionTextStyle(context)) },
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
            .border(1.dp, CC.extraColor2(), RoundedCornerShape(10.dp))
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
                style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
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
                    .border(1.dp, CC.textColor(), CircleShape)
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
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
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            // Display the current date
            Text(CC.getCurrentDate(CC.getTimeStamp()), style = CC.descriptionTextStyle(context))
        }

        // Title input section
        Text("Enter announcement title", style = CC.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(10.dp))
        AnnouncementTextField(
            value = title, onValueChange = { newTitle ->
                title = newTitle
            }, singleLine = true, placeholder = "Title", context = context
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Description input section
        Text("Enter announcement description", style = CC.descriptionTextStyle(context))
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
                        date = CC.getTimeStamp()
                    ), onComplete = { success ->
                        if (success) {
                            onComplete() // Call the onComplete callback if save is successful
                        }
                    })
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CC.extraColor2())
            ) {
                Text("Edit", style = CC.descriptionTextStyle(context))
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}


@Composable
fun AnnouncementCard(
    announcement: AnnouncementEntity,
    onEdit: () -> Unit,
    onDelete: (String) -> Unit,
    context: Context,
    isEditing: Boolean,
    onEditComplete: () -> Unit,
    announcementViewModel: AnnouncementViewModel
) {
    // State to track whether the card is expanded or not
    var expanded by remember { mutableStateOf(false) }
    val iconRotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "")
    val cardElevation by animateDpAsState(targetValue = if (expanded) 8.dp else 2.dp, label = "")
    val descriptionAlpha by animateFloatAsState(targetValue = if (expanded) 1f else 0.8f,
        label = ""
    )

    Card(
        elevation = CardDefaults.elevatedCardElevation(cardElevation),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded }
            .animateContentSize() // Animate size changes
    ) {
        Column(
            modifier = Modifier
                .background(CC.secondary(), shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile image or author's initial
                Box(
                    modifier = Modifier
                        .border(1.dp, CC.textColor(), CircleShape)
                        .clip(CircleShape)
                        .background(CC.secondary(), CircleShape)
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (announcement.imageLink.isNotEmpty()) {
                        // Load image asynchronously if the link is available
                        AsyncImage(
                            model = announcement.imageLink,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Display author's initial if no image is available
                        Text(
                            "${announcement.authorName[0]}",
                            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Announcement title
                Text(
                    text = announcement.title,
                    style = CC.descriptionTextStyle(context),
                    fontWeight = FontWeight.Bold,
                    color = CC.textColor(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Button to toggle expansion of the card
                IconButton(
                    onClick = { expanded = !expanded },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = CC.primary().copy(0.3f),
                        contentColor = CC.textColor()
                    )
                ) {
                    Icon(
                        Icons.Default.ArrowDownward,
                        "Expand",
                        modifier = Modifier.rotate(iconRotation)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Announcement description
                    Text(
                        text = announcement.description,
                        style = CC.descriptionTextStyle(context).copy(fontSize = 14.sp),
                        color = CC.textColor().copy(alpha = descriptionAlpha),
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Author name and date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = announcement.authorName,
                            style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp),
                            color = CC.textColor().copy(alpha = 0.6f),
                        )

                        Text(
                            text = CC.getRelativeDate(CC.getCurrentDate(announcement.date)),
                            style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp),
                            color = CC.textColor().copy(alpha = 0.6f),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Edit and Delete buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { onEdit() },
                            colors = ButtonDefaults.buttonColors(containerColor = CC.primary())
                        ) {
                            Text("Edit", style = CC.descriptionTextStyle(context))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onDelete(announcement.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CC.tertiary())
                        ) {
                            Text("Delete", style = CC.descriptionTextStyle(context))
                        }
                    }
                }
            }

            // Inline editing form when the card is in editing mode
            if (isEditing) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    EditAnnouncement(
                        announcement = announcement, context = context, onComplete = {
                            onEditComplete()
                        }, announcementViewModel = announcementViewModel
                    )
                }
            }
        }
    }
}



@Preview
@Composable
fun AlertsPreview() {

}