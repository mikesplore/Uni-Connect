package com.mike.uniadmin.chat

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.model.Message
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.fetchUserDataByEmail
import com.mike.uniadmin.model.MyDatabase.fetchUserToUserMessages
import com.mike.uniadmin.model.MyDatabase.getUsers
import com.mike.uniadmin.model.User
import com.mike.uniadmin.ui.theme.GlobalColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mike.uniadmin.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsScreen(navController: NavController, context: Context) {
    val auth = FirebaseAuth.getInstance()
    var users by remember { mutableStateOf<List<User>?>(null) }
    var currentMe by remember { mutableStateOf(User()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var currentPerson by remember { mutableStateOf("") }
    var searchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(key1 = Unit) {
        GlobalColors.loadColorScheme(context)
        auth.currentUser?.email?.let { email ->
            fetchUserDataByEmail(email) { fetchedUser ->
                fetchedUser?.let {
                    currentMe = it
                    currentPerson = it.firstName
                }
            }
        }
    }

    LaunchedEffect(loading) {
        getUsers { fetchedUsers ->
            if (fetchedUsers == null) {
                errorMessage = "Failed to fetch users. Please try again later."
            } else {
                users = fetchedUsers
            }
            loading = false
        }
    }

    val filteredUsers = users?.filter { user ->
        user.firstName.contains(searchQuery, ignoreCase = true) ||
                user.lastName.contains(searchQuery, ignoreCase = true)
    } ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Uni Chat", style = CC.titleTextStyle(context)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = CC.textColor()
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { loading = true }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CC.textColor())
                    }
                    IconButton(onClick = { searchVisible = !searchVisible }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = CC.textColor())
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
                        TextField(value = searchQuery,
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
                        loading -> {
                            CircularProgressIndicator(color = CC.textColor())
                        }
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
                                color = CC.textColor(),
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
                                    ProfileCard(user, navController, context)
                                }
                            }
                        }
                    }
                }
            }
        },
        containerColor = CC.primary()
    )
}


@Composable
fun ProfileCard(user: User, navController: NavController, context: Context) {
    val auth = FirebaseAuth.getInstance()
    var currentMe by remember { mutableStateOf(User()) }
    var currentPerson by remember { mutableStateOf("") }
    var admissionNumber by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(emptyList<Message>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        auth.currentUser?.email?.let { email ->
            fetchUserDataByEmail(email) { fetchedUser ->
                fetchedUser?.let {
                    currentMe = it
                    currentPerson = it.firstName
                    admissionNumber = it.id
                }
            }
        }
    }

    val conversationId = "Direct Messages/${generateConversationId(admissionNumber, user.id)}"

    fun fetchMessages(conversationId: String) {
        try {
            fetchUserToUserMessages(conversationId) { fetchedMessages ->
                messages = fetchedMessages
            }
        } catch (e: Exception) {
            errorMessage = e.message
            scope.launch {
                snackbarHostState.showSnackbar("Failed to fetch messages: ${e.message}")
                Log.e("UserChatScreen", "Failed to fetch messages from $conversationId: ${e.message}", e)
            }
        }
    }

    LaunchedEffect(conversationId) {
        while (true) {
            fetchMessages(conversationId)
            delay(1000) // Adjust the delay as needed
        }
    }

    val latestMessage = messages.lastOrNull()

    Card(
        modifier = Modifier
            .clickable { navController.navigate("chat/${user.id}") }
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CC.primary())
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (user.profileImageLink.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(user.profileImageLink),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
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
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayName = if (user.id == currentMe.id) "You" else user.firstName + " "+ user.lastName
                    Text(
                        text = displayName,
                        style = CC.titleTextStyle(navController.context).copy(fontSize = 18.sp),
                        color = CC.textColor()
                    )

                    Text(
                        text = latestMessage?.time?: "",
                        style = CC.descriptionTextStyle(navController.context),
                        color = CC.textColor()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = latestMessage?.message ?: "No messages yet",
                        style = CC.descriptionTextStyle(navController.context),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = CC.textColor().copy(alpha = 0.5f),
                        modifier = Modifier.weight(1f)
                    )
                    messages.let {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GlobalColors.extraColor1, CircleShape),
                            contentAlignment = Alignment.Center
                        ){
                            Text(
                                text = "${it.size}",
                                style = CC.descriptionTextStyle(context),
                                color = CC.textColor(),
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun UsersPreview() {
    //ParticipantsScreen(rememberNavController(), LocalContext.current)
    ProfileCard(
        user = User(firstName = "Mike", email = "mike@2020"),
        navController = rememberNavController(),
        context = LocalContext.current
    )
}
