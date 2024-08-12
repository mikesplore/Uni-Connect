package com.mike.uniadmin.chat

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.groupchat.generateConversationId
import com.mike.uniadmin.dataModel.userchat.MessageEntity
import com.mike.uniadmin.dataModel.userchat.MessageViewModel
import com.mike.uniadmin.dataModel.userchat.MessageViewModel.MessageViewModelFactory
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserStateEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import kotlinx.coroutines.launch
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalPagerApi::class)
@Composable
fun UniChat(navController: NavController, context: Context) {
    val messageAdmin = context.applicationContext as UniAdmin
    val messageRepository = remember { messageAdmin.messageRepository }
    val messageViewModel: MessageViewModel = viewModel(
        factory = MessageViewModelFactory(messageRepository)
    )

    val userRepository = remember { messageAdmin.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(userRepository)
    )

    var users by remember { mutableStateOf<List<UserEntity>?>(null) }
    var usersLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val userStates by userViewModel.userStates.observeAsState(emptyMap())
    val currentUser by userViewModel.user.observeAsState()

    LaunchedEffect(Unit) {
        userViewModel.findUserByEmail(FirebaseAuth.getInstance().currentUser?.email!!) {}
        userViewModel.checkAllUserStatuses()
    }

    val observedUsers by userViewModel.users.observeAsState()
    val observedUsersLoading by userViewModel.isLoading.observeAsState(false)

    users = observedUsers
    usersLoading = observedUsersLoading

    val filteredUsers = users?.filter { user ->
        user.firstName.contains(searchQuery, ignoreCase = true) || user.lastName.contains(
            searchQuery,
            ignoreCase = true
        ) || user.email.contains(searchQuery, ignoreCase = true)
    } ?: emptyList()

    val brush = Brush.verticalGradient(
        colors = listOf(CC.secondary(), CC.primary())
    )

    Scaffold(containerColor = CC.secondary()) {
        Column(
            modifier = Modifier
                .background(CC.primary())
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .background(brush)
                    .height(100.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Uni Chat", style = CC.titleTextStyle(context).copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = Brush.verticalGradient(
                            colors = listOf(CC.extraColor2(), CC.textColor(), CC.extraColor1())
                        )
                    ), modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            val pagerState = rememberPagerState()
            val coroutineScope = rememberCoroutineScope()

            // Custom TabRow with shifting container animation
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .background(CC.primary(), shape = RoundedCornerShape(10.dp))
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TabRow(
                    containerColor = CC.primary(),
                    contentColor = CC.textColor(),
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        // Custom animated indicator
                        val currentTabPosition = tabPositions[pagerState.currentPage]
                        Box(
                            Modifier
                                .tabIndicatorOffset(currentTabPosition)
                                .padding(4.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                        )
                    },
                    modifier = Modifier
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (pagerState.currentPage == 0) CC.secondary() else CC.primary())
                            .padding(16.dp)
                    ) {
                        Text(
                            "Chats",
                            style = CC.descriptionTextStyle(context)
                                .copy(if (pagerState.currentPage == 0) CC.textColor() else CC.secondary())
                        )
                    }
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (pagerState.currentPage == 1) CC.secondary() else CC.primary())
                            .padding(16.dp)
                    ) {
                        Text("Groups", style = CC.descriptionTextStyle(context))
                    }
                }
            }

            HorizontalPager(count = 2, state = pagerState) { page ->
                when (page) {
                    0 -> {
                        // Chats Section
                        LazyColumn {
                            items(filteredUsers) { user ->
                                val conversationId = "Direct Messages/${
                                    currentUser?.id?.let { id ->
                                        generateConversationId(id, user.id)
                                    }
                                }"
                                LaunchedEffect(conversationId) {
                                    messageViewModel.fetchCardMessages(conversationId)
                                }

                                val messages by messageViewModel.getCardMessages(conversationId)
                                    .observeAsState(emptyList())

                                if (messages.isNotEmpty()) {
                                    val sortedMessages =
                                        messages.sortedBy { content -> content.timeStamp }
                                    val latestMessage = sortedMessages.lastOrNull()

                                    AnimatedVisibility(
                                        visible = true, enter = fadeIn(), exit = fadeOut()
                                    ) {
                                        UserMessageCard(
                                            userEntity = user,
                                            latestMessage = latestMessage,
                                            userState = userStates[user.id],
                                            context = context,
                                            userViewModel = userViewModel,
                                            onUserClicked = {},
                                            navController = navController
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Add your groups implementation here
                        UniGroups(
                            context = context, navController = navController
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun UserMessageCard(
    userEntity: UserEntity,
    latestMessage: MessageEntity?,
    userState: UserStateEntity?,
    context: Context,
    userViewModel: UserViewModel,
    onUserClicked: (Boolean) -> Unit,
    navController: NavController
) {
    val currentUser by userViewModel.user.observeAsState()

    Card(modifier = Modifier
        .fillMaxWidth()
        .height(85.dp)
        .clickable { navController.navigate("chat/${userEntity.id}") }
        .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        )) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onUserClicked(true) },
                modifier = Modifier
                    .border(1.dp, CC.secondary(), CircleShape)
                    .clip(CircleShape)
                    .size(50.dp)
            ) {
                ProfileImage(currentUser = userEntity)
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (userEntity.id == currentUser?.id) "You" else userEntity.firstName,
                    style = CC.descriptionTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                latestMessage?.message?.let {
                    val senderName = if (latestMessage.recipientID == userEntity.id) "You: " else ""

                    Text(
                        text = "$senderName ${createAnnotatedMessage(createAnnotatedText(it).toString())}",
                        style = CC.descriptionTextStyle(context)
                            .copy(fontSize = 14.sp, color = CC.extraColor2().copy(alpha = 0.8f)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                if (userState == null) {
                    Text(
                        text = "Never Online",
                        style = CC.descriptionTextStyle(context).copy(fontSize = 10.sp),
                        color = Color.Gray // Or any color you prefer for "Never Online"
                    )
                } else {
                    userState.let { status ->
                        val textColor = if (status.online == "online") Color.Green else Color.Red
                        Text(
                            text = when {
                                status.online == "online" -> "Online"
                                else -> "${CC.getRelativeDate(CC.getCurrentDate(status.lastDate))} at ${
                                    CC.getFormattedTime(
                                        status.lastTime
                                    )
                                }"
                            },
                            style = CC.descriptionTextStyle(context).copy(fontSize = 10.sp),
                            color = textColor
                        )
                    }
                }

                latestMessage?.timeStamp?.let {
                    Text(
                        text = CC.getRelativeTime(it),
                        style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp),
                        color = CC.textColor().copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}


@Composable
fun ProfileImage(currentUser: UserEntity?) {
    if (currentUser?.profileImageLink?.isNotBlank() == true) {
        AsyncImage(
            model = currentUser.profileImageLink,
            contentDescription = "Profile Image",
            modifier = Modifier
                .clip(CircleShape)
                .fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.student),
            contentDescription = "Profile Image",
            modifier = Modifier.fillMaxSize()
        )
    }
}


// Function to create AnnotatedString
fun createAnnotatedText(message: String): AnnotatedString {
    return AnnotatedString.Builder().apply {
        append(message)
    }.toAnnotatedString()
}


@Composable
fun createAnnotatedMessage(message: String): AnnotatedString {
    val emojiRegex = Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]")

    return buildAnnotatedString {
        var startIndex = 0
        emojiRegex.findAll(message).forEach { matchResult ->
            val emoji = matchResult.value
            val emojiIndex = matchResult.range.first

            append(message.substring(startIndex, emojiIndex))
            addStyle(
                SpanStyle(color = CC.textColor().copy(alpha = 0.5f)), startIndex, emojiIndex
            ) // Apply color to non-emoji text

            append(emoji)

            startIndex = matchResult.range.last + 1
        }

        append(message.substring(startIndex))
        addStyle(
            SpanStyle(color = CC.secondary().copy(alpha = 0.5f)), startIndex, message.length
        ) // Apply color to non-emoji text
    }
}

//@Composable
//fun ClosedChat(context: Context,  usersLoading: Boolean, users: List<UserEntity>?){
//        Column(
//            modifier = Modifier
//                .verticalScroll(rememberScrollState())
//                .fillMaxSize()
//                .padding(16.dp), // Add padding for better spacing
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Image(
//                painter = painterResource(R.drawable.logo),
//                contentDescription = "App Logo" // Add content description
//            )
//            Spacer(modifier = Modifier.height(16.dp)) // Add space after logo
//
//            Text(
//                text = "Uni Chat",
//                style = CC.titleTextStyle(context).copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
//            )
//            Spacer(modifier = Modifier.height(8.dp)) // Add space after title
//
//            Text(
//                text = "Select a user profile to start chatting! 📱",
//                style = CC.descriptionTextStyle(context).copy(fontSize = 16.sp)
//            )
//            Spacer(modifier = Modifier.height(24.dp)) // Add space before subtitles
//
//            // User Status Indicators
//            Text("User Status Indicators", style = CC.titleTextStyle(context).copy(fontSize = 18.sp))
//            Spacer(modifier = Modifier.height(12.dp))
//
//            StatusIndicatorRow(color = CC.secondary(), text = "Recently Online", context)
//            StatusIndicatorRow(color = Color.Green, text = "Currently Online", context)
//            StatusIndicatorRow(color = Color.Red, text = "Never Been Online ", context)
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Chat Feature Information
//            Text("Chat Features", style = CC.titleTextStyle(context).copy(fontSize = 18.sp), fontWeight = FontWeight.Bold)
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Text("• Click a user to open the chat. 💬", style = CC.descriptionTextStyle(context))
//            Text("• Long press a user to close the current chat. 🔒", style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center))
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                "Note: To optimize Firebase storage, this chat feature currently supports text messages only. 📄",
//                style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center)
//            )
//            Spacer(modifier = Modifier.height(24.dp)) // Add space after chat features
//
//            Text(
//                "• Perhaps you may need to select a random user below to start chatting! 😀🗨️",
//                style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center)
//            )
//            Spacer(modifier = Modifier.height(24.dp)) // Add space after chat features
//
//            if (usersLoading) {
//                CircularProgressIndicator(
//                    color = CC.textColor(),
//                    strokeWidth = 1.dp,
//                    modifier = Modifier.size(30.dp)
//                )
//            } else {
//                LazyRow(
//                    modifier = Modifier.height(50.dp),
//                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
//                ) {
//                    items(users ?: emptyList()) { user -> // Handle null or empty list
//                        Box(
//                            modifier = Modifier
//                                .clickable {
//                                    TargetUser.targetUserId.value = user.id
//                                }
//                                .size(50.dp)
//                                .border(1.dp, CC.textColor(), CircleShape)
//                                .clip(CircleShape)
//                        ) {
//                            ProfileImage(currentUser = user)
//                        }
//                    }
//                }
//            }
//            Spacer(modifier = Modifier.height(24.dp)) // Add space after LazyRow
//
//            Text("Happy Chatting! 🎉", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold))
//        }
//}