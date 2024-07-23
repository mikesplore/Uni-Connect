package com.mike.uniadmin

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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.mike.uniadmin.chat.GroupItem
import com.mike.uniadmin.chat.getCurrentTimeInAmPm
import com.mike.uniadmin.dataModel.announcements.Announcement
import com.mike.uniadmin.dataModel.announcements.AnnouncementRepository
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModel
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModelFactory
import com.mike.uniadmin.dataModel.courses.CourseRepository
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.GroupEntity
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.User
import com.mike.uniadmin.dataModel.users.UserRepository
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.getUpdate
import com.mike.uniadmin.model.Screen
import com.mike.uniadmin.model.Update
import com.mike.uniadmin.ui.theme.GlobalColors
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mike.uniadmin.CommonComponents as CC


@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalSnapperApi::class,
    ExperimentalFoundationApi::class,
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
    val currentPerson = FirebaseAuth.getInstance().currentUser
    val courseRepository = remember { CourseRepository() }
    val userRepository = remember { UserRepository() }

    val uniAdmin = context.applicationContext as? UniAdmin
    val chatRepository = uniAdmin?.chatRepository ?: throw IllegalStateException("ChatRepository not initialized")
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.ChatViewModelFactory(chatRepository)
    )


    val announcementRepository = remember { AnnouncementRepository() }
    val announcementViewModel: AnnouncementViewModel =
        viewModel(factory = AnnouncementViewModelFactory(announcementRepository))
    val courseViewModel: CourseViewModel =
        viewModel(factory = CourseViewModelFactory(courseRepository))
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
    val user by userViewModel.user.observeAsState()
    val userStatus by userViewModel.userState.observeAsState()
    val users by userViewModel.users.observeAsState(emptyList())
    val groups by chatViewModel.groups.observeAsState(emptyList())
    val signedInUser = remember { mutableStateOf<User?>(null) }
    val announcement by remember { mutableStateOf(Announcement()) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf(Update()) }
    val userGroups = groups.filter { it.members.contains(signedInUser.value?.id) }

    LaunchedEffect(currentPerson?.email) {
        GlobalColors.loadColorScheme(context)
        currentPerson?.email?.let { email ->
            userViewModel.findUserByEmail(email) {}
        }
    }

    LaunchedEffect(user) {
        user?.let {
            signedInUser.value = it
            userViewModel.checkAllUserStatuses()
            chatViewModel.fetchGroups()
            userViewModel.checkUserStateByID(it.id)
        }
        getUpdate { fetched ->
            if (fetched != null) {
                update = fetched

            }
        }
    }

    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
    }


    //main content starts here
    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = {
            scope.launch {
                sheetState.hide()
                showBottomSheet = false
            }
        }, containerColor = CC.primary(), sheetState = sheetState, content = {
            signedInUser.value?.let {
                ModalDrawerItem(
                    signedInUser = it,
                    user = user!!,
                    context = context,
                    navController = navController,
                    announcement = announcement,
                    users = users,
                    userViewModel = userViewModel,
                    chatViewModel = chatViewModel,
                    userGroups = userGroups
                )
            }
        })
    }
    CheckUpdate(context)
    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
        Column(
            modifier = Modifier
                .background(CC.secondary())
                .fillMaxHeight(0.7f)
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
                signedInUser.value?.let { SideProfile(it, context) }

            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                SideBarItem(icon = Icons.Default.AccountCircle,
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
                        navController.navigate("users")
                    })
                SideBarItem(icon = Icons.Default.Groups, text = "Groups", context, onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    navController.navigate("groups")
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
                            "${signedInUser.value?.firstName} invites you to join Uni Konnect! Get organized and ace your studies.\n Download now: ${update.updateLink}"
                        ) // Customize the text
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                    scope.launch { drawerState.close() }
                })
                SideBarItem(icon = Icons.Default.ArrowDownward,
                    text = "More",
                    context,
                    onClicked = {
                        scope.launch {
                            drawerState.close()
                        }
                        showBottomSheet = true
                    })
            }
            SideBarItem(icon = Icons.AutoMirrored.Filled.ExitToApp,
                text = "Sign Out",
                context,
                onClicked = {
                    scope.launch {
                        drawerState.close()
                    }
                    userStatus?.let {
                        MyDatabase.writeUserActivity(
                            it.copy(
                                online = "offline",
                                lastTime = getCurrentTimeInAmPm()
                            ), onSuccess = {
                                Toast.makeText(
                                    context,
                                    "Signed Out Successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        )
                    }
                    navController.navigate("login")
                    FirebaseAuth.getInstance().signOut()
                })

        }
    }) {
        Scaffold(
            bottomBar = {
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
                                        showBottomSheet = true
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
            }, containerColor = CC.primary()
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                count = screens.size,
                modifier = Modifier.padding(innerPadding),
                flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
            ) { page ->
                when (screens[page]) {
                    Screen.Home -> Dashboard(navController, context)
                    Screen.Assignments -> AssignmentScreen(navController, context)
                    Screen.Announcements -> AnnouncementsScreen(navController, context)
                    Screen.Timetable -> TimetableScreen(navController, context)
                    Screen.Attendance -> ManageAttendanceScreen(navController, context)
                }
            }
        }
    }
}

@Composable
fun SideProfile(user: User, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(CC.extraColor1()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(CC.secondary(), CircleShape)
                .size(100.dp)
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
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            user.firstName + " " + user.lastName,
            style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(user.id, style = CC.descriptionTextStyle(context))
    }
}


@Composable
fun ModalDrawerItem(
    signedInUser: User,
    user: User,
    context: Context,
    navController: NavController,
    announcement: Announcement,
    users: List<User>,
    userViewModel: UserViewModel,
    chatViewModel: ChatViewModel,
    userGroups: List<GroupEntity>
) {


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
                    .clip(CircleShape)
                    .background(CC.secondary(), CircleShape)
                    .size(50.dp)
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
                        style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    user.firstName + " " + user.lastName,
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
                )
                Text(user.email, style = CC.descriptionTextStyle(context))
                Text(user.id, style = CC.descriptionTextStyle(context))
            }


        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Users", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Select a user to start a chat with them",
            style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text("Long press to view their details", style = CC.descriptionTextStyle(context))
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

        if (userGroups.isEmpty()) {
            Text(
                text = "No groups available",
                style = CC.descriptionTextStyle(context).copy(fontSize = 18.sp),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier.animateContentSize()
            ) {
                items(userGroups) { group ->
                    if (group.name.isNotEmpty() && group.description.isNotEmpty()) {
                        GroupItem(
                            group,
                            context,
                            navController,
                            chatViewModel,
                            userViewModel,
                            signedInUser
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Quick Settings",
            style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(10.dp))
        QuickSettings(navController, context)

    }
}

@Composable
fun QuickSettings(navController: NavController, context: Context) {
    var darkMode by remember { mutableStateOf(false) }

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
            IconButton(onClick = {},
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CC.secondary())
                    .size(50.dp)
            ) {
                Icon(
                    if (darkMode) Icons.Default.Nightlight else Icons.Default.LightMode,
                    "theme",
                    tint = CC.extraColor2()
                )
            }

            Text("App theme", style = CC.descriptionTextStyle(context))
            Switch(
                onCheckedChange = {
                    darkMode = it
                    GlobalColors.saveColorScheme(context, it)
                }, checked = darkMode, colors = SwitchDefaults.colors(
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
fun UserItem(user: User, context: Context, navController: NavController, viewModel: UserViewModel) {
    var visible by remember { mutableStateOf(false) }
    val userStates by viewModel.userStates.observeAsState(emptyMap())
    val userState = userStates[user.id]

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
                .background(CC.tertiary(), CircleShape)
                .clip(CircleShape)
                .combinedClickable(onClick = {
                    navController.navigate("chat/${user.id}")
                }, onLongClick = {
                    visible = !visible
                })
                .size(size), contentAlignment = Alignment.Center) {
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
                    Text(
                        "${user.firstName[0]}${user.lastName[0]}",
                        style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (userState?.online == "online") Color.Green else if(userState?.online == "offline") Color.DarkGray else Color.Red,
                        CircleShape
                    )
                    .align(Alignment.BottomEnd)
                    .offset(x = (-6).dp, y = (-6).dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = if (user.firstName.length > 10) {
                user.firstName.substring(0, 10) + "..."
            } else {
                user.firstName
            }, style = CC.descriptionTextStyle(context), maxLines = 1
        )
        val state = when (userState?.online) {
            "online" -> "Online"
            "offline" -> "Last Seen ${userState.lastTime}"
            else -> "Never Online"
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedVisibility(visible = visible) {
            UserInfo(user = user, state, context)
        }
    }
}

@Composable
fun UserInfo(user: User, userState: String, context: Context) {
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
    Row(modifier = Modifier
        .clickable { onClicked() }
        .height(40.dp)
        .background(CC.secondary())
        .fillMaxWidth(0.9f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start) {
        Icon(
            icon, "", tint = CC.textColor()
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, style = CC.descriptionTextStyle(context))

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
            GlobalColors.loadColorScheme(context)
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
    val screens = listOf(
        Screen.Home, Screen.Announcements, Screen.Assignments, Screen.Timetable, Screen.Attendance
    )

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
        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            Context.RECEIVER_EXPORTED
        )

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
