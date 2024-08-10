package com.mike.uniadmin.notification

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.dataModel.groupchat.ChatEntity
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.groupchat.generateConversationId
import com.mike.uniadmin.dataModel.notifications.NotificationEntity
import com.mike.uniadmin.dataModel.notifications.NotificationViewModel
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNotifications(navController: NavController, context: Context) {
    val notificationAdmin = context.applicationContext as UniAdmin

    val notificationRepository = remember { notificationAdmin.notificationRepository }
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.NotificationViewModelFactory(notificationRepository)
    )
    val chatRepository = remember { notificationAdmin.chatRepository }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.ChatViewModelFactory(chatRepository)
    )
    val userRepository = remember { notificationAdmin.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(userRepository)
    )

    // Keep track of previous notifications
    var previousNotifications by remember { mutableStateOf<List<NotificationEntity>>(emptyList()) }
    val notifications by notificationViewModel.notifications.observeAsState(emptyList())
    var refresh by remember { mutableStateOf(false) }

    LaunchedEffect(refresh) {
        notificationViewModel.fetchNotifications()
    }

    // Check for new notifications
    LaunchedEffect(notifications) {
        val newNotifications = notifications.filterNot { previousNotifications.contains(it) }
        if (newNotifications.isNotEmpty()) {
            newNotifications.forEach { notification ->
                showNotification(context, notification.title, notification.description)
            }
        }
        previousNotifications = notifications
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = {
                        refresh = !refresh
                    }) {
                        Icon(
                            Icons.Default.Refresh, "Refresh",
                            tint = CC.textColor()
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("homeScreen") }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew, "Refresh",
                            tint = CC.textColor()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary()
                )
            )

        },
        containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Notifications",
                    style = CC.titleTextStyle(context)
                        .copy(fontSize = 30.sp, fontWeight = FontWeight.Bold)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Group notifications by category
                val groupedNotifications = notifications.groupBy { categorizedNotification -> categorizedNotification.category }

                // Sort notifications within each category by date in descending order
                groupedNotifications.forEach { (category, notificationsForCategory) ->
                    val sortedNotifications = notificationsForCategory.sortedByDescending { it.date }

                    item {
                        // Display category header only once
                        Text(
                            text = when (category) {
                                "Announcements" -> "Announcements"
                                else -> "New Users"
                            },
                            style = CC.descriptionTextStyle(context)
                                .copy(fontWeight = FontWeight.Bold, fontSize = 25.sp),
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    items(sortedNotifications) { notification ->
                        when (notification.category) {
                            "Announcements" -> AnnouncementNotification(notification, context)
                            else -> NotificationItem(
                                notification = notification,
                                context = context,
                                chatViewModel,
                                userViewModel
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun NotificationItem(notification: NotificationEntity, context: Context, chatViewModel: ChatViewModel, userViewModel: UserViewModel) {
    val user by userViewModel.user.observeAsState()
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FirebaseAuth.getInstance().currentUser?.email?.let { userViewModel.findUserByEmail(it) {} }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, CC.secondary(), RoundedCornerShape(8.dp))
            .background(CC.primary(), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = notification.title,
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            text = notification.description,
            style = CC.descriptionTextStyle(context).copy(color = CC.textColor().copy(0.7f)),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${CC.getCurrentDate(notification.date)} at ${CC.getCurrentTime(notification.time)}",
                style = CC.descriptionTextStyle(context).copy(color = CC.textColor().copy(0.5f), fontSize = 12.sp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .height(30.dp)
                .background(CC.extraColor1(), RoundedCornerShape(10.dp))
                .padding(end = 5.dp),
                contentAlignment = Alignment.Center){
                Text("Hi, ${notification.name} 👋",
                    modifier = Modifier.padding(start = 10.dp),
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold))
            }
            IconButton(onClick = {
                loading = true
                MyDatabase.generateChatID { id ->
                    user?.let { currentUser ->
                        notification.userId.let { notificationUserId ->
                            val conversationId = "Direct Messages/${generateConversationId(userId1 = currentUser.id, userId2 = notificationUserId)}"
                            chatViewModel.saveChat(
                                path = conversationId,
                                chat = ChatEntity(
                                    id = id,
                                    message = "Hi, ${notification.name} 👋",
                                    senderName = currentUser.firstName,
                                    senderID = currentUser.id,
                                    time = CC.getTimeStamp(),
                                    date = CC.getTimeStamp(),
                                    profileImageLink = currentUser.profileImageLink
                                ),
                                onSuccess = { success ->
                                    if (success) {
                                        loading = false
                                        Toast.makeText(context, "Sent!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        loading = false
                                        Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    } ?: run {
                        Toast.makeText(context, "Current user is null", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                AnimatedVisibility(loading) {
                    CircularProgressIndicator(color = CC.textColor(), strokeWidth = 1.dp, modifier = Modifier.size(30.dp))
                }
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Wave",
                    tint = CC.secondary()
                )
            }}
        }
    }
}


@Composable
fun AnnouncementNotification(notification: NotificationEntity, context: Context) {
    Column(
        modifier = Modifier
            .border(1.dp, CC.extraColor2(), RoundedCornerShape(12.dp))
            .padding(12.dp)
            .fillMaxWidth(0.95f)
            .background(CC.primary(), RoundedCornerShape(12.dp))
    ) {
        Text(
            text = notification.title,
            style = CC.descriptionTextStyle(context).copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )

        Text(
            text = notification.description,
            style = CC.descriptionTextStyle(context).copy(
                color = CC.textColor().copy(0.7f),
                fontSize = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Text(
            text = CC.getRelativeDate(CC.getCurrentDate(notification.date)),
            style = CC.descriptionTextStyle(context).copy(
                color = CC.textColor().copy(0.7f),
                fontSize = 12.sp
            ),
            modifier = Modifier.align(Alignment.End)
        )
    }
}



