package com.mike.uniadmin.chat

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.mike.uniadmin.ui.theme.CommonComponents as CC

object TargetUser {
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

    var expanded by remember { mutableStateOf(false) }
    val userStates by userViewModel.userStates.observeAsState(emptyMap())
    val currentUser by userViewModel.user.observeAsState()
    val users by userViewModel.users.observeAsState()
    val usersLoading by userViewModel.isLoading.observeAsState(false)
    var text by remember { mutableStateOf("") } // State for text



    LaunchedEffect(Unit) {
        userViewModel.findUserByEmail(FirebaseAuth.getInstance().currentUser?.email!!) {}
        userViewModel.checkAllUserStatuses()
    }

    val brush = Brush.verticalGradient(
        colors = listOf(CC.secondary(), CC.primary())
    )

    Scaffold(containerColor = CC.secondary()) {
        Column(
            modifier = Modifier
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
                    text = "Uni Chat",
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                BasicTextField(value = text,
                    onValueChange = { text = it },
                    cursorBrush = brush,
                    textStyle = CC.descriptionTextStyle(context).copy(fontSize = 16.sp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(35.dp)
                        .border(1.dp, CC.textColor(), RoundedCornerShape(8.dp)),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // Check if the text field is empty to show the placeholder
                            if (text.isEmpty()) {
                                Text(
                                    "Search User",
                                    color = CC.textColor()
                                        .copy(alpha = 0.5f), // Adjust placeholder color as needed
                                    style = CC.descriptionTextStyle(context).copy(fontSize = 16.sp)
                                )
                            }
                            innerTextField()
                        }
                    })

            }
            Column(
                modifier = Modifier
                    .background(CC.primary())
                    .fillMaxSize(1f)
            ) {
                LazyColumn {
                    items(users ?: emptyList()) { user ->
                        val conversationId = "Direct Messages/${
                            currentUser?.id?.let { id ->
                                generateConversationId(
                                    id,
                                    user.id
                                )
                            }
                        }"
                        LaunchedEffect(conversationId) {
                            messageViewModel.fetchCardMessages(conversationId)
                        }
                        val messages by messageViewModel.getCardMessages(conversationId)
                            .observeAsState(emptyList())
                        val sortedMessages =
                            messages.sortedBy { sortedMessage -> sortedMessage.timeStamp }
                        val latestMessage = sortedMessages.lastOrNull()

                        UserMessageCard(userEntity = user,
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


//@Composable
//fun ExpandingColumn(
//    modifier: Modifier = Modifier,
//    context: Context,
//    currentUser: UserEntity?,
//    columnSize: Dp,
//    columnExpanded: Boolean,
//    onColumnClick: () -> Unit,
//    onCloseChat: (Boolean) -> Unit, // Add a lambda for home navigation
//    userViewModel: UserViewModel,
//    messageViewModel: MessageViewModel,
//    navController: NavController
//) {
//    val userLoading by userViewModel.isLoading.observeAsState(false)
//    val users by userViewModel.users.observeAsState(emptyList())
//    val userStates by userViewModel.userStates.observeAsState(emptyMap())
//
//    // Fetch user statuses and users
//    LaunchedEffect(Unit) {
//        userViewModel.checkAllUserStatuses()
//        userViewModel.fetchUsers()
//    }
//
//    // Gradient background brush
//    val brush = Brush.verticalGradient(
//        colors = listOf(CC.primary(), CC.secondary())
//    )
//
//    Box(
//        modifier = modifier
//            .background(brush)
//            .width(columnSize)
//            .fillMaxHeight()
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Box(
//                modifier = Modifier
//                    .height(100.dp)
//                    .width(columnSize)
//                    .background(
//                        CC
//                            .primary()
//                            .copy(0.5f)
//                    ),
//                contentAlignment = Alignment.Center
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(50.dp)
//                        .clip(CircleShape)
//                        .background(CC.secondary())
//                        .border(1.dp, CC.secondary(), CircleShape)
//                        .clickable { onColumnClick() },
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (userLoading) {
//                        CircularProgressIndicator(
//                            color = CC.textColor(),
//                            strokeWidth = 1.dp,
//                            modifier = Modifier.size(30.dp)
//                        )
//                    } else {
//                        ProfileImage(currentUser)
//                    }
//                }
//                Column(
//                    modifier = Modifier.align(Alignment.BottomCenter),
//                    verticalArrangement = Arrangement.Center,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    AnimatedVisibility(visible = columnExpanded) {
//                        currentUser?.firstName?.let {
//                            Text(
//                                text = it,
//                                textAlign = TextAlign.Center
//                            )
//                        }
//                    }
//                }
//            }
//            Spacer(modifier = Modifier.height(10.dp))
//
//            // Header for users with messages
//            Text(
//                "Continue Chatting",
//                style = CC.descriptionTextStyle(context).copy(
//                    fontWeight = FontWeight.Bold,
//                    textAlign = TextAlign.Center
//                ),
//                modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(modifier = Modifier.height(10.dp))
//
//            // LazyColumn to display user messages
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .fillMaxSize()
//            ) {
//                // Display users with messages
//                items(users) { user ->
//                    val conversationId =
//                        "Direct Messages/${currentUser?.id?.let { generateConversationId(it, user.id) }}"
//
//                    // Observe messages for this user
//                    val messages by messageViewModel.getCardMessages(conversationId).observeAsState(emptyList())
//
//                    // Fetch messages for each user when the conversation ID changes
//                    LaunchedEffect(conversationId) {
//                        messageViewModel.fetchCardMessages(conversationId)
//                        Log.d("ExpandingColumn", "Fetching messages for $conversationId")
//                    }
//
//                    // Sort messages by timestamp
//                    val sortedMessages = messages.sortedBy { it.timeStamp }
//
//                    if (sortedMessages.isNotEmpty()) {
//                        val latestMessage = sortedMessages.lastOrNull()
//                        UserMessageCard(
//                            userEntity = user,
//                            latestMessage = latestMessage,
//                            userState = userStates[user.id],
//                            columnExpanded = columnExpanded,
//                            columnSize = columnSize,
//                            context = context,
//                            userViewModel = userViewModel,
//                            onUserClicked = { onCloseChat(false) }
//                        )
//                    }
//                }
//
//                // Divider and "Start Chat" section
//                item {
//                    HorizontalDivider(
//                        color = CC.textColor().copy(alpha = 0.5f),
//                        thickness = 1.dp,
//                        modifier = Modifier.padding(vertical = 8.dp)
//                    )
//                    Text(
//                        text = "Start Chat",
//                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 4.dp)
//                    )
//                }
//
//                // Display users without messages
//                items(users) { user ->
//                    val conversationId =
//                        "Direct Messages/${currentUser?.id?.let { generateConversationId(it, user.id) }}"
//
//                    val messages by messageViewModel.getCardMessages(conversationId).observeAsState(emptyList())
//
//                    if (messages.isEmpty()) {
//                        UserMessageCard(
//                            userEntity = user,
//                            latestMessage = null,
//                            userState = userStates[user.id],
//                            columnExpanded = columnExpanded,
//                            columnSize = columnSize,
//                            context = context,
//                            userViewModel = userViewModel,
//                            onUserClicked = { onCloseChat(false)}
//                        )
//                    }
//                }
//            }
//        }
//
//        // Fixed TextButton at the bottom for navigating to the home screen
//        Row(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .fillMaxWidth()
//                .background(CC.extraColor1())
//                .padding(5.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            if (columnExpanded) {
//                TextButton(onClick = { navController.navigate("homeScreen") }) {
//                    Text("Home", style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold))
//                }
//                Spacer(modifier = Modifier.width(5.dp))
//                TextButton(onClick = {onCloseChat(true)}) {
//                    Text("Close", style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold))
//                }
//                Spacer(modifier = Modifier.width(5.dp))
//                IconButton(onClick = { onColumnClick()}) {
//                    Icon(
//                        Icons.Default.ArrowBackIosNew,
//                        contentDescription = "Close chat",
//                        tint = CC.textColor(),
//                    )
//                }
//
//            } else {
//                IconButton(onColumnClick) {
//                    Icon(
//                        Icons.AutoMirrored.Filled.ArrowForwardIos,
//                        contentDescription = "Open Column",
//                        tint = CC.textColor(),
//                    )
//                }
//
//            }
//        }
//    }
//}


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

    Row(
        modifier = Modifier
            .border(
                1.dp, CC.textColor().copy(alpha = 0.5f), RoundedCornerShape(8.dp)
            )
            .height(85.dp)
            .fillMaxWidth()
            .padding(8.dp)
            .background(CC.primary(), shape = RoundedCornerShape(8.dp))
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
                .clickable {
                    navController.navigate("chat/${userEntity.id}")

                }
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (userEntity.id == currentUser?.id) "You" else userEntity.firstName,
                style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            latestMessage?.message?.let {
                val senderName = if (currentUser?.id == userState?.id) "You: "  else ""

                Text(
                    text = "$senderName ${createAnnotatedMessage(createAnnotatedText(it).toString())}",
                    style = CC.descriptionTextStyle(context).copy(
                        color = CC.textColor().copy(alpha = 0.7f),
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            userState?.let { status ->
                val textColor = if (status.online == "online") Color.Green else Color.Red
                Text(
                    text = when {
                        status.online == "online" -> "Online"
                        else -> "${CC.getRelativeDate(CC.getCurrentDate(status.lastDate))} at ${CC.getFormattedTime(status.lastTime)}"
                    },
                    style = CC.descriptionTextStyle(context).copy(fontSize = 10.sp),
                    color = textColor
                )
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