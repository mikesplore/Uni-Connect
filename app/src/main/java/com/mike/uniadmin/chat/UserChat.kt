package com.mike.uniadmin.chat

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.Background
import com.mike.uniadmin.R
import com.mike.uniadmin.model.Message
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.ExitScreen
import com.mike.uniadmin.model.MyDatabase.fetchUserDataByAdmissionNumber
import com.mike.uniadmin.model.MyDatabase.fetchUserDataByEmail
import com.mike.uniadmin.model.MyDatabase.fetchUserToUserMessages
import com.mike.uniadmin.model.MyDatabase.sendUserToUserMessage
import com.mike.uniadmin.model.User
import com.mike.uniadmin.ui.theme.GlobalColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.mike.uniadmin.CommonComponents as CC


@Composable
fun UserChatScreen(navController: NavController, context: Context, targetUserId: String) {
    var user by remember { mutableStateOf(User()) }
    var user2 by remember { mutableStateOf(User()) }
    var name by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(emptyList<Message>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var currentName by remember { mutableStateOf("") }
    var currentAdmissionNumber by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var timeSpent by remember { mutableLongStateOf(0L) }
    val screenID = "SC5"
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(targetUserId) {
        GlobalColors.loadColorScheme(context)
        currentUser?.email?.let { email ->
            fetchUserDataByEmail(email) { fetchedUser ->
                fetchedUser?.let {
                    user = it
                    currentName = it.firstName
                    currentAdmissionNumber = it.id
                }
            }
        }
        fetchUserDataByAdmissionNumber(targetUserId) { fetchedUser ->
            fetchedUser?.let {
                user2 = it
                name = user2.firstName
            }
        }
        while (true) {
            timeSpent = System.currentTimeMillis() - startTime
            delay(1000) // Update every second (adjust as needed)
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            ExitScreen(
                context = context,
                screenID = screenID,
                timeSpent = timeSpent
            )
        }
    }


    //functions for sending and retrieving messages
    // Generate a unique conversation ID for the current user and the target user
    val conversationId =
        "Direct Messages/${generateConversationId(currentAdmissionNumber, targetUserId)}"

    fun fetchMessages(conversationId: String) {
        try {
            fetchUserToUserMessages(conversationId) { fetchedMessages ->
                messages = fetchedMessages
            }
        } catch (e: Exception) {
            errorMessage = e.message
            scope.launch {
                snackbarHostState.showSnackbar("Failed to fetch messages: ${e.message}")
                Log.e(
                    "UserChatScreen",
                    "Failed to fetch messages from $conversationId: ${e.message}",
                    e
                )
            }
        }
    }

    LaunchedEffect(conversationId) {
        GlobalColors.loadColorScheme(context)
        while (true) {
            fetchMessages(conversationId)
            delay(100) // Adjust the delay as needed
        }
    }

    fun sendMessage(messageContent: String) {
        try {
            MyDatabase.generateChatID { chatId ->
                val newMessage = Message(
                    id = chatId,
                    message = messageContent,
                    senderName = user.firstName,
                    senderID = currentAdmissionNumber,
                    recipientID = targetUserId,
                    time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                    date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
                    profileImageLink = ""
                )
                sendUserToUserMessage(newMessage, conversationId) { success ->
                    if (success) {
                        fetchMessages(conversationId)
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Failed to send message")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = e.message
            scope.launch {
                snackbarHostState.showSnackbar("Failed to send message: ${e.message}")
            }
        }
    }


        Box{
            Background(context)
            val scrollState = rememberLazyListState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),

            ) {
                TopAppBarComponent(
                    name,
                    navController,
                    context,
                    user,
                    onValueChange = {isSearchVisible = !isSearchVisible}
                )
                if (isSearchVisible) {
                    SearchTextField(
                        searchQuery = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    state = scrollState
                ) {
                    val groupedMessages = messages.groupBy { it.date }

                    groupedMessages.forEach { (date, chatsForDate) ->
                        item {
                            // Display date header
                            RowDate(date, context)
                            Spacer(modifier = Modifier.height(8.dp))
                            RowMessage(context)
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        items(chatsForDate.filter {
                            it.message.contains(searchQuery, ignoreCase = true)
                        }) { chat ->
                            MessageBubble(
                                message = chat,
                                isUser = chat.senderID == currentAdmissionNumber,
                                context = context
                            )
                        }
                    }
                }
                ChatInput(
                    modifier = Modifier.fillMaxWidth(),
                    onMessageChange = { message = it },
                    sendMessage = { sendMessage(message) },
                    context
                )
            }

    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MessageBubble(
    message: Message,
    isUser: Boolean,
    context: Context,
) {
    val alignment = if (isUser) Alignment.TopEnd else Alignment.TopStart
    val backgroundColor = if (isUser) CC.extraColor1() else CC.extraColor2()
    val bubbleShape = RoundedCornerShape(
        bottomStart = 16.dp,
        bottomEnd = 16.dp,
        topStart = if (isUser) 16.dp else 0.dp,
        topEnd = if (isUser) 0.dp else 16.dp
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor, bubbleShape)
                .padding(8.dp)
                .align(alignment)
        ) {
            Column {
                Text(
                    text = message.message, style = CC.descriptionTextStyle(context)
                )
                Text(
                    text = message.time,
                    style = CC.descriptionTextStyle(context),
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SearchTextField(
    searchQuery: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
){
    TextField(value = searchQuery,
        onValueChange = { onValueChange(searchQuery) },
        label = { Text("Search Chats") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CC.primary(),
            unfocusedIndicatorColor = CC.textColor(),
            focusedIndicatorColor = CC.secondary(),
            unfocusedContainerColor = CC.primary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedLabelColor = CC.secondary(),
            unfocusedLabelColor = CC.textColor()
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(10.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarComponent(
    name: String,
    navController: NavController,
    context: Context,
    user: User,
    isSearchVisible: Boolean = false,
    onValueChange: (Boolean) -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                if (user.profileImageLink.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profileImageLink),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.student), // Replace with your profile icon
                        contentDescription = "Profile Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(name, style = CC.titleTextStyle(context))
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
            IconButton(onClick = { /* TODO: Handle more options click */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(Icons.Default.ArrowBackIosNew,"",
                    tint = CC.textColor())
            }
        },
        modifier = Modifier.height(70.dp),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary())
    )
}

@Composable
fun ChatInput(modifier: Modifier = Modifier, onMessageChange: (String) -> Unit, sendMessage: (String) -> Unit, context: Context) {

    var input by remember { mutableStateOf(TextFieldValue("")) }
    val textEmpty by remember { derivedStateOf { input.text.isEmpty() }
    }

    Row(
        modifier = modifier
            .padding(horizontal = 5.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {

        ChatTextField(modifier = modifier.weight(1f),
            context,
            input = input,
            onValueChange = {
                input = it
            },
            )

        Spacer(modifier = Modifier.width(6.dp))

        FloatingActionButton(modifier = Modifier.size(48.dp),
            containerColor = CC.extraColor1(),
            onClick = {
                if (!textEmpty) {
                    onMessageChange(input.text)
                    sendMessage(input.text)
                    input = TextFieldValue("")
                }
            }) {
            androidx.compose.material.Icon(
                tint = CC.textColor(), imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null
            )
        }
    }
}

@Composable
fun ChatTextField(
    modifier: Modifier = Modifier,
    context: Context,
    input: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {

    OutlinedTextField(
        value = input,
        textStyle = CC.descriptionTextStyle(context),
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(56.dp),
        placeholder = { Text(text = "Message", style = CC.descriptionTextStyle(context)) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CC.primary(),
            unfocusedIndicatorColor = CC.textColor(),
            focusedIndicatorColor = CC.secondary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            unfocusedContainerColor = CC.primary(),

        ),
        shape = RoundedCornerShape(24.dp),
        singleLine = true
    )
}

@Preview
@Composable
fun MyChatPreview()
{
    ChatInput(
        modifier = Modifier
            .width(300.dp)
            .height(48.dp)
        ,
        onMessageChange = {},
        sendMessage = {},
        LocalContext.current

    )
}


@Composable
fun RowMessage(context: Context){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    CC.secondary(), RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Chats are end-to-end encrypted",
                modifier = Modifier.padding(5.dp),
                style = CC.descriptionTextStyle(context),
                textAlign = TextAlign.Center,
                color = CC.textColor()
            )
        }
    }
}

@Composable
fun RowDate(date: String, context: Context){
    fun formatDate(dateString: String): String {
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val yesterday = SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)) // Yesterday's date

        return when (dateString) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> dateString
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    CC.secondary(), RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = formatDate(date),
                modifier = Modifier.padding(5.dp),
                style = CC.descriptionTextStyle(context),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun generateConversationId(userId1: String, userId2: String): String {
    return if (userId1 < userId2) {
        "$userId1$userId2"
    } else {
        "$userId2$userId1"
    }
}

