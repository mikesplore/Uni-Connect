package com.mike.uniadmin.home

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.PagerState
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.DeviceTheme
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.attendance.ManageAttendanceScreen
import com.mike.uniadmin.timetable.TimetableScreen
import com.mike.uniadmin.announcements.AnnouncementsScreen
import com.mike.uniadmin.assignments.AssignmentScreen
import com.mike.uniadmin.chat.GroupItem
import com.mike.uniadmin.chat.TargetUser
import com.mike.uniadmin.clearAllPreferences
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.GroupEntity
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.SignedInUser
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserStateEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.getUpdate
import com.mike.uniadmin.model.Screen
import com.mike.uniadmin.model.Update
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalSnapperApi::class,
    ExperimentalMaterial3Api::class,
)
@Composable
fun HomeScreen(
    navController: NavController,
    context: Context,
    pagerState: PagerState,
    activity: MainActivity,
    screens: List<Screen>,
    coroutineScope: CoroutineScope,
) {

    // ViewModel instantiation
    val uniAdmin = context.applicationContext as? UniAdmin

    val chatRepository =
        uniAdmin?.chatRepository ?: throw IllegalStateException("ChatRepository not initialized")
    val chatViewModel: ChatViewModel =
        viewModel(factory = ChatViewModel.ChatViewModelFactory(chatRepository))

    val userRepository = remember { uniAdmin.userRepository }
    val userViewModel: UserViewModel =
        viewModel(factory = UserViewModelFactory(userRepository))

    // State observation
    val signedInUser by userViewModel.signedInUser.observeAsState()
    val fetchedUserDetails by userViewModel.user.observeAsState()
    val userStatus by userViewModel.userState.observeAsState()
    val users by userViewModel.users.observeAsState(initial = emptyList())
    val groups by chatViewModel.groups.observeAsState(emptyList())

    // Local state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf(Update()) }

    val signedInUserLoading by userViewModel.isLoading.observeAsState()

    // Derived state
    val userGroups = groups.filter { it.members.contains(fetchedUserDetails?.id)}

    // Side effects
    LaunchedEffect(signedInUser, fetchedUserDetails) {
        // 1. Get signed-in user and fetch details (if needed)
        userViewModel.getSignedInUser()
        signedInUser?.email?.let { email ->
            userViewModel.findUserByEmail(email,) { user -> Log.d("Fetched User details", "$user") }
        }

        // 2. Fetch app version (can run concurrently)
        launch {
            getUpdate { fetched ->
                if (fetched != null) {
                    update = fetched
                }
            }
        }

        // 3. Perform other data fetching and updates
        userViewModel.checkAllUserStatuses()
        chatViewModel.fetchGroups()
        userViewModel.fetchUsers()
        fetchedUserDetails?.id?.let { userId ->
            userViewModel.checkUserStateByID(userId)
        }
    }

    // Main content starts here
    if (showBottomSheet) {
        ModalBottomSheet(tonalElevation = 5.dp, onDismissRequest = {
            scope.launch {
                sheetState.hide()
                showBottomSheet = false
            }
        }, containerColor = CC.primary(), sheetState = sheetState, content = {
                ModalDrawerItem(
                    context = context,
                    navController = navController,
                    userViewModel = userViewModel,
                    chatViewModel = chatViewModel,
                    activity = activity
                )
        })
    }
    CheckUpdate(context)
    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
            ModalNavigationDrawerItem(
                drawerState = drawerState,
                scope = coroutineScope,
                context = context,
                navController = navController,
                userViewModel = userViewModel,
                chatViewModel = chatViewModel,
                signedInUserLoading = signedInUserLoading,
                signedInUser = signedInUser,
                fetchedUserDetails = fetchedUserDetails,
                showBottomSheet = {value -> showBottomSheet = value },
                userStatus = userStatus,
                update = update

            )


    }) {
        Scaffold(
            bottomBar = {
                BottomBar(
                    context,
                    screens,
                    pagerState,
                    coroutineScope,
                    drawerState,
                    userViewModel,
                    chatViewModel,
                    scope,
                    showBottomSheet = { showBottomSheet = true }
                )
            },
            containerColor = CC.primary()
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    userScrollEnabled = false,
                    state = pagerState,
                    count = screens.size,
                    modifier = Modifier.padding(innerPadding),
                    flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
                ) { page ->
                    when (screens[page]) {
                        Screen.Home -> Dashboard(navController, context)
                        Screen.Assignments -> AssignmentScreen(context)
                        Screen.Announcements -> AnnouncementsScreen(context)
                        Screen.Timetable -> TimetableScreen(context)
                        Screen.Attendance -> ManageAttendanceScreen(context)
                    }
                }
            }
        }
    }
}

@Composable
fun ModalNavigationDrawerItem(
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    signedInUserLoading: Boolean?,
    signedInUser: SignedInUser?,
    fetchedUserDetails: UserEntity?,
    showBottomSheet:(Boolean) -> Unit,
    userStatus: UserStateEntity?,
    update: Update

){
    Column(
        modifier = Modifier
            .background(
                CC.extraColor1(), RoundedCornerShape(0.dp, 0.dp, 10.dp, 10.dp)
            )
            .fillMaxHeight(0.8f)
            .fillMaxWidth(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (signedInUserLoading == true) {
                CircularProgressIndicator(color = CC.textColor())
            } else if (signedInUser != null) {
                fetchedUserDetails?.let { SideProfile(it, context) }
            } else{
                Icon(Icons.Default.AccountCircle, "", tint = CC.textColor())
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            SideBarItem(
                icon = Icons.Default.AccountCircle,
                text = "Profile",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate("profile")
                })
            SideBarItem(icon = Icons.AutoMirrored.Filled.Message,
                text = "Uni Chat",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    userViewModel.fetchUsers()
                    chatViewModel.fetchGroups()
                    navController.navigate("uniChat")
                })
            SideBarItem(icon = Icons.Default.Groups,
                text = "Groups",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    userViewModel.fetchUsers()
                    chatViewModel.fetchGroups()
                    navController.navigate("groups")
                })
            SideBarItem(icon = Icons.Default.Notifications,
                text = "Notifications",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    userViewModel.fetchUsers()
                    chatViewModel.fetchGroups()
                    navController.navigate("notifications")
                })
            SideBarItem(icon = Icons.Default.Settings, text = "Settings", context, onClicked = {
                scope.launch {
                    drawerState.close()
                }
                navController.navigate("settings")
            })
            SideBarItem(icon = Icons.Default.Share, text = "Share App", context, onClicked = {
                scope.launch {
                    drawerState.close()
                }
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${fetchedUserDetails?.firstName} invites you to join Uni Konnect! Get organized and ace your studies.\n Download now: ${update.updateLink}"
                    ) // Customize the text
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
                scope.launch { drawerState.close() }
            })
            SideBarItem(
                icon = Icons.Default.ArrowDownward,
                text = "More",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    userViewModel.fetchUsers()
                    chatViewModel.fetchGroups()
                    showBottomSheet(true)
                })
        }
        Row(
            modifier = Modifier
                .background(CC.extraColor1())
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = {
                scope.launch {
                    drawerState.close()
                }
                userStatus.let {
                    MyDatabase.writeUserActivity(it!!.copy(
                        online = "offline", lastDate = CC.getTimeStamp(), lastTime = CC.getTimeStamp()
                    ), onSuccess = {
                        userViewModel.deleteAllTables()
                        userViewModel.deleteSignedInUser()
                        clearAllPreferences(context)

                        // Sign out AFTER clearing data
                        FirebaseAuth.getInstance().signOut()

                        navController.navigate("login") {
                            popUpTo("homeScreen") { inclusive = true }
                        }
                        Toast.makeText(
                            context, "Signed Out Successfully!", Toast.LENGTH_SHORT
                        ).show()
                    })
                }

            }) {
                Text(
                    "Sign Out",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
@Composable
fun BottomBar(
    context: Context,
    screens: List<Screen>,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    scope: CoroutineScope,
    showBottomSheet:(Boolean) -> Unit
){
        NavigationBar(
            modifier = Modifier
                .height(85.dp)
                .background(Color.Transparent),
            containerColor = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        CC
                            .primary()
                            .copy()
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEachIndexed { index, screen ->
                    val isSelected = pagerState.currentPage == index

                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) CC.textColor() else CC.textColor()
                            .copy(0.7f), label = "", animationSpec = tween(500)
                    )

                    // Use NavigationBarItem
                    NavigationBarItem(selected = isSelected, label = {
                        AnimatedVisibility(visible = isSelected,
                            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                                animationSpec = tween(500)
                            ) { initialState -> initialState },
                            exit = fadeOut(animationSpec = tween(500)) + slideOutVertically(
                                animationSpec = tween(500)
                            ) { initialState -> initialState }) {
                            Text(
                                text = screen.name,
                                style = CC.descriptionTextStyle(context)
                                    .copy(fontSize = 13.sp),
                                color = CC.textColor()
                            )
                        }
                    }, colors = NavigationBarItemDefaults.colors(
                        indicatorColor = CC.extraColor2(),
                        unselectedIconColor = CC.textColor(),
                        selectedIconColor = CC.textColor()
                    ), onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }, icon = {
                        Column(modifier = Modifier.combinedClickable(onLongClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }, onDoubleClick = {
                            userViewModel.fetchUsers()
                            chatViewModel.fetchGroups()
                            showBottomSheet(true)
                        }) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.name,
                                tint = iconColor,
                                modifier = Modifier.size(25.dp)
                            )
                        }
                    })
                }
            }
        }

}


@Composable
fun SideProfile(user: UserEntity, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                CC
                    .extraColor2()
                    .copy(0.5f)
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(15.dp))
        Box(
            modifier = Modifier
                .border(
                    1.dp, CC.textColor(), CircleShape
                )
                .clip(CircleShape)
                .background(CC.secondary(), CircleShape)
                .size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            if (user.profileImageLink.isNotEmpty()) {
                AsyncImage(
                    model = user.profileImageLink,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    "${user.firstName[0]}${user.lastName[0]}",
                    style = CC.titleTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            user.firstName + " " + user.lastName,
            style = CC.titleTextStyle(context)
                .copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 18.sp),
            maxLines = 2
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(user.id, style = CC.descriptionTextStyle(context))
    }
}


@Composable
fun ModalDrawerItem(
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    activity: MainActivity
) {
    val signedInUser by userViewModel.user.observeAsState()
    val currentUser by userViewModel.signedInUser.observeAsState()
    val users by userViewModel.users.observeAsState(emptyList())
    val groups by chatViewModel.groups.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        userViewModel.getSignedInUser()
        currentUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }
        userViewModel.fetchUsers()
        chatViewModel.fetchGroups()
    }
    Column(
        modifier = Modifier
            .padding(10.dp)
            .background(CC.primary())
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .border(
                        1.dp, CC.textColor(), CircleShape
                    )
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .size(70.dp),
                contentAlignment = Alignment.Center
            ) {
                if (signedInUser?.profileImageLink?.isNotEmpty() == true) {
                    AsyncImage(
                        model = signedInUser?.profileImageLink,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        "${signedInUser?.firstName?.get(0)}${signedInUser?.lastName?.get(0)}",
                        style = CC.titleTextStyle(context)
                            .copy(fontWeight = FontWeight.Bold, fontSize = 27.sp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    signedInUser?.firstName + " " + signedInUser?.lastName,
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    maxLines = 2
                )
                signedInUser?.email?.let { Text(it, style = CC.descriptionTextStyle(context)) }
                signedInUser?.id?.let { Text(it, style = CC.descriptionTextStyle(context)) }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Chat", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Select a user to open chat",
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(
            modifier = Modifier.animateContentSize()
        ) {
            items(users, key = { it.id }) { user ->
                UserItem(user, context, navController, userViewModel)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Class Discussions",
            style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Select a group to open",
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (groups.isEmpty()) {
            Text(
                text = "No groups available",
                style = CC.descriptionTextStyle(context).copy(fontSize = 18.sp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.animateContentSize()
            ) {
                items(groups, key = { it.id }) { group ->
                    if (group.name.isNotEmpty() && group.description.isNotEmpty()) {
                        signedInUser?.let {
                            GroupItem(
                                group,
                                context,
                                navController,
                                chatViewModel,
                                userViewModel,
                                it
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Quick Settings", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))
        QuickSettings(context, activity)

    }
}

@Composable
fun QuickSettings(context: Context, activity: MainActivity) {
    var isBiometricsEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(CC.primary())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CC.secondary())
                    .size(50.dp)
            ) {
                Icon(
                    if (DeviceTheme.darkMode.value) Icons.Default.Nightlight else Icons.Default.LightMode,
                    "theme",
                    tint = CC.textColor()
                )
            }

            Text("App theme", style = CC.descriptionTextStyle(context))
            Switch(
                onCheckedChange = {
                    DeviceTheme.darkMode.value = it
                    DeviceTheme.saveDarkModePreference(it)
                }, checked = DeviceTheme.darkMode.value, colors = SwitchDefaults.colors(
                    checkedThumbColor = CC.extraColor1(),
                    uncheckedThumbColor = CC.extraColor2(),
                    checkedTrackColor = CC.extraColor2(),
                    uncheckedTrackColor = CC.extraColor1(),
                    checkedIconColor = CC.textColor(),
                    uncheckedIconColor = CC.textColor()
                )
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CC.secondary())
                    .size(50.dp)
            ) {
                Icon(
                    Icons.Default.Fingerprint, "theme", tint = CC.textColor()
                )
            }

            Text("Biometrics", style = CC.descriptionTextStyle(context))
            Switch(
                onCheckedChange = { checked -> // Add checked parameter
                    if (checked) {
                        activity.promptManager.showBiometricPrompt(title = "User Authentication",
                            description = "Please Authenticate",
                            onResult = { success ->
                                isBiometricsEnabled = success // Update state based on success
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Authenticated Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Authentication Failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    } else {
                        isBiometricsEnabled = false // Update state if switch is turned off manually
                    }
                }, checked = isBiometricsEnabled, colors = SwitchDefaults.colors(
                    checkedThumbColor = CC.extraColor1(),
                    uncheckedThumbColor = CC.extraColor2(),
                    checkedTrackColor = CC.extraColor2(),
                    uncheckedTrackColor = CC.extraColor1(),
                    checkedIconColor = CC.textColor(),
                    uncheckedIconColor = CC.textColor()
                )
            )
        }
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserItem(
    user: UserEntity,
    context: Context,
    navController: NavController,
    viewModel: UserViewModel
) {
    var visible by remember { mutableStateOf(false) }
    val userStates by viewModel.userStates.observeAsState(emptyMap())
    val userState = userStates[user.id]
    val signedInUser by viewModel.signedInUser.observeAsState()

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = if (visible) 16.dp else 0.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(end = 10.dp)
        ) {
            val size = 50.dp
            Box(modifier = Modifier
                .border(
                    1.dp, CC.textColor(), CircleShape
                )
                .background(CC.tertiary(), CircleShape)
                .clip(CircleShape)
                .combinedClickable(onClick = {
                    TargetUser.targetUserId.value = user.id
                    navController.navigate("uniChat")
                }, onLongClick = {
                    visible = !visible
                })
                .size(size), contentAlignment = Alignment.Center
            ) {
                if (user.profileImageLink.isNotEmpty()) {
                    AsyncImage(
                        model = user.profileImageLink,
                        contentDescription = user.firstName,
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val name =
                        if (signedInUser?.email == user.email) "You" else "${user.firstName[0]}${
                            user.lastName[0]
                        }"
                    Text(
                        name, style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            val onlineStatus by animateColorAsState(
                animationSpec = tween(500, easing = LinearEasing),
                targetValue = if (userState?.online == "online") Color.Green else if (userState?.online == "offline") Color.DarkGray else Color.Red,
                label = ""
            )
            Box(
                modifier = Modifier
                    .border(
                        1.dp, CC.extraColor1(), CircleShape
                    )
                    .size(12.dp)
                    .background(
                        onlineStatus, CircleShape
                    )
                    .align(Alignment.BottomEnd)
                    .offset(x = (-6).dp, y = (-6).dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = user.firstName.let {
                if (it.length > 10) it.substring(0, 10) + "..." else it
            },
            style = CC.descriptionTextStyle(context),
            maxLines = 1
        )
        val date = if (userState?.lastDate?.let { CC.getCurrentDate(it) } == CC.getCurrentDate(CC.getTimeStamp())) {
            "Today at ${userState.lastTime.let { CC.getCurrentTime(it) }}"
        } else {
            "${userState?.lastDate?.let { CC.getCurrentDate(it) } ?: ""} at ${userState?.lastTime?.let { CC.getCurrentTime(it) } ?: ""}"
        }

        val state = when (userState?.online) {
            "online" -> "Online"
            "offline" -> date
            else -> "Never Online"
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedVisibility(visible = visible) {
            UserInfo(user = user, state, context)
        }
    }
}

@Composable
fun UserInfo(user: UserEntity, userState: String, context: Context) {
    Column(
        modifier = Modifier.fillMaxWidth(0.9f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            user.firstName + " " + user.lastName,
            style = CC.titleTextStyle(context).copy(fontSize = 15.sp)
        )
        Text(user.id, style = CC.descriptionTextStyle(context).copy(fontSize = 15.sp))
        Text(userState, style = CC.descriptionTextStyle(context).copy(fontSize = 15.sp))

    }
}


@Composable
fun SideBarItem(icon: ImageVector, text: String, context: Context, onClicked: () -> Unit) {
    Spacer(modifier = Modifier.height(10.dp))
    TextButton(onClick = onClicked) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                icon, "", tint = CC.textColor()
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text, style = CC.descriptionTextStyle(context))

        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckUpdate(context: Context) {
    var update by remember { mutableStateOf(false) }
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    var isDownloading by remember { mutableStateOf(false) }
    var downloadId by remember { mutableLongStateOf(-1L) }
    var myUpdate by remember { mutableStateOf(Update()) }

    LaunchedEffect(Unit) {
        while (true) {
            getUpdate { localUpdate ->
                if (localUpdate != null) {
                    myUpdate = localUpdate

                    if (myUpdate.version != versionName) {
                        update = true
                    }
                }
            }
            delay(60000) // Wait for 60 seconds
        }
    }
    fun installApk(context: Context, uri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(installIntent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun startDownload(context: Context, url: String, onProgress: (Int, Long) -> Unit) {
        val request = DownloadManager.Request(Uri.parse(url)).setTitle("UniKonnect Update")
            .setDescription("Downloading update")
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "UniKonnect.apk")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true).setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadid = downloadManager.enqueue(request)

        // Registering receiver for download complete
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadid) {
                    context.unregisterReceiver(this)
                    val apkUri = downloadManager.getUriForDownloadedFile(id)
                    installApk(context, apkUri)
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        }

        // Track progress
        val progressHandler = Handler(Looper.getMainLooper())
        progressHandler.post(object : Runnable {
            override fun run() {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor: Cursor? = downloadManager.query(query)

                cursor?.use {
                    if (it.moveToFirst()) {
                        val bytesDownloadedIndex =
                            it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val bytesTotalIndex =
                            it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

                        if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                            val bytesDownloaded = it.getLong(bytesDownloadedIndex)
                            val bytesTotal = it.getLong(bytesTotalIndex)

                            // Log for debugging
                            Log.d(
                                "DownloadManager",
                                "Downloaded: $bytesDownloaded, Total: $bytesTotal"
                            )

                            if (bytesTotal > 0) {
                                val progress = ((bytesDownloaded * 100) / bytesTotal).toInt()
                                onProgress(progress, downloadId)

                                // Update progress in UI
                                Log.d("DownloadProgress", "Progress: $progress%")

                                if (progress < 100) {
                                    progressHandler.postDelayed(this, 1000)
                                }
                            }
                        } else {
                            Log.e("DownloadManager", "Column index not found")
                        }
                    }
                }
                cursor?.close()
            }
        })
    }




    if (update) {
        BasicAlertDialog(
            onDismissRequest = {
                isDownloading = false
                update = false
            }, modifier = Modifier.background(
                Color.Transparent, RoundedCornerShape(10.dp)
            )
        ) {
            Column(
                modifier = Modifier
                    .background(CC.secondary(), RoundedCornerShape(10.dp))
                    .padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "New Update available!", style = CC.titleTextStyle(context).copy(
                        fontSize = 18.sp, fontWeight = FontWeight.Bold
                    ), modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    "A new version of this app is available. The update contains bug fixes and improvements.",
                    style = CC.descriptionTextStyle(context),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (isDownloading) {
                    Text(
                        "Downloading update...please wait",
                        style = CC.descriptionTextStyle(context),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LinearProgressIndicator(
                        color = CC.textColor(), trackColor = CC.extraColor1()
                    )
                }
                val downloadUrl = myUpdate.updateLink
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (!isDownloading && downloadUrl.isNotEmpty()) {
                                startDownload(context, url = downloadUrl) { progress, id ->
                                    downloadId = id
                                    isDownloading = progress < 100
                                }
                                isDownloading = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Download failed: Could not get download link",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CC.primary())
                    ) {
                        Text("Update", style = CC.descriptionTextStyle(context))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { update = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Cancel", color = CC.primary())
                    }
                }
            }
        }
    }

}
