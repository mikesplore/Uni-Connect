package com.mike.uniadmin.chat

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.dataModel.groupchat.generateConversationId
import com.mike.uniadmin.dataModel.userchat.Message
import com.mike.uniadmin.dataModel.users.User
import com.mike.uniadmin.dataModel.users.UserRepository
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase.fetchUserToUserMessages
import com.mike.uniadmin.ui.theme.GlobalColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mike.uniadmin.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsScreen(navController: NavController, context: Context) {
    val userRepository = remember { UserRepository() }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
    val auth = FirebaseAuth.getInstance()
    val users by userViewModel.users.observeAsState(initial = emptyList())
    val errorMessage by remember { mutableStateOf<String?>(null) }
    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    LaunchedEffect(key1 = Unit) {
        GlobalColors.loadColorScheme(context)
        auth.currentUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }
    }

    // Filter and sort users
    val filteredUsers = users
        .filter { user ->
            user.firstName.contains(searchQuery, ignoreCase = true) ||
                    user.lastName.contains(searchQuery, ignoreCase = true)
        }
        .sortedByDescending { it.id }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uni Chat", style = CC.titleTextStyle(context)) },
                actions = {
                    IconButton(onClick = { searchVisible = !searchVisible }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = CC.textColor()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary())
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    AnimatedVisibility(visible = searchVisible) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Participants") },
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
                    when {
                        errorMessage != null -> {
                            Text(
                                text = errorMessage!!,
                                color = Color.Red,
                                style = CC.descriptionTextStyle(context)
                            )
                        }
                        filteredUsers.isEmpty() -> {
                            Text(
                                text = "No participants found.",
                                style = CC.descriptionTextStyle(context)
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxSize()
                            ) {
                                items(filteredUsers) { user ->
                                    ProfileCard(user, navController, context, userViewModel)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = CC.primary()
    )

    ModalNavigationDrawer(
        modifier = Modifier.fillMaxWidth(0.5f),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Display list of users to start a conversation with
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CC.secondary())
                        .padding(16.dp)
                ) {
                    items(users) { user ->
                        UserListItem(user, navController)
                    }
                }
            }
        },
        content = {

        }
    )
}

@Composable
fun UserListItem(user: User, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("chat/${user.id}") }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp)){
        Image(
            painter = if (user.profileImageLink.isNotBlank()) rememberAsyncImagePainter(user.profileImageLink) else painterResource(id = R.drawable.student),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )}
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = "${user.firstName} ${user.lastName}", style = CC.titleTextStyle(navController.context).copy(fontSize = 16.sp))
        }
    }
}

@Composable
fun ProfileCard(
    user: User,
    navController: NavController,
    context: Context,
    viewModel: UserViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val currentMe by viewModel.user.observeAsState()
    val admissionNumber by remember { mutableStateOf(currentMe?.id) }
    var messages by remember { mutableStateOf(emptyList<Message>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val userStates by viewModel.userStates.observeAsState(emptyMap())

    val userState = userStates[user.id]

    LaunchedEffect(Unit) {
        auth.currentUser?.email?.let { email ->
            viewModel.findUserByEmail(email) {}
            viewModel.checkAllUserStatuses()
        }
    }

    val conversationId =
        "Direct Messages/${admissionNumber?.let { generateConversationId(it, user.id) }}"

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

    LaunchedEffect(key1 = conversationId) {
        while (true) {
            fetchMessages(conversationId)
            delay(1000) // Adjust the delay as needed
        }
    }

    val latestMessage = messages.lastOrNull()

    Card(modifier = Modifier
        .clickable { navController.navigate("chat/${user.id}") }
        .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CC.primary())) {
        Row(
            modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.size(50.dp)) {
                if (user.profileImageLink.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(user.profileImageLink),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.student), // Replace with your profile icon
                        contentDescription = "Profile Icon",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayName =
                        if (user.id == currentMe?.id) "${user.firstName}(You)" else user.firstName + " " + user.lastName
                    Text(
                        text = displayName,
                        style = CC.titleTextStyle(navController.context).copy(fontSize = 18.sp),
                        color = CC.textColor()
                    )
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        val textColor = if (userState == null) {
                            CC.textColor().copy(alpha = 0.5f)
                        } else if (userState.online == "online") {
                            Color.Green
                        } else {
                            Color.Red
                        }
                        Text(
                            text = when {
                                userState == null -> "Never online"
                                userState.online == "online" -> "Online"
                                else -> "Last seen ${userState.lastTime}"
                            },
                            style = CC.descriptionTextStyle(navController.context).copy(
                                fontSize = 12.sp
                            ),
                            color = textColor
                        )
                        Text(
                            text = latestMessage?.time ?: "",
                            style = CC.descriptionTextStyle(navController.context).copy(fontSize = 12.sp),
                        )}
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = latestMessage?.message ?: "",
                        style = CC.descriptionTextStyle(navController.context),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = CC.textColor().copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}







