package com.mike.uniadmin.uniChat.userChat

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.backEnd.groupchat.generateConversationId
import com.mike.uniadmin.backEnd.userchat.DeliveryStatus
import com.mike.uniadmin.backEnd.userchat.UserChatEntity
import com.mike.uniadmin.getUserChatViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.Background
import com.mike.uniadmin.uniChat.userChat.userChatComponents.ChatInput
import com.mike.uniadmin.uniChat.userChat.userChatComponents.MessageBubble
import com.mike.uniadmin.uniChat.userChat.userChatComponents.RowDate
import com.mike.uniadmin.uniChat.userChat.userChatComponents.SearchTextField
import com.mike.uniadmin.uniChat.userChat.userChatComponents.TopAppBarComponent
import kotlinx.coroutines.launch
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun UserChatScreen(navController: NavController, context: Context, targetUserId: String) {

    val userGroupChatViewModel = getUserChatViewModel(context)
    val userViewModel = getUserViewModel(context)

    val messages by userGroupChatViewModel.userChats.observeAsState(emptyList())
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
    val typing by userGroupChatViewModel.isTyping.observeAsState(false)

    myUserState = when {
        userState == null -> "Never online"
        userState!!.online == "online" -> "Online"
        else -> "Last seen ${CC.getRelativeDate(CC.getCurrentDate(userState!!.lastDate))} at ${
            CC.getFormattedTime(
                userState!!.lastTime
            )
        }"
    }

    LaunchedEffect(targetUserId) {
        currentUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }

        userViewModel.findUserByAdmissionNumber(targetUserId) {}
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
            userGroupChatViewModel.fetchUserChats(conversationId)
            userGroupChatViewModel.listenForTypingStatus(typingStatusID, targetUserId)
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
                val newMessage = UserChatEntity(
                    path = conversationId,
                    id = chatId,
                    message = messageContent,
                    senderID = user?.id.orEmpty(),
                    recipientID = targetUserId,
                    timeStamp = CC.getTimeStamp(),
                    date = CC.getTimeStamp(),
                    deliveryStatus = DeliveryStatus.SENT
                )
                userGroupChatViewModel.saveMessage(newMessage, conversationId) { success ->
                    if (success) {
                        Log.d("userGroupChatViewModel", "Message sent successfully")
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
                    val groupedMessages = messages.groupBy { message ->
                        CC.getCurrentDate(message.date)
                    }.also { _ ->
                        messages.forEach { message ->
                            // If the current user is not the sender (and thus the receiver), or if it's a self-chat
                            if (message.senderID != user?.id || message.senderID == message.recipientID) {
                                userGroupChatViewModel.markMessageAsRead(message, conversationId)
                            }
                        }
                    }


                    groupedMessages.forEach { (_, chatsForDate) ->
                        // Get the original timestamp for the first message in the group
                        val originalTimestamp = chatsForDate.first().date

                        item {
                            RowDate(
                                originalTimestamp,
                                context
                            ) // Pass the original timestamp to RowDate
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(chatsForDate.filter {
                            it.message.contains(searchQuery, ignoreCase = true)
                        }) { chat ->
                            MessageBubble(
                                message = chat,
                                isUser = chat.senderID == user?.id,
                                context = context,
                                userGroupChatViewModel = userGroupChatViewModel,
                                path = conversationId,
                                senderID = user?.id.orEmpty()
                            )
                            Spacer(modifier = Modifier.height(5.dp))

                        }
                    }
                }

                ChatInput(modifier = Modifier.fillMaxWidth(),
                    onMessageChange = { message = it },
                    sendMessage = { sendMessage(message) },
                    context = context,
                    isTypingChange = { isTyping ->
                        user?.id?.let {
                            userGroupChatViewModel.updateTypingStatus(typingStatusID, it, isTyping)
                        }
                    })
            }
        }
    }
}








