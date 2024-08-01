package com.mike.uniadmin.announcements

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.chat.getCurrentDate
import com.mike.uniadmin.dataModel.announcements.AnnouncementEntity
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModel
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase

import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(context: Context) {

    var addAnnouncement by rememberSaveable { mutableStateOf(false) }
    val announcementAdmin = context.applicationContext as? UniAdmin
    val announcementRepository = remember { announcementAdmin?.announcementRepository }
    val announcementViewModel: AnnouncementViewModel = viewModel(
        factory = AnnouncementViewModelFactory(
            announcementRepository ?: throw IllegalStateException("AnnouncementRepository is null")
        )
    )

    val userRepository = remember { announcementAdmin?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("UserRepository is null")
        )
    )
    val announcements by announcementViewModel.announcements.observeAsState()
    var refresh by remember { mutableStateOf(true) }
    var editingAnnouncementId by remember { mutableStateOf<String?>(null) }

    val announcementsLoading by announcementViewModel.isLoading.observeAsState()

    LaunchedEffect(refresh) {
        announcementViewModel.fetchAnnouncements()
        refresh = false
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, actions = {
                IconButton(onClick = {
                    addAnnouncement = !addAnnouncement
                }) {
                    Icon(
                        Icons.Default.Add, "Add", tint = CC.textColor()
                    )
                }

                IconButton(onClick = {
                    refresh = !refresh
                }) {
                    if (refresh) {
                        CircularProgressIndicator(color = CC.extraColor2())
                    }
                    Icon(
                        Icons.Default.Refresh, "Refresh", tint = CC.textColor()
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CC.primary()
            ))
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
            AnimatedVisibility(visible = addAnnouncement) {
                AddAnnouncement(
                    context,
                    onComplete = { addAnnouncement = false },
                    userViewModel,
                    announcementViewModel
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            when {
                announcementsLoading == true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CC.textColor())
                    }
                }
                announcements.isNullOrEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No announcements available", color = CC.textColor())
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxWidth(0.9f)) {
                        announcements?.let { announcements ->
                            items(announcements) { announcement ->
                                val isEditing = editingAnnouncementId == announcement.id

                                AnnouncementCard(announcement = announcement, onEdit = {
                                    editingAnnouncementId = if (isEditing) null else announcement.id
                                }, onDelete = { id ->
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
                                }, context = context, isEditing = isEditing, onEditComplete = {
                                    editingAnnouncementId = null
                                }, announcementViewModel = announcementViewModel
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
    announcementViewModel: AnnouncementViewModel
) {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val user by userViewModel.user.observeAsState()

    val profileLink = user?.profileImageLink
    val author = user?.firstName
    val senderId = user?.id

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.email?.let { userViewModel.findUserByEmail(it) {} }
    }


    Column(
        modifier = Modifier
            .imePadding()
            .border(1.dp, CC.extraColor2(), RoundedCornerShape(10.dp))
            .fillMaxWidth(0.9f), horizontalAlignment = Alignment.CenterHorizontally
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

            Text(getCurrentDate(), style = CC.descriptionTextStyle(context))
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
            Button(
                onClick = {
                    MyDatabase.generateAnnouncementID { id ->
                        val newAnnouncement = AnnouncementEntity(
                            id = id,
                            title = title,
                            description = description,
                            date = getCurrentDate(),
                            authorName = author,
                            authorID = senderId,
                            imageLink = profileLink
                        )
                        if (title != "" && description != "") {
                            announcementViewModel.saveAnnouncement(newAnnouncement,
                                onComplete = { success ->
                                    if (success) {
                                        title = ""
                                        description = ""
                                        onComplete(true)
                                    }
                                })
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter title and description",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                )
            ) {
                Text("Post", style = CC.descriptionTextStyle(context))
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
            focusedContainerColor = CC.extraColor2(),
            unfocusedContainerColor = CC.primary(),
            focusedIndicatorColor = CC.extraColor2(),
            unfocusedIndicatorColor = CC.textColor(),
            focusedPlaceholderColor = CC.textColor(),
            unfocusedPlaceholderColor = CC.textColor()
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

    var title by remember { mutableStateOf(announcement.title) }
    var description by remember { mutableStateOf(announcement.description) }

    Column(
        modifier = Modifier
            .imePadding()
            .border(1.dp, CC.extraColor2(), RoundedCornerShape(10.dp))
            .fillMaxWidth(0.9f), horizontalAlignment = Alignment.CenterHorizontally
    ) {

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

        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.8f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
                if (announcement.imageLink?.isNotEmpty() == true) {
                    AsyncImage(
                        model = announcement.imageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "${announcement.authorName?.get(0)}",
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Text(getCurrentDate(), style = CC.descriptionTextStyle(context))
        }

        Text("Enter announcement title", style = CC.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(10.dp))

        title?.let {
            AnnouncementTextField(
                value = it, onValueChange = { newTitle ->
                    title = newTitle
                }, singleLine = true, placeholder = "Title", context = context
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Enter announcement description", style = CC.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(10.dp))
        description?.let {
            AnnouncementTextField(
                value = it, onValueChange = { newDescription ->
                    description = newDescription
                }, singleLine = false, placeholder = "Description", context = context
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(0.9f)) {
            Button(
                onClick = {
                    announcementViewModel.saveAnnouncement(announcement.copy(
                        title = title,
                        description = description,
                        date = getCurrentDate()
                    ), onComplete = { success ->
                        if (success) {
                            onComplete()
                            Log.d(
                                "EditAnnouncement",
                                "Announcement updated successfully, the new announcement is: $announcement"
                            )
                        }
                    })

                }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                )
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
    var expanded by remember { mutableStateOf(false) }
    val text = if (expanded) "Close" else "Open"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CC.secondary(), shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
            .imePadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, CC.textColor(), CircleShape)
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (announcement.imageLink?.isNotEmpty() == true) {
                    AsyncImage(
                        model = announcement.imageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "${announcement.authorName?.get(0)}",
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            announcement.title?.let {
                Text(
                    text = it,
                    style = CC.descriptionTextStyle(context),
                    fontWeight = FontWeight.Bold,
                    color = CC.textColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            Button(
                onClick = { expanded = !expanded },
                colors = ButtonDefaults.buttonColors(containerColor = CC.primary())
            ) {
                Text(text, style = CC.descriptionTextStyle(context))
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            announcement.description?.let {
                Text(
                    text = it,
                    style = CC.descriptionTextStyle(context).copy(fontSize = 14.sp),
                    color = CC.textColor().copy(alpha = 0.8f),
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                announcement.authorName?.let {
                    Text(
                        text = it,
                        style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp),
                        color = CC.textColor().copy(alpha = 0.6f),
                    )
                }
                announcement.date?.let {
                    Text(
                        text = it,
                        style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp),
                        color = CC.textColor().copy(alpha = 0.6f),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
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

@Preview
@Composable
fun AlertsPreview() {

}