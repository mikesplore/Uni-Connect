package com.mike.uniadmin.uniChat.userChat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.groupchat.generateConversationId
import com.mike.uniadmin.dataModel.userchat.DeliveryStatus
import com.mike.uniadmin.dataModel.userchat.MessageEntity
import com.mike.uniadmin.dataModel.userchat.MessageViewModel
import com.mike.uniadmin.dataModel.userchat.MessageViewModel.MessageViewModelFactory
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.Background
import com.mike.uniadmin.uniChat.userChat.userChatComponents.ChatInput
import com.mike.uniadmin.uniChat.userChat.userChatComponents.MessageBubble
import com.mike.uniadmin.uniChat.userChat.userChatComponents.RowDate
import com.mike.uniadmin.uniChat.userChat.userChatComponents.SearchTextField
import kotlinx.coroutines.launch
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun UserChatScreen(navController: NavController, context: Context, targetUserId: String) {
    val messageAdmin = context.applicationContext as? UniAdmin
    val messageRepository = remember { messageAdmin?.messageRepository }
    val messageViewModel: MessageViewModel = viewModel(
        factory = MessageViewModelFactory(
            messageRepository ?: throw IllegalStateException("ChatRepository is null")
        )
    )

    val userAdmin = context.applicationContext as? UniAdmin
    val userRepository = remember { userAdmin?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("UserRepository is null")
        )
    )

    val messages by messageViewModel.messages.observeAsState(emptyList())
    val user by userViewModel.user.observeAsState()
    val user2 by userViewModel.user2.observeAsState()
    val userState by userViewModel.userState.observeAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser

    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()

    var myUserState by remember { mutableStateOf("") }
    val typing by messageViewModel.isTyping.observeAsState(false)

    myUserState = when {
        userState == null -> "Never online"
        userState!!.online == "online" -> "Online"
        else -> "Last seen ${CC.getRelativeDate(CC.getCurrentDate(userState!!.lastDate))} at ${CC.getFormattedTime(userState!!.lastTime)}"
    }

    LaunchedEffect(targetUserId) {
        currentUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }

        userViewModel.findUserByAdmissionNumber(targetUserId)
        userViewModel.checkUserStateByID(targetUserId)
    }

    // Generate a unique conversation ID only if user and user2 are not null
    val conversationId = remember(user, user2) {
        if (user != null && user2 != null) {
            "Direct Messages/${generateConversationId(user!!.id, targetUserId)}"
        } else {
            ""
        }
    }

    val typingStatusID = remember(user, user2) {
        if (user != null && user2 != null) {
            "Typing Status/${generateConversationId(user!!.id, targetUserId)}"
        } else {
            ""
        }
    }

    LaunchedEffect(conversationId) {
        if (conversationId.isNotEmpty()) {
            messageViewModel.fetchMessages(conversationId)
            messageViewModel.listenForTypingStatus(typingStatusID, targetUserId)
        }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            scrollState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage(messageContent: String) {
        try {
            MyDatabase.generateChatID { chatId ->
                val newMessage = MessageEntity(
                    path = conversationId,
                    id = chatId,
                    message = messageContent,
                    senderID = user?.id.orEmpty(),
                    recipientID = targetUserId,
                    timeStamp = CC.getTimeStamp(),
                    date = CC.getTimeStamp(),
                    deliveryStatus = DeliveryStatus.SENT
                )
                messageViewModel.saveMessage(newMessage, conversationId) { success ->
                    if (success) {
                        Log.d("MessageViewModel", "Message sent successfully")
                        // Once the message is sent, update it to delivered when the receiver gets it
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Failed to send message")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Failed to send message: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = {
            if (user2 != null) {
                TopAppBarComponent(
                    navController = navController,
                    name = user2!!.firstName,
                    context = context,
                    user = user2!!,
                    userState = myUserState,
                    onValueChange = {
                        isSearchVisible = !isSearchVisible
                    },
                    isTyping = typing
                )
            }
        }, containerColor = CC.primary()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            Background()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .imePadding(),
            ) {
                if (isSearchVisible) {
                    SearchTextField(
                        searchQuery = searchQuery,
                        onValueChange = { searchQuery = it },
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f), state = scrollState
                ) {
                    // Group messages by formatted date string
                    val groupedMessages = messages.groupBy { message ->
                        // Format the timestamp to a date string
                        CC.getCurrentDate(message.date)
                    }.also { _ ->
                        // Iterate over messages and check if the message should be marked as delivered
                        messages.forEach { message ->
                            // If the current user is not the sender (and thus the receiver), or if it's a self-chat
                            if (message.senderID != user?.id || message.senderID == message.recipientID) {
                                messageViewModel.onMessageReceived(message, conversationId)
                            }
                        }
                    }


                    // Iterate over each group of messages by date
                    groupedMessages.forEach { (_, chatsForDate) ->
                        // Get the original timestamp for the first message in the group
                        val originalTimestamp = chatsForDate.first().date

                        item {
                            RowDate(originalTimestamp, context) // Pass the original timestamp to RowDate
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(chatsForDate.filter {
                            it.message.contains(searchQuery, ignoreCase = true)
                        }) { chat ->
                            MessageBubble(
                                message = chat,
                                isUser = chat.senderID == user?.id,
                                context = context,
                                messageViewModel = messageViewModel,
                                path = conversationId,
                                senderID = user?.id.orEmpty()
                            )
                            Spacer(modifier = Modifier.height(5.dp))

                        }
                    }
                }

                ChatInput(
                    modifier = Modifier.fillMaxWidth(),
                    onMessageChange = { message = it },
                    sendMessage = { sendMessage(message) },
                    context = context,
                    isTypingChange = { isTyping -> user?.id?.let {
                        messageViewModel.updateTypingStatus(typingStatusID, it, isTyping)
                    } }
                )
            }
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(
    navController: NavController,
    name: String,
    context: Context,
    user: UserEntity,
    userState: String,
    isSearchVisible: Boolean = false,
    onValueChange: (Boolean) -> Unit,
    isTyping: Boolean

) {
    TopAppBar(title = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            IconButton(onClick = {
                navController.navigate("uniChat"){
                    popUpTo("chat/${user.id}"){
                        inclusive = true
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = CC.textColor()
                )
            }
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)
            ) {
                if (user.profileImageLink.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profileImageLink),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.student), // Replace with your profile icon
                        contentDescription = "Profile Icon",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
            ) {
                Text(name, style = CC.titleTextStyle(context).copy(fontSize = 18.sp))
                Spacer(modifier = Modifier.height(5.dp))
                if (isTyping) {
                    Text("Typing...", style = CC.descriptionTextStyle(context).copy(fontSize = 10.sp))
                } else{
                    Text(userState, style = CC.descriptionTextStyle(context).copy(fontSize = 10.sp))
                }
            }
        }
    },
        actions = {
            IconButton(onClick = { onValueChange(!isSearchVisible) }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
            val intent = Intent(Intent.ACTION_DIAL)
            IconButton(onClick = {
                if (user.phoneNumber != "") {
                    intent.data = Uri.parse("tel:${user.phoneNumber}")
                    context.startActivity(intent)
                }

                context.startActivity(intent)
            }) {
                Icon(
                    imageVector = Icons.Default.Call, // Replace with call icon
                    contentDescription = "Call",
                    tint = CC.textColor(),
                )
            }
        },
        modifier = Modifier.height(70.dp),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary())
    )
}



