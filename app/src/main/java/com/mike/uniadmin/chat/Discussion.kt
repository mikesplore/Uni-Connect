package com.mike.uniadmin.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.UserItem
import com.mike.uniadmin.dataModel.groupchat.ChatEntity
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.GroupEntity
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.ExitScreen
import com.mike.uniadmin.ui.theme.Background
import com.mike.uniadmin.ui.theme.GlobalColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun DiscussionScreen(
    navController: NavController, context: Context, targetGroupID: String
) {
    val uniAdmin = context.applicationContext as? UniAdmin
    val chatRepository = remember { uniAdmin?.chatRepository }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.ChatViewModelFactory(chatRepository ?: throw IllegalStateException("ChatRepository is null"))
    )
    val userAdmin = context.applicationContext as? UniAdmin
    val userRepository = remember { userAdmin?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("UserRepository is null")
        )
    )


    val chats by chatViewModel.chats.observeAsState(listOf())
    val user by userViewModel.user.observeAsState(initial = null)
    val group by chatViewModel.group.observeAsState(initial = null)
    val users by userViewModel.users.observeAsState(emptyList())

    val startTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var timeSpent by remember { mutableLongStateOf(0L) }
    var messagetext by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showUsers by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val screenID = "SC6"
    val scrollState = rememberLazyListState()
    val groupPath = "Group Chat/$targetGroupID"


    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        while (true) {
            timeSpent = System.currentTimeMillis() - startTime
            delay(1000)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ExitScreen(
                context = context, screenID = screenID, timeSpent = timeSpent
            )
        }
    }

    LaunchedEffect(currentUser?.email) {
        currentUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }
    }

    LaunchedEffect(Unit) {
        if (chats.isNotEmpty()) {
            scrollState.animateScrollToItem(chats.size - 1)
        }
        userViewModel.checkAllUserStatuses()
        chatViewModel.fetchChats(groupPath)
        chatViewModel.fetchGroupById(targetGroupID)
    }


    Scaffold(topBar = {
        GroupDetails.groupName.value?.let {
            GroupDetails.groupImageLink.value?.let { it1 ->
                ChatTopAppBar(navController = navController,
                    name = it,
                    link = it1,
                    context = context,
                    onSearchClick = { isSearchVisible = !isSearchVisible },
                    onShowUsersClick = { showUsers = !showUsers })
            }
        }
    }, snackbarHost = { SnackbarHost(snackbarHostState) }, content = { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Background(context)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(8.dp)
                    .imePadding()
            ) {
                group?.let {
                    GroupUsersList(
                        isVisible = showUsers,
                        users = users,
                        navController = navController,
                        context = context,
                        viewModel = userViewModel,
                        group = it
                    )
                }
                SearchBar(isSearchVisible = isSearchVisible,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it })
                user?.let { currentUser ->
                    LazyColumn(
                        state = scrollState, modifier = Modifier.weight(1f)
                    ) {
                        val groupedChats = chats.groupBy { it.date }

                        groupedChats.forEach { (_, chatsForDate) ->
                            item {
                                RowText(context = context)
                                Spacer(modifier = Modifier.height(8.dp))
                                DateHeader(context = context)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(chatsForDate.filter {
                                it.message?.contains(
                                    searchQuery.text,
                                    ignoreCase = true
                                ) ?: false
                            }) { chat ->
                                ChatBubble(
                                    chat = chat,
                                    isUser = chat.senderID == currentUser.id,
                                    context = context,
                                    navController = navController
                                )
                            }
                        }
                    }
                    MessageInputRow(message = messagetext,
                        onMessageChange = { messagetext = it },
                        onSendClick = {
                            if (messagetext.isNotBlank() && currentUser.firstName?.isNotBlank() == true) {
                                MyDatabase.generateChatID { chatID ->
                                    val chat = ChatEntity(
                                        message = messagetext,
                                        senderName = currentUser.firstName,
                                        senderID = currentUser.id,
                                        time = getCurrentTimeInAmPm(),
                                        date = getCurrentDate(),
                                        id = chatID,
                                        profileImageLink = currentUser.profileImageLink
                                    )
                                    sendMessage(
                                        chat = chat, viewModel = chatViewModel, path = groupPath
                                    )
                                    messagetext = ""
                                }
                            }
                        },
                        context = context
                    )
                } ?: run {
                    Text("Loading...", style = CC.descriptionTextStyle(context))
                }
            }
        }
    })
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopAppBar(
    navController: NavController,
    name: String,
    link: String,
    context: Context,
    onSearchClick: () -> Unit,
    onShowUsersClick: () -> Unit
) {
    TopAppBar(title = {
        Row(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
        ) {
            // Group Icon on the left
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .size(50.dp), contentAlignment = Alignment.Center
            ) {
                if (link.isNotBlank()) {
                    AsyncImage(
                        model = link,
                        contentDescription = "Group Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.logo),
                        error = painterResource(id = R.drawable.logo)
                    )
                } else {
                    Text(
                        text = name[0].toString(),
                        style = CC.titleTextStyle(context).copy(fontSize = 18.sp)
                    )
                }

            }

            Spacer(modifier = Modifier.width(8.dp)) // Space between icon and text

            // Group Name and User Info in the center
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = name,
                    style = CC.titleTextStyle(context).copy(fontSize = 18.sp),
                    maxLines = 1
                )
            }
        }
    },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Filled.Search, contentDescription = "Search", tint = CC.textColor()
                )
            }
            IconButton(onClick = { onShowUsersClick() }) {
                Icon(Icons.Filled.Person, "Participants", tint = CC.textColor())
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("homescreen") }) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = CC.textColor()
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = GlobalColors.primaryColor),
        modifier = Modifier.statusBarsPadding() // Adjust for status bar
    )
}

@Composable
fun GroupUsersList(
    isVisible: Boolean,
    users: List<UserEntity>,
    navController: NavController,
    context: Context,
    viewModel: UserViewModel,
    group: GroupEntity
) {
    val filteredUsers = users.filter { user ->
        group.members?.contains(user.id) ?: false  // Filter users based on membership
    }

    AnimatedVisibility(visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { -it })
    ) {
        LazyRow(
            modifier = Modifier
                .background(CC.secondary())
                .fillMaxWidth(),
        ) {
            items(filteredUsers) { user ->  // Use the filtered list
                UserItem(user, context, navController, viewModel)
            }
        }
    }
}

@Composable
fun SearchBar(
    isSearchVisible: Boolean,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit
) {
    AnimatedVisibility(visible = isSearchVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { -it })
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search Chats") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GlobalColors.primaryColor,
                unfocusedIndicatorColor = CC.textColor(),
                focusedIndicatorColor = GlobalColors.secondaryColor,
                unfocusedContainerColor = GlobalColors.primaryColor,
                focusedTextColor = CC.textColor(),
                unfocusedTextColor = GlobalColors.textColor,
                focusedLabelColor = GlobalColors.secondaryColor,
                unfocusedLabelColor = GlobalColors.textColor
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
fun DateHeader(context: Context) {
    val currentDateString = getFormattedDate() // Get the formatted date string

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(GlobalColors.secondaryColor, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = currentDateString,
                style = CC.descriptionTextStyle(context),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun getFormattedDate(): String {
    val calendar = Calendar.getInstance()
    val today = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, -1) // Go back one day
    val yesterday = calendar.time

    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    return when (dateFormat.format(today)) {
        dateFormat.format(Date()) -> "Today"
        dateFormat.format(yesterday) -> "Yesterday"
        else -> dateFormat.format(Date())
    }
}


@Composable
fun MessageInputRow(
    message: String, onMessageChange: (String) -> Unit, onSendClick: () -> Unit, context: Context
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            textStyle = CC.descriptionTextStyle(context),
            onValueChange = onMessageChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .heightIn(min = 0.dp, max = 100.dp),
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
            singleLine = false
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onSendClick,
            colors = ButtonDefaults.buttonColors(containerColor = GlobalColors.extraColor2),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, "Send")
        }
    }
}

fun sendMessage(
    chat: ChatEntity, viewModel: ChatViewModel, path: String
) {
    viewModel.saveChat(chat, path) {}
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChatBubble(
    chat: ChatEntity, isUser: Boolean, context: Context, navController: NavController
) {
    val alignment = if (isUser) Alignment.TopEnd else Alignment.TopStart
    val backgroundColor = if (isUser) GlobalColors.extraColor1 else GlobalColors.extraColor2
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
                if (!isUser) {
                    Row(
                        horizontalArrangement = Arrangement.Start
                    ) {
                        chat.senderName?.let {
                            Text(
                                text = it,
                                style = CC.descriptionTextStyle(context),
                                fontWeight = FontWeight.Bold,
                                color = GlobalColors.primaryColor
                            )
                        }
                    }
                }
                SelectionContainer {
                    ClickableText(text = buildAnnotatedString {
                        val linkStyle = SpanStyle(
                            color = Color.Blue, textDecoration = TextDecoration.Underline
                        )
                        append(chat.message)
                        // Use a regex or any other method to detect links and apply linkStyle
                        val regex = Regex("(https?://[\\w./?=#]+)")
                        chat.message?.let {
                            regex.findAll(it).forEach { result ->
                                val start = result.range.first
                                val end = result.range.last + 1
                                addStyle(linkStyle, start, end)
                                addStringAnnotation(
                                    tag = "URL", annotation = result.value, start = start, end = end
                                )
                            }
                        }
                    }, onClick = { offset ->
                        val annotations = chat.message?.substring(offset)
                        annotations.let {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotations))
                            context.startActivity(intent)
                        }
                    }, style = CC.descriptionTextStyle(context)
                    )
                }
                chat.time?.let {
                    Text(
                        text = it,
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
        if (!isUser) {
            // Icon with first letter of sender's name, positioned outside the bubble
            Box(modifier = Modifier
                .clickable {
                    navController.navigate("chat/${chat.senderID}")
                }
                .offset(x = (-16).dp, y = (-16).dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(GlobalColors.primaryColor, CircleShape)
                .padding(4.dp),
                contentAlignment = Alignment.Center) {
                if (chat.profileImageLink?.isNotBlank() == true) {
                    AsyncImage(
                        model = chat.profileImageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.logo),
                        error = painterResource(id = R.drawable.logo)
                    )
                } else {
                    chat.senderName?.get(0)?.let {
                        Text(
                            text = it.toString(),
                            style = CC.titleTextStyle(context).copy(fontSize = 18.sp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RowText(context: Context) {
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
                text = "Group chats are moderated by admin.",
                modifier = Modifier.padding(5.dp),
                style = CC.descriptionTextStyle(context),
                textAlign = TextAlign.Center,
                color = CC.textColor()
            )
        }
    }
}


fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    val currentDate = Date()
    return dateFormat.format(currentDate)
}

fun getCurrentTimeInAmPm(): String {
    val currentTime = Date()
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(currentTime)
}


@Preview
@Composable
fun PreviewMyScreen() {

}
