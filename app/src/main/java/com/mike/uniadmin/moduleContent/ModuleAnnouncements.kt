package com.mike.uniadmin.moduleContent

import android.content.Context
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements.ModuleAnnouncement
import com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements.ModuleAnnouncementViewModel
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.dashboard.isDeviceOnline
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun AnnouncementsItem(
    moduleID: String,
    moduleAnnouncementViewModel: ModuleAnnouncementViewModel,
    context: Context,
    userViewModel: UserViewModel
) {
    var visible by remember { mutableStateOf(false) }
    val userType = UniAdminPreferences.userType.value
    val announcements =
        moduleAnnouncementViewModel.announcements.observeAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .background(
                    CC
                        .tertiary()
                        .copy(0.1f), RoundedCornerShape(10.dp)
                )
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { moduleAnnouncementViewModel.getModuleAnnouncements(moduleID) },

                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = background,
                )) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = CC.textColor()
                )
            }
            Text("${announcements.value.size} announcements", style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center),
                modifier = Modifier.weight(1f))

            if (userType == "admin"){
            FloatingActionButton(
                onClick = { visible = !visible },
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(35.dp),
                containerColor = background,
                contentColor = CC.textColor()
            ) {
                Icon(Icons.Default.Add, "Add announcement")
            }}
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible) {
                AddAnnouncementItem(
                    moduleID,
                    context,
                    onExpandedChange = { visible = !visible },
                    moduleAnnouncementViewModel,
                    userViewModel
                )
            }

            if (announcements.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                ){
                Text("No Announcements",
                    style = CC.descriptionTextStyle(context),
                    modifier = Modifier.wrapContentSize(Alignment.Center)
                )}
            }
            else {
                LazyColumn {
                    items(announcements.value) { announcement ->
                        AnnouncementCard(announcement, context, userViewModel)
                    }
                }
            }
        }
    }
}


@Composable
fun AnnouncementCard(
    moduleAnnouncement: ModuleAnnouncement,
    context: Context,
    userViewModel: UserViewModel
) {
    var profileImageLink by remember { mutableStateOf("") }
    var authorName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        userViewModel.findUserByAdmissionNumber(moduleAnnouncement.author) { user ->
            user?.profileImageLink?.let {
                profileImageLink = it
                authorName = user.firstName
            }
        }
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CC.secondary()),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp)),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Announcement Title
            Text(
                text = moduleAnnouncement.title,
                style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold, fontSize = 20.sp)
            )

            // Announcement Description
            Text(
                text = moduleAnnouncement.description,
                style = CC.descriptionTextStyle(context).copy(
                    color = CC.textColor().copy(0.7f),
                    textAlign = TextAlign.Start
                )
            )

            // Divider for visual separation
            HorizontalDivider(color = CC.textColor().copy(0.2f))

            // Sender Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Image
                    AsyncImage(
                        model = profileImageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CC.primary())
                            .border(1.dp, CC.textColor(), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Author's Name and Date
                    Column {
                        Text(
                            text = authorName,
                            style = CC.descriptionTextStyle(context).copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = moduleAnnouncement.date,
                            style = CC.descriptionTextStyle(context).copy(
                                fontSize = 12.sp,
                                color = CC.textColor().copy(0.6f)
                            )
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun AddAnnouncementItem(
    moduleID: String,
    context: Context,
    onExpandedChange: (Boolean) -> Unit,
    moduleViewModel: ModuleAnnouncementViewModel,
    userViewModel: UserViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var senderName by remember { mutableStateOf("") }
    var profileImageLink by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        val email = UniAdminPreferences.userEmail.value

        userViewModel.findUserByEmail(email) { fetchedUser ->
            fetchedUser?.let {
                senderName = it.id
                profileImageLink = it.profileImageLink
            }
        }
    }

    Column(
        modifier = Modifier
            .border(
                width = 1.dp, color = CC.secondary(), shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
    ) {
        // User Info Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (profileImageLink.isNotEmpty()) {
                AsyncImage(
                    model = profileImageLink,
                    contentDescription = senderName,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(CC.tertiary()),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(CC.secondary()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = senderName.firstOrNull()?.toString() ?: "",
                        style = CC.titleTextStyle(context).copy(
                            fontWeight = FontWeight.Bold,
                            color = CC.textColor()
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = senderName,
                    style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "New announcement for date: ${CC.getDateFromTimeStamp(CC.getTimeStamp())}",
                    style = CC.descriptionTextStyle(context).copy(
                        color = CC.textColor().copy(alpha = 0.6f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Form Fields
        AddTextField(
            label = "Title", value = title, onValueChange = { title = it }, context
        )
        Spacer(modifier = Modifier.height(16.dp))
        AddTextField(
            label = "Description",
            value = description,
            onValueChange = { description = it },
            context,
            singleLine = false,
            maxLines = 4
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    onExpandedChange(false)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Cancel", style = CC.descriptionTextStyle(context))
            }

            Button(
                onClick = {
                    if (title.isEmpty() || description.isEmpty()) {
                        return@Button
                    }
                    loading = true
                    MyDatabase.generateAnnouncementID { iD ->
                        val newAnnouncement = ModuleAnnouncement(
                            moduleID = moduleID,
                            announcementID = iD,
                            title = title,
                            description = description,
                            author = senderName,
                            date = CC.getDateFromTimeStamp(CC.getTimeStamp())
                        )
                        moduleViewModel.saveModuleAnnouncement(
                            moduleID = moduleID, announcement = newAnnouncement
                        )
                        loading = false
                        onExpandedChange(false)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CC.primary()
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = background,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Post", style = CC.descriptionTextStyle(context))
                }
            }
        }
    }
}

@Composable
fun InternetError(context: Context){
    Box(modifier = Modifier
        .padding(horizontal = 8.dp, vertical = 8.dp)
        .fillMaxWidth(), contentAlignment = Alignment.Center){
        Text("Oops, No Internet detected", style = CC.descriptionTextStyle(context))
    }
}