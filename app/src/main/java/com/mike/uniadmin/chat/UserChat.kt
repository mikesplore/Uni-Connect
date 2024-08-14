package com.mike.uniadmin.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.CountDownTimer
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.groupchat.generateConversationId
import com.mike.uniadmin.dataModel.userchat.MessageEntity
import com.mike.uniadmin.dataModel.userchat.MessageViewModel
import com.mike.uniadmin.dataModel.userchat.MessageViewModel.MessageViewModelFactory
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.Background
import kotlinx.coroutines.delay
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

    myUserState = when{
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
                    id = chatId,
                    message = messageContent,
                    senderID = user?.id.orEmpty(),
                    recipientID = targetUserId,
                    timeStamp = CC.getTimeStamp(),
                    date = CC.getTimeStamp(),
                )
                messageViewModel.saveMessage(newMessage, conversationId) { success ->
                    if (!success) {
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
                    }
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
                        onValueChange = {searchQuery = it},
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f), state = scrollState
                ) {
                    // Group messages by formatted date string
                    val groupedMessages = messages.groupBy { message ->
                        CC.getCurrentDate(message.date) // Format the timestamp to a date string
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
                            Spacer(modifier = Modifier.height(10.dp))
                            AnimatedVisibility(visible = typing,
                                enter = (slideInHorizontally {
                                    -it
                                }),
                                exit = (slideOutHorizontally { -it })

                            ) {
                                TypingIndicator()
                            }
                        }
                    }
                }

                ChatInput(
                    modifier = Modifier.fillMaxWidth(),
                    onMessageChange = { message = it },
                    sendMessage = { sendMessage(message) },
                    context = context,
                    isTypingChange = { isTyping -> user?.id?.let {
                        messageViewModel.updateTypingStatus(typingStatusID,
                            it, isTyping)
                    } }
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MessageBubble(
    message: MessageEntity,
    isUser: Boolean,
    context: Context,
    messageViewModel: MessageViewModel,
    path: String,
    senderID: String) {
    val alignment = if (isUser) Alignment.TopEnd else Alignment.TopStart
    val bubbleShape = RoundedCornerShape(
        bottomStart = 16.dp,
        bottomEnd = 16.dp,
        topStart = if (isUser) 16.dp else 0.dp,
        topEnd = if (isUser) 0.dp else 16.dp
    )

    val senderBrush = Brush.linearGradient(
        colors = listOf(CC.extraColor1(), CC.extraColor2())
    )

    val receiverBrush = Brush.linearGradient(
        colors = listOf(CC.tertiary(), CC.extraColor1())
    )
    val backgroundColor = if (isUser) senderBrush else receiverBrush

    var showDeleteDialog by remember { mutableStateOf(false) }

    fun deleteMessage() {
        messageViewModel.deleteMessage(message.id, path, onSuccess = {
            Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
        })
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    if (message.senderID != senderID)
                        return@combinedClickable
                    showDeleteDialog = true
                }
            ),
        contentAlignment = alignment
    ) {
        val maxBubbleWidth = with(LocalDensity.current) { (maxWidth * 0.75f) }

        Row(
            modifier = Modifier.align(alignment),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time on the left for the sender
            if (isUser) {
                Text(
                    text = CC.getFormattedTime(message.timeStamp),
                    style = CC.descriptionTextStyle(context),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .padding(end = 4.dp)
                )
            }

            // Message bubble
            Box(
                modifier = Modifier
                    .background(backgroundColor, bubbleShape)
                    .widthIn(max = maxBubbleWidth)
                    .padding(8.dp)
            ) {
                SelectionContainer { // Wrap the Text composable with SelectionContainer
                    Text(
                        text = message.message,
                        style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp)
                    )
                }
            }

            // Time on the right for the user
            if (!isUser) {
                Text(
                    text = CC.getFormattedTime(message.timeStamp),
                    style = CC.descriptionTextStyle(context),
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(start = 4.dp)
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            containerColor = CC.primary(),
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Delete Message", style = CC.titleTextStyle(context)) },
            text = { Text(text = "Are you sure you want to delete this message?", style = CC.descriptionTextStyle(context)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteMessage()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", style = CC.descriptionTextStyle(context))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel", style = CC.descriptionTextStyle(context))
                }
            }
        )
    }
}





@Composable
fun SearchTextField(
    searchQuery: String,
    onValueChange: (String) -> Unit,
) {
    TextField(value = searchQuery,
        onValueChange = { onValueChange(it) },
        placeholder = { Text("Search Chats") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CC.primary(),
            unfocusedIndicatorColor = CC.textColor(),
            focusedIndicatorColor = CC.textColor(),
            unfocusedContainerColor = CC.primary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedLabelColor = CC.textColor(),
            unfocusedLabelColor = CC.textColor(),
            cursorColor = CC.textColor()
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(10.dp)
    )
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
                Text(userState, style = CC.descriptionTextStyle(context).copy(fontSize = 10.sp))
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


@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    onMessageChange: (String) -> Unit,
    sendMessage: (String) -> Unit,
    context: Context,
    isTypingChange: (Boolean) -> Unit
) {
    var input by remember { mutableStateOf(TextFieldValue("")) }
    var typingTimer: CountDownTimer? by remember { mutableStateOf(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text Field
        BasicTextField(
            value = input,
            onValueChange = { message ->
                input = message

                // Cancel any existing timer
                typingTimer?.cancel()

                // Set typing status to true
                isTypingChange(true)

                // Start a new timer to reset typing status after inactivity
                typingTimer = object : CountDownTimer(3000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        // Set typing status to false after 3 seconds of inactivity
                        isTypingChange(false)
                    }
                }.start()
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
                .background(CC.secondary(), RoundedCornerShape(24.dp))
                .heightIn(min = 40.dp),
            textStyle = CC.descriptionTextStyle(context),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (input.text.isEmpty()) {
                        Text(
                            text = "Message",
                            style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp)
                        )
                    }
                    innerTextField()
                }
            },
            cursorBrush = SolidColor(CC.textColor())
        )

        // Send Button
        IconButton(
            onClick = {
                if (input.text.isNotBlank()) {
                    onMessageChange(input.text)
                    sendMessage(input.text)
                    input = TextFieldValue("") // Clear input field
                    isTypingChange(false) // Set typing status to false after sending message
                }
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(CC.secondary())
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = CC.extraColor2()
            )
        }
    }
}




@Composable
fun TypingIndicator() {
    val animationDuration = 500 // duration for each dot animation
    val delayDuration = 150L // delay between each dot's animation

    // Remembering the states for each dot's color
    var colorState1 by remember { mutableStateOf(Color.Gray) }
    var colorState2 by remember { mutableStateOf(Color.Gray) }
    var colorState3 by remember { mutableStateOf(Color.Gray) }

    // Coroutine to animate color change in sequence
    LaunchedEffect(Unit) {
        while (true) {
            colorState1 = Color.Gray
            colorState2 = Color.Gray
            colorState3 = Color.Gray

            colorState1 = Color.Black
            delay(delayDuration)
            colorState2 = Color.Black
            delay(delayDuration)
            colorState3 = Color.Black
            delay(delayDuration)
        }
    }

    // Applying the color animation
    val animatedColor1 by animateColorAsState(
        targetValue = colorState1,
        animationSpec = tween(animationDuration), label = ""
    )
    val animatedColor2 by animateColorAsState(
        targetValue = colorState2,
        animationSpec = tween(animationDuration), label = ""
    )
    val animatedColor3 by animateColorAsState(
        targetValue = colorState3,
        animationSpec = tween(animationDuration), label = ""
    )

    // Row containing the three dots
    Row(
        modifier = Modifier
            .background(CC.secondary(), RoundedCornerShape(10.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier
                .padding(2.dp)
                .size(8.dp),
            shape = CircleShape,
            color = animatedColor1
        ) {}

        Surface(
            modifier = Modifier
                .padding(2.dp)
                .size(8.dp),
            shape = CircleShape,
            color = animatedColor2
        ) {}

        Surface(
            modifier = Modifier
                .padding(2.dp)
                .size(8.dp),
            shape = CircleShape,
            color = animatedColor3
        ) {}
    }
}

@Composable
fun RowDate(date: String, context: Context) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    CC.secondary(), RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center
        ) {
            Text(
                text = CC.getRelativeDate(CC.getCurrentDate(date)),
                modifier = Modifier.padding(5.dp),
                style = CC.descriptionTextStyle(context),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


