package com.mike.uniadmin.uniChat.mainChatScreen

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.backEnd.groupchat.generateConversationId
import com.mike.uniadmin.backEnd.userchat.DeliveryStatus
import com.mike.uniadmin.backEnd.userchat.MessageViewModel
import com.mike.uniadmin.backEnd.userchat.MessageViewModel.MessageViewModelFactory
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.backEnd.users.UserStateEntity
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.getAnnouncementViewModel
import com.mike.uniadmin.getChatViewModel
import com.mike.uniadmin.getCourseTimetableViewModel
import com.mike.uniadmin.getCourseViewModel
import com.mike.uniadmin.getMessageViewModel
import com.mike.uniadmin.getNotificationViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.homeScreen.UserItem
import com.mike.uniadmin.uniChat.UsersProfile
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.UniGroups
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniChat(navController: NavController, context: Context) {
    val userViewModel = getUserViewModel(context)
    val messageViewModel = getMessageViewModel(context)

    var usersLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val userStates by userViewModel.userStates.observeAsState(emptyMap())
    val currentUser by userViewModel.user.observeAsState()


    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val users by userViewModel.users.observeAsState()

    val filteredUsers = users?.filter { user ->
        user.firstName.contains(searchQuery, ignoreCase = true) || user.lastName.contains(
            searchQuery, ignoreCase = true
        ) || user.email.contains(searchQuery, ignoreCase = true)
    } ?: emptyList()


    val tabs = listOf("Chats", "Groups", "Status", "Users")

    LaunchedEffect(Unit) {
        userViewModel.findUserByEmail(FirebaseAuth.getInstance().currentUser?.email!!) {}
        userViewModel.checkAllUserStatuses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "UNI CHAT", style = CC.titleTextStyle(context).copy(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.verticalGradient(
                                colors = listOf(CC.extraColor2(), CC.textColor(), CC.extraColor1())
                            )
                        ), modifier = Modifier.padding(start = 15.dp)
                    )
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.secondary()
                )
            )
        },
        containerColor = CC.primary(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // Scrollable Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CC.secondary(),
                contentColor = CC.textColor(),
                edgePadding = 16.dp, // Padding at the start and end of the row
                modifier = Modifier.background(CC.secondary())
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Box(
                                modifier = Modifier.background(
                                        if (selectedTabIndex == index) CC.primary() else CC.secondary(),
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Text(
                                    text = title,
                                    style = CC.descriptionTextStyle(context).copy(
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = CC.textColor()
                                    ),
                                    modifier = Modifier.padding(
                                        vertical = 5.dp,
                                        horizontal = 8.dp
                                    ) //padding inside the tab
                                )
                            }
                        })
                }
            }

            // Tab content
            when (selectedTabIndex) {
                0 -> ChatsScreen(
                    currentUser,
                    users,
                    filteredUsers,
                    context,
                    navController,
                    messageViewModel,
                    userStates,
                    userViewModel
                )

                1 -> UniGroups(context, navController)
                2 -> ContactsScreen()
                3 -> UsersProfile(context, navController)
                4 -> StatusScreen()
            }
        }
    }
}

@Composable
fun ChatsScreen(
    currentUser: UserEntity?,
    users: List<UserEntity>?,
    filteredUsers: List<UserEntity>,
    context: Context,
    navController: NavController,
    messageViewModel: MessageViewModel,
    userStates: Map<String, UserStateEntity>,
    userViewModel: UserViewModel
) {
    LazyRow(
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        items(users ?: emptyList()) { user ->
            UserItem(user, context, navController, userViewModel)
        }
    }
    // Chats Section
    LazyColumn {
        items(filteredUsers) { user ->

            val conversationId = "Direct Messages/${
                currentUser?.id?.let { id ->
                    generateConversationId(id, user.id)
                }
            }"

            LaunchedEffect(conversationId) {
                Log.d("Card Messages", "fetching messages for the path: $conversationId")
                messageViewModel.fetchCardMessages(conversationId)
            }

            val messages by messageViewModel.getCardMessages(conversationId)
                .observeAsState(emptyList())
            Log.d("Card Messages", "fetched messages are: $messages")

            if (messages.isNotEmpty()) {

                val sortedMessages = messages.sortedBy { content -> content.timeStamp }
                val latestMessage = sortedMessages.lastOrNull()

                val messageCounter =
                    sortedMessages.count { unreadMessages -> unreadMessages.deliveryStatus == DeliveryStatus.SENT && unreadMessages.senderID == user.id }

                AnimatedVisibility(
                    visible = true, enter = fadeIn(), exit = fadeOut()
                ) {
                    UserMessageCard(
                        userEntity = user,
                        latestMessage = latestMessage,
                        userState = userStates[user.id],
                        context = context,
                        userViewModel = userViewModel,
                        navController = navController,
                        messageCounter = messageCounter
                    )
                }
            }
        }
    }


}

@Composable
fun ContactsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Contacts Screen", style = TextStyle(fontSize = 24.sp))
    }
}

@Composable
fun StatusScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Status Screen", style = TextStyle(fontSize = 24.sp))
    }
}




//AnimatedVisibility(pagerState.currentPage == 0, enter = fadeIn(), exit = fadeOut()) {
//    LazyRow(modifier = Modifier.padding(start = 15.dp)) {
//        items(users ?: emptyList()) { user ->
//            UserItem(user, context, navController, userViewModel)
//        }
//    }
//}