package com.mike.uniadmin.chat

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
import java.time.Instant
import com.mike.uniadmin.ui.theme.CommonComponents as CC

object TargetUser{
    var targetUserId: MutableState<String> = mutableStateOf("")
}

@Composable
fun UniChat(navController: NavController, context: Context) {
    val messageAdmin = context.applicationContext as UniAdmin
    val messageRepository = remember { messageAdmin.messageRepository }
    val messageViewModel: MessageViewModel = viewModel(
        factory = MessageViewModelFactory(
            messageRepository
        )
    )

    val userRepository = remember { messageAdmin.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository
        )
    )

    var columnExpanded by remember { mutableStateOf(false) }
    val columnSize by animateDpAsState(
        targetValue = if (columnExpanded) 200.dp else 70.dp,
        animationSpec = tween(500), label = ""
    )

    val currentUser by userViewModel.user.observeAsState()
    val users by userViewModel.users.observeAsState()
    val usersLoading by userViewModel.isLoading.observeAsState(false)
    LaunchedEffect(key1 = Unit) {
       userViewModel.findUserByEmail(FirebaseAuth.getInstance().currentUser?.email!!){}
    }

    Scaffold(content = {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(CC.primary())
                .padding(it)
        ) {
            // Column for users
            ExpandingColumn(
                userViewModel = userViewModel,
                messageViewModel = messageViewModel,
                modifier = Modifier.widthIn(max = columnSize),
                context = context,
                columnSize = columnSize,
                columnExpanded = columnExpanded,
                onColumnClick = { columnExpanded = !columnExpanded },
                currentUser = currentUser,
                onHomeClick = {navController.navigate("homeScreen")}

            )

            // Column for chats
            Column(
                modifier = Modifier
                    .background(CC.extraColor1())
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(
                        visible = TargetUser.targetUserId.value.isEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxSize()
                                .padding(16.dp), // Add padding for better spacing
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(R.drawable.logo),
                                contentDescription = "App Logo" // Add content description
                            )
                            Spacer(modifier = Modifier.height(16.dp)) // Add space after logo

                            Text(
                                text = "Uni Chat 🎓",
                                style = CC.titleTextStyle(context).copy(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                            )
                            Spacer(modifier = Modifier.height(8.dp)) // Add space after title

                            Text(
                                text = "Select a user to start chatting! 📱",
                                style = CC.descriptionTextStyle(context).copy(fontSize = 16.sp)
                            )
                            Spacer(modifier = Modifier.height(24.dp)) // Add space before subtitles

                            // User Status Indicators
                            Text("User Status Indicators", style = CC.titleTextStyle(context).copy(fontSize = 18.sp))
                            Spacer(modifier = Modifier.height(12.dp))

                            StatusIndicatorRow(color = CC.secondary(), text = "Recently Online", context)
                            StatusIndicatorRow(color = Color.Green, text = "Currently Online", context)
                            StatusIndicatorRow(color = Color.Red, text = "Never Been Online ", context)
                            Spacer(modifier = Modifier.height(24.dp))

                            // Chat Feature Information
                            Text("Chat Features", style = CC.titleTextStyle(context).copy(fontSize = 18.sp), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("• Click a user to open the chat. 💬", style = CC.descriptionTextStyle(context))
                            Text("• Long press a user to close the current chat. 🔒", style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Note: To optimize Firebase storage, this chat feature currently supports text messages only. 📄",
                                style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center)
                            )
                            Spacer(modifier = Modifier.height(24.dp)) // Add space after chat features

                            Text(
                                "• Perhaps you may need to select a random user below to start chatting! 😀🗨️",
                                style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center)
                            )
                            Spacer(modifier = Modifier.height(24.dp)) // Add space after chat features

                            if (usersLoading) {
                                CircularProgressIndicator(
                                    color = CC.textColor(),
                                    strokeWidth = 1.dp,
                                    modifier = Modifier.size(30.dp)
                                )
                            } else {
                                LazyRow(
                                    modifier = Modifier.height(50.dp),
                                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                                ) {
                                    items(users ?: emptyList()) { user -> // Handle null or empty list
                                        Box(
                                            modifier = Modifier
                                                .clickable { TargetUser.targetUserId.value = user.id }
                                                .size(50.dp)
                                                .border(1.dp, CC.textColor(), CircleShape)
                                                .clip(CircleShape)
                                        ) {
                                            ProfileImage(currentUser = user)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp)) // Add space after LazyRow

                            Text("Happy Chatting! 🎉", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold))
                        }
                    }




                    AnimatedVisibility(
                        visible = TargetUser.targetUserId.value.isNotEmpty(),
                        enter = slideInHorizontally(
                            animationSpec = tween(500),
                            initialOffsetX = { fullWidth -> -fullWidth } // Slide in from left
                        ),
                        exit = slideOutHorizontally(
                            animationSpec = tween(500),
                            targetOffsetX = { fullWidth -> fullWidth } // Slide out to right
                        )
                    ) {
                        UserChatScreen(
                            navController = navController,
                            context = context,
                            targetUserId = TargetUser.targetUserId.value
                        )
                    }
                }
            }
        }
    })
}

// Helper Composable for Status Indicator Rows
@Composable
fun StatusIndicatorRow(color: Color, text: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(30.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp))
    }
}


@Composable
fun ExpandingColumn(
    modifier: Modifier = Modifier,
    context: Context,
    currentUser: UserEntity?,
    columnSize: Dp,
    columnExpanded: Boolean,
    onColumnClick: () -> Unit,
    onHomeClick: () -> Unit, // Add a lambda for home navigation
    userViewModel: UserViewModel,
    messageViewModel: MessageViewModel
) {
    val userLoading by userViewModel.isLoading.observeAsState(false)
    val users by userViewModel.users.observeAsState(emptyList())
    val userStates by userViewModel.userStates.observeAsState(emptyMap())

    // Fetch user statuses and users
    LaunchedEffect(Unit) {
        userViewModel.checkAllUserStatuses()
        userViewModel.fetchUsers()
    }

    // Gradient background brush
    val brush = Brush.verticalGradient(
        colors = listOf(CC.primary(), CC.secondary())
    )

    Box(
        modifier = modifier
            .background(brush)
            .width(columnSize)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .width(columnSize)
                    .background(
                        CC.primary().copy(0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(CC.secondary())
                        .border(1.dp, CC.secondary(), CircleShape)
                        .clickable { onColumnClick() }
                ) {
                    if (userLoading) {
                        CircularProgressIndicator(
                            color = CC.textColor(),
                            strokeWidth = 1.dp,
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        ProfileImage(currentUser)
                    }
                }
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(visible = columnExpanded) {
                        currentUser?.firstName?.let {
                            Text(
                                text = it,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Header for users with messages
            Text(
                "Continue Chatting",
                style = CC.descriptionTextStyle(context).copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            // LazyColumn to display user messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxSize()
            ) {
                // Display users with messages
                items(users) { user ->
                    val conversationId =
                        "Direct Messages/${currentUser?.id?.let { generateConversationId(it, user.id) }}"

                    // Observe messages for this user
                    val messages by messageViewModel.getCardMessages(conversationId).observeAsState(emptyList())

                    // Fetch messages for each user when the conversation ID changes
                    LaunchedEffect(conversationId) {
                        messageViewModel.fetchCardMessages(conversationId)
                        Log.d("ExpandingColumn", "Fetching messages for $conversationId")
                    }

                    // Sort messages by timestamp
                    val sortedMessages = messages.sortedBy { it.timeStamp }

                    if (sortedMessages.isNotEmpty()) {
                        val latestMessage = sortedMessages.lastOrNull()
                        UserMessageCard(
                            userEntity = user,
                            latestMessage = latestMessage,
                            userState = userStates[user.id],
                            columnExpanded = columnExpanded,
                            columnSize = columnSize,
                            onColumnClick = onColumnClick,
                            context = context,
                            userViewModel = userViewModel
                        )
                    }
                }

                // Divider and "Start Chat" section
                item {
                    HorizontalDivider(
                        color = CC.textColor().copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = "Start Chat",
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }

                // Display users without messages
                items(users) { user ->
                    val conversationId =
                        "Direct Messages/${currentUser?.id?.let { generateConversationId(it, user.id) }}"

                    val messages by messageViewModel.getCardMessages(conversationId).observeAsState(emptyList())

                    if (messages.isEmpty()) {
                        UserMessageCard(
                            userEntity = user,
                            latestMessage = null,
                            userState = userStates[user.id],
                            columnExpanded = columnExpanded,
                            columnSize = columnSize,
                            onColumnClick = onColumnClick,
                            context = context,
                            userViewModel = userViewModel
                        )
                    }
                }
            }
        }

        // Fixed TextButton at the bottom for navigating to the home screen
        TextButton(
            onClick = onHomeClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(CC.primary())
                .padding(16.dp)
        ) {
            if (columnExpanded) {
            Text(
                "Go to Home",
                style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                color = CC.textColor()
            )}
            else {
                Icon(Icons.Default.Home, contentDescription = "Go to Home", tint = CC.textColor(),
                    modifier = Modifier.size(50.dp))
            }
        }
    }
}







@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserMessageCard(
    userEntity: UserEntity,
    latestMessage: MessageEntity?,
    userState: UserStateEntity?,
    columnExpanded: Boolean,
    columnSize: Dp,
    onColumnClick: () -> Unit,
    context: Context,
    userViewModel: UserViewModel
) {
    val currentUser by userViewModel.user.observeAsState()

    Row(
        modifier = Modifier
            .width(columnSize)
            .height(80.dp)
            .padding(8.dp)
            .background(CC.primary(), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .combinedClickable(
                onLongClick = {
                    TargetUser.targetUserId.value = ""
                }
            ) {
                TargetUser.targetUserId.value = userEntity.id
                onColumnClick()
            }
            .border(
                1.dp,
                CC.secondary(),
                CircleShape
            )
            .clip(CircleShape)
            .size(if (columnExpanded) 40.dp else 30.dp)) {
            ProfileImage(currentUser = userEntity)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Use AnimatedVisibility to animate the visibility of the expanded content
        AnimatedVisibility(visible = columnExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .size(5.dp)
            ){
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = if (userEntity.id == currentUser?.id) "You" else userEntity.firstName,
                    style = CC.descriptionTextStyle(context),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                latestMessage?.message?.let {
                    Text(
                        text = createAnnotatedMessage(createAnnotatedText(it).toString()),
                        style = CC.descriptionTextStyle(context).copy(
                            color = CC.textColor().copy(0.5f),
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val currentTime = System.currentTimeMillis()
                val messageTime = latestMessage?.timeStamp?.toLongOrNull() ?: 0L

                val timeDifference = java.time.Duration.between(
                    Instant.ofEpochMilli(messageTime),
                    Instant.ofEpochMilli(currentTime)
                )
                val timeText = when {
                    timeDifference.toMinutes() < 60 -> "${timeDifference.toMinutes()} mins ago"
                    timeDifference.toHours() < 24 -> "${timeDifference.toHours()} hour${if (timeDifference.toHours() > 1) "s" else ""} ago"
                    else -> CC.getCurrentTime(messageTime.toString()) // Display the actual time if older than a day
                }

                Text(
                    text = timeText, style = CC.descriptionTextStyle(context).copy(fontSize = 10.sp)
                )
            }
                val backColor by animateColorAsState(
                    targetValue = if (userState?.online == "online") Color.Green else if (userState?.online == "offline") CC.secondary() else Color.Red,
                    animationSpec = tween(durationMillis = 500), label = ""
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(10.dp)
                        .align(Alignment.TopEnd)
                        .background(backColor)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
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
            SpanStyle(color = CC.textColor().copy(alpha = 0.5f)), startIndex, message.length
        ) // Apply color to non-emoji text
    }
}