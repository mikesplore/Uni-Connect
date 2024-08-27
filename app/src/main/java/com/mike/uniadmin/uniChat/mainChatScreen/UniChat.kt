package com.mike.uniadmin.uniChat.mainChatScreen

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.backEnd.groupchat.generateConversationId
import com.mike.uniadmin.backEnd.userchat.DeliveryStatus
import com.mike.uniadmin.backEnd.userchat.UserChatViewModel
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.backEnd.users.UserStateEntity
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.getUserChatViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.homeScreen.UserItem
import com.mike.uniadmin.uniChat.UsersProfile
import com.mike.uniadmin.uniChat.groupChat.groupChatComponents.UniGroups
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniChat(navController: NavController, context: Context) {
    val userViewModel = getUserViewModel(context)
    val messageViewModel = getUserChatViewModel(context)
    val searchQuery by remember { mutableStateOf("") }
    val userStates by userViewModel.userStates.observeAsState(emptyMap())
    val currentUser by userViewModel.user.observeAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val users by userViewModel.users.observeAsState()

    val filteredUsers = users?.filter { user ->
        user.firstName.contains(searchQuery, ignoreCase = true) || user.lastName.contains(
            searchQuery, ignoreCase = true
        ) || user.email.contains(searchQuery, ignoreCase = true)
    } ?: emptyList()


    val tabs = listOf("Chats", "Groups", "Contacts")

    LaunchedEffect(Unit) {
        userViewModel.findUserByEmail(FirebaseAuth.getInstance().currentUser?.email!!) {}
        userViewModel.checkAllUserStatuses()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                   IconButton(onClick = {navController.navigate("homeScreen")}) {
                       Icon(Icons.Default.ArrowBackIosNew, "Back")
                   }
                },
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
                modifier = Modifier.background(CC.secondary())
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        modifier = Modifier.weight(1f), // Add weight modifier here
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
                                    modifier = Modifier.padding(vertical = 5.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    )
                }
            }

            // Tab content
            when (selectedTabIndex) {
                0 -> ChatsScreen(
                    currentUser,
                    filteredUsers,
                    context,
                    navController,
                    messageViewModel,
                    userStates,
                    userViewModel
                )

                1 -> UniGroups(context, navController)
                2 -> UsersProfile(context, navController)
            }
        }
    }
}

@Composable
fun ChatsScreen(
    currentUser: UserEntity?,
    filteredUsers: List<UserEntity>,
    context: Context,
    navController: NavController,
    userChatViewModel: UserChatViewModel,
    userStates: Map<String, UserStateEntity>,
    userViewModel: UserViewModel
) {
    val usersWithMessages = filteredUsers.filter { user ->
        val conversationId =
            "Direct Messages/${currentUser?.id?.let { generateConversationId(it, user.id) }}"
        val messages by userChatViewModel.getCardUserChats(conversationId)
            .observeAsState(emptyList())
        messages.isNotEmpty()
    }

    LazyRow(
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        items(usersWithMessages) { user ->
            UserItem(user, context, navController, userViewModel)
        }
    }
    // Chats Section
    if (usersWithMessages.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            UsersList(userViewModel, context)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "No Chats", style = CC.descriptionTextStyle(context).copy(fontSize = 20.sp))
        }
    }
    LazyColumn {
        items(filteredUsers) { user ->

            val conversationId = "Direct Messages/${
                currentUser?.id?.let { id ->
                    generateConversationId(id, user.id)
                }
            }"

            LaunchedEffect(conversationId) {
                Log.d("Card Messages", "fetching messages for the path: $conversationId")
                userChatViewModel.fetchCardUserChats(conversationId)
            }

            val messages by userChatViewModel.getCardUserChats(conversationId)
                .observeAsState(emptyList())
            Log.d("Card Messages", "fetched messages are: $messages")

            if (messages.isNotEmpty()) {
                val messageCounter =
                    messages.count { unreadMessages -> unreadMessages.deliveryStatus == DeliveryStatus.SENT && unreadMessages.senderID == user.id }

                AnimatedVisibility(
                    visible = true, enter = fadeIn(), exit = fadeOut()
                ) {
                    UserMessageCard(
                        userEntity = user,
                        latestMessage = messages.lastOrNull(),
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
private fun UsersList(userViewModel: UserViewModel, context: Context) {
    val users by userViewModel.users.observeAsState()
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy((-8).dp)
    ) {
        items(users ?: emptyList()) { user ->
            UserCard(user, context)
        }
    }
}

@Composable
private fun UserCard(user: UserEntity, context: Context) {
    Box(
        modifier = Modifier
            .background(randomColor.random(), CircleShape)
            .clip(CircleShape)
            .border(
                1.dp,
                CC.secondary(),
                CircleShape
            )
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        if (user.profileImageLink.isNotBlank()) {
            AsyncImage(
                model = user.profileImageLink,
                contentDescription = user.firstName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(text = user.firstName[0].toString(), style = CC.descriptionTextStyle(context))
        }
    }

}