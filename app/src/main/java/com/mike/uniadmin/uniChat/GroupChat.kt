package com.mike.uniadmin.uniChat

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.dataModel.groupchat.ChatEntity
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.GroupEntity
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.home.UserItem
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.Background
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun DiscussionScreen(
    navController: NavController, context: Context, targetGroupID: String
) {
    val uniAdmin = context.applicationContext as? UniAdmin
    val chatRepository = remember { uniAdmin?.chatRepository }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.ChatViewModelFactory(
            chatRepository ?: throw IllegalStateException("ChatRepository is null")
        )
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

    var messageText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showUsers by remember { mutableStateOf(false) }
    var isSearchVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scrollState = rememberLazyListState()
    val groupPath = "Group Chat/$targetGroupID"


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
        GroupDetails.groupName.value?.let { groupName ->
            GroupDetails.groupImageLink.value?.let { imageLink ->
                ChatTopAppBar(navController = navController,
                    name = groupName,
                    link = imageLink,
                    context = context,
                    onSearchClick = { isSearchVisible = !isSearchVisible },
                    onShowUsersClick = { showUsers = !showUsers })
            }
        }
    }, snackbarHost = { SnackbarHost(snackbarHostState) }, content = { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Background()
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
                        state = scrollState, modifier = Modifier
                            .animateContentSize()
                            .weight(1f)
                    ) {

                        val groupedChats = chats.groupBy { CC.getCurrentDate(it.date) }

                        groupedChats.forEach { (_, chatsForDate) ->
                            item {
                                RowText(context = context)
                                Spacer(modifier = Modifier.height(8.dp))
                                DateHeader(context = context)
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(chatsForDate.filter {
                                it.message.contains(
                                    searchQuery.text, ignoreCase = true
                                )
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
                    MessageInputRow(message = messageText,
                        onMessageChange = { messageText = it },
                        onSendClick = {
                            if (messageText.isNotBlank() && currentUser.firstName.isNotBlank()) {
                                MyDatabase.generateChatID { chatID ->
                                    val chat = ChatEntity(
                                        message = messageText,
                                        senderName = currentUser.firstName,
                                        senderID = currentUser.id,
                                        time = CC.getTimeStamp(),
                                        date = CC.getTimeStamp(),
                                        id = chatID,
                                        profileImageLink = currentUser.profileImageLink
                                    )
                                    sendMessage(
                                        chat = chat, viewModel = chatViewModel, path = groupPath
                                    )
                                    messageText = ""
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
            IconButton(onClick = {
                navController.navigate("uniChat") {
                    popUpTo("GroupChat/${GroupDetails.groupName.value}") {
                        inclusive = true
                    }
                }
            }) {
                Icon(
                    Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = CC.textColor()
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary()),
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
        group.members.contains(user.id)  // Filter users based on membership
    }

    AnimatedVisibility(visible = isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
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
                focusedContainerColor = CC.primary(),
                unfocusedIndicatorColor = CC.textColor(),
                focusedIndicatorColor = CC.secondary(),
                unfocusedContainerColor = CC.primary(),
                focusedTextColor = CC.textColor(),
                unfocusedTextColor = CC.textColor(),
                focusedLabelColor = CC.secondary(),
                unfocusedLabelColor = CC.textColor()
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

@Composable
fun DateHeader(context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(CC.secondary(), RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = CC.getRelativeDate(CC.getCurrentDate(CC.getTimeStamp())),
                style = CC.descriptionTextStyle(context),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
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
        BasicTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp) // Add padding to the right
                .background(CC.secondary(), RoundedCornerShape(24.dp)) // Use surface color
                .heightIn(min = 40.dp), // Minimum height
            textStyle = CC.descriptionTextStyle(context),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.padding(16.dp), // Increased padding
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (message.isEmpty()) {
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
        IconButton(
            onClick = {
                if (message.isNotBlank()) { // Check for non-blank messages
                    onMessageChange(message)
                    onSendClick()
                }
            }, modifier = Modifier
                .clip(CircleShape) // Circular button
                .background(CC.secondary()) // Button background color
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = CC.extraColor2() // Icon color on secondary background
            )
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        val maxBubbleWidth = maxWidth * 0.75f
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (!isUser) {

                Box(modifier = Modifier
                    .clickable {
                        navController.navigate("chat/${chat.senderID}") {
                            popUpTo("chat/${chat.senderID}") {
                                inclusive = true
                            }
                        }
                    }
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CC.primary(), CircleShape)
                    .padding(4.dp), contentAlignment = Alignment.Center) {
                    if (chat.profileImageLink.isNotBlank()) {
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
                        Text(
                            text = chat.senderName[0].toString(),
                            style = CC.titleTextStyle(context).copy(fontSize = 18.sp)
                        )
                    }
                }
            }
            if (isUser) {
                Text(
                    text = CC.getFormattedTime(chat.time),
                    style = CC.descriptionTextStyle(context),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .background(backgroundColor, bubbleShape)
                    .widthIn(max = maxBubbleWidth)
                    .padding(12.dp)
            ) {
                Column {
                    if (!isUser) {
                        Text(
                            text = chat.senderName,
                            style = CC.descriptionTextStyle(context),
                            fontWeight = FontWeight.Bold,
                            color = CC.primary()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    SelectionContainer {
                        Text(
                            text = chat.message,
                            style = CC.descriptionTextStyle(context).copy(fontSize = 12.sp)
                        )
                    }
                }
            }
            if (!isUser) {
                Text(
                    text = CC.getFormattedTime(chat.time),
                    style = CC.descriptionTextStyle(context),
                    fontSize = 10.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp)
                )
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



