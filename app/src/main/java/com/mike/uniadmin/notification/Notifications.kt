package com.mike.uniadmin.notification

import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.chat.getCurrentDate
import com.mike.uniadmin.chat.getCurrentTimeInAmPm
import com.mike.uniadmin.dataModel.groupchat.ChatEntity
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.groupchat.generateConversationId
import com.mike.uniadmin.dataModel.notifications.NotificationEntity
import com.mike.uniadmin.dataModel.notifications.NotificationViewModel
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.GlobalColors
import java.text.SimpleDateFormat
import java.util.*
import com.mike.uniadmin.CommonComponents as CC

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


    val notifications by notificationViewModel.notifications.observeAsState(emptyList())
    var refresh by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        notificationViewModel.fetchNotifications()
    }

    val groupedNotifications = remember(notifications) {
        notifications.groupBy { it.getFormattedDateForGrouping() }
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
                    IconButton(onClick = { navController.navigate("homescreen") }) {
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
                groupedNotifications.forEach { (date, notificationsForDate) ->
                    item {
                        if (date != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.background(
                                        CC.secondary(),
                                        RoundedCornerShape(10.dp)
                                    )
                                ) {
                                    Text(
                                        text = date,
                                        style = CC.descriptionTextStyle(context),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                    items(notificationsForDate) { notification ->
                        NotificationItem(notification = notification, context = context, chatViewModel, userViewModel)

                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationEntity, context: Context, chatViewModel: ChatViewModel, userViewModel: UserViewModel) {
    val user by userViewModel.user.observeAsState()

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
        notification.title?.let {
            Text(
                text = it,
                style = CC.titleTextStyle(context),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        notification.description?.let {
            Text(
                text = it,
                style = CC.descriptionTextStyle(context),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            notification.time?.let {
                Text(
                    text = it,
                    style = CC.descriptionTextStyle(context).copy(color = Color.Gray)
                )
            }
            IconButton(onClick = {
                MyDatabase.generateChatID { id ->
                    user?.let { currentUser ->
                        notification.userId?.let { notificationUserId ->
                            val conversationId = "Direct Messages/${generateConversationId(userId1 = currentUser.id, userId2 = notificationUserId)}"
                            chatViewModel.saveChat(
                                path = conversationId,
                                chat = ChatEntity(
                                    id = id,
                                    message = "Hi ${notification.name} 👋",
                                    senderName = currentUser.firstName,
                                    senderID = currentUser.id,
                                    time = getCurrentTimeInAmPm(),
                                    date = getCurrentDate(),
                                    profileImageLink = currentUser.profileImageLink
                                ),
                                onSuccess = { success ->
                                    if (success) {
                                        Toast.makeText(context, "Sent!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        } ?: run {
                            Toast.makeText(context, "Notification userId is null", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Toast.makeText(context, "Current user is null", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
                Icon(
                    Icons.AutoMirrored.Filled.Send, // Use a wave hand icon from your resources
                    contentDescription = "Wave",
                    tint = CC.secondary()
                )
            }
        }
    }
}


private fun NotificationEntity.getFormattedDateForGrouping(): String? {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val notificationDate = Calendar.getInstance().apply {
        time = this@getFormattedDateForGrouping.date?.let { dateFormat.parse(it) }!!
    }

    return when {
        dateFormat.format(notificationDate.time) == dateFormat.format(today.time) -> "Today"
        dateFormat.format(notificationDate.time) == dateFormat.format(yesterday.time) -> "Yesterday"
        else -> this.date
    }
}
