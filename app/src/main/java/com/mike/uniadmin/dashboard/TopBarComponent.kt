package com.mike.uniadmin.dashboard

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.dataModel.notifications.NotificationEntity
import com.mike.uniadmin.dataModel.notifications.NotificationViewModel
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC
import kotlinx.coroutines.delay

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContent(
    signedInUser: UserEntity,
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel,
    notificationViewModel: NotificationViewModel
) {
    val loading by userViewModel.isLoading.observeAsState()
    val notifications by notificationViewModel.notifications.observeAsState()
    val isOnline = remember { mutableStateOf(isDeviceOnline(context)) }
    var expanded by remember { mutableStateOf(false) }

    // periodically check the network status
    LaunchedEffect(Unit) {
        while (true) {
            isOnline.value = isDeviceOnline(context)
            delay(10000L) // Check every 10 seconds
        }
    }

    TopAppBar(title = {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoxWithConstraints(
                modifier = Modifier.padding(end = 10.dp)
            ) {
                val size = 50.dp
                Box(
                    modifier = Modifier
                        .border(
                            1.dp, CC.textColor(), CircleShape
                        )
                        .background(CC.secondary(), CircleShape)
                        .clip(CircleShape)
                        .size(size),
                    contentAlignment = Alignment.Center
                ) {

                    if (loading == true) {
                        CircularProgressIndicator(color = CC.textColor())
                    } else if (signedInUser.firstName.isEmpty()) {
                        Icon(
                            Icons.Default.AccountCircle, "Location", tint = CC.textColor()
                        )
                    } else {
                        if (signedInUser.profileImageLink.isNotEmpty()) {
                            AsyncImage(
                                model = signedInUser.profileImageLink,
                                contentDescription = signedInUser.firstName,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                "${signedInUser.firstName[0]}${signedInUser.lastName[0]}",
                                style = CC.titleTextStyle(context)
                                    .copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                val onlineStatus by animateColorAsState(
                    animationSpec = tween(500, easing = LinearEasing),
                    targetValue = if (isOnline.value) Color.Green else Color.Red,
                    label = ""
                )
                // the small dot
                Box(
                    modifier = Modifier
                        .border(
                            1.dp, CC.secondary(), CircleShape
                        )
                        .size(12.dp)
                        .background(
                            onlineStatus, CircleShape
                        )
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = (-6).dp)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 10.dp, end = 20.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = CC.getGreetingMessage(),
                    style = CC.descriptionTextStyle(context)
                        .copy(color = CC.textColor().copy(alpha = 0.5f))
                )
                Text(
                    text = signedInUser.firstName,
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold)
                )

            }
        }
    }, actions = {
        BoxWithConstraints(modifier = Modifier.padding(end = 5.dp)) {
            IconButton(onClick = {
                notificationViewModel.fetchNotifications()
                expanded = !expanded
            }, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = CC.secondary(),
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .border(
                            1.dp, CC.secondary(), CircleShape
                        )
                        .size(10.dp)
                        .background(
                            if (notifications?.isNotEmpty() == true) Color.Green else Color.Red,
                            CircleShape
                        )
                        .align(Alignment.TopCenter)
                        .offset(y = (10).dp, x = (8).dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .heightIn(max = 200.dp)
                    .width(160.dp)
                    .background(CC.extraColor1())
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (notifications != null && notifications!!.isNotEmpty()) {
                        notifications!!.take(5).forEach { notification ->
                            NotificationTitleContent(notification, context)
                        }
                        HorizontalDivider()
                        TextButton(
                            onClick = {
                                navController.navigate("notifications")
                                expanded = false
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View All", style = CC.descriptionTextStyle(context))
                        }
                    } else {
                        Text("No notifications", style = CC.descriptionTextStyle(context))
                    }
                }
            }
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = CC.primary(),
    )
    )
}


@Composable
fun NotificationTitleContent(
    notification: NotificationEntity, context: Context
) {
    Row(modifier = Modifier
        .height(30.dp)
        .padding(5.dp)
        .fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text(
            text = notification.title,
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}