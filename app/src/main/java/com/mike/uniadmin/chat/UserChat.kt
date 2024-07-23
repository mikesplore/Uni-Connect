package com.mike.uniadmin.chat

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.dataModel.groupchat.generateConversationId
import com.mike.uniadmin.dataModel.userchat.Message
import com.mike.uniadmin.dataModel.userchat.MessageViewModel
import com.mike.uniadmin.dataModel.userchat.MessageViewModel.MessageViewModelFactory
import com.mike.uniadmin.dataModel.users.User
import com.mike.uniadmin.dataModel.users.UserRepository
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.ExitScreen
import com.mike.uniadmin.ui.theme.GlobalColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Call
import com.mike.uniadmin.dataModel.userchat.MessageRepository
import com.mike.uniadmin.ui.theme.Background
import com.mike.uniadmin.CommonComponents as CC


@Composable
fun UserChatScreen(navController: NavController, context: Context, targetUserId: String) {
    val messageRepository = remember { MessageRepository() }
    val userRepository = remember { UserRepository() }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
    val messageViewModel: MessageViewModel = viewModel(factory = MessageViewModelFactory(messageRepository))

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

    val startTime = remember { System.currentTimeMillis() }
    var timeSpent by remember { mutableLongStateOf(0L) }
    val screenID = "SC5"
    val scrollState = rememberLazyListState()

    var myUserState  by remember { mutableStateOf("") }
    if(userState != null){
        if(userState!!.online == "online"){
            myUserState = "Online"
        }else{
            myUserState = "Last seen ${userState!!.lastTime}"
        }
    }

    LaunchedEffect(targetUserId) {
        GlobalColors.loadColorScheme(context)
        currentUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }
        userViewModel.findUserByAdmissionNumber(targetUserId)
        userViewModel.checkUserStateByID(targetUserId)
        while (true) {
            timeSpent = System.currentTimeMillis() - startTime
            delay(1000)
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

    // Generate a unique conversation ID only if user and user2 are not null
    val conversationId = remember(user, user2) {
        if (user != null && user2 != null) {
            "Direct Messages/${generateConversationId(user!!.id, targetUserId)}"
        } else {
            ""
        }
    }

    LaunchedEffect(conversationId) {
        if (conversationId.isNotEmpty()) {
            messageViewModel.fetchMessages(conversationId)
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
                val newMessage = Message(
                    id = chatId,
                    message = messageContent,
                    senderName = user?.firstName.orEmpty(),
                    senderID = user?.id.orEmpty(),
                    recipientID = targetUserId,
                    time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                    date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
                    profileImageLink = user?.profileImageLink.orEmpty()
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
                    name = user2!!.firstName,
                    navController = navController,
                    context = context,
                    user = user2!!,
                    userState = myUserState,
                    onValueChange = {
                        isSearchVisible = !isSearchVisible
                    }
                )
            }
        },
        containerColor = CC.primary()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            Background(context)
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
                    modifier = Modifier.weight(1f),
                    state = scrollState
                ) {
                    val groupedMessages = messages.groupBy { it.date }

                    groupedMessages.forEach { (date, chatsForDate) ->
                        item {
                            RowMessage(context)
                            Spacer(modifier = Modifier.height(8.dp))
                            RowDate(date, context)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(chatsForDate.filter {
                            it.message.contains(searchQuery, ignoreCase = true)
                        }) { chat ->
                            MessageBubble(
                                message = chat,
                                isUser = chat.senderID == user?.id,
                                context = context
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
                ChatInput(
                    modifier = Modifier.fillMaxWidth(),
                    onMessageChange = { message = it },
                    sendMessage = { sendMessage(message) },
                    context = context
                )
            }
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
    userState: String,
    isSearchVisible: Boolean = false,
    onValueChange: (Boolean) -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(40.dp)
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
                Column {
                Text(name, style = CC.titleTextStyle(context))
                Text(userState, style = CC.descriptionTextStyle(context))}
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
        navigationIcon = {
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "",
                    tint = CC.textColor()
                )
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
    TopAppBarComponent(
        name = "Student",
        navController = NavController(LocalContext.current),
        context = LocalContext.current,
        user = User(
            id = "123456789",
            firstName = "Student",
            lastName = "Name",
            email = "",
            phoneNumber = "",
            gender = "",
            profileImageLink = ""
        ),
        isSearchVisible = false,
        onValueChange = {},
        userState = "Online"
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


