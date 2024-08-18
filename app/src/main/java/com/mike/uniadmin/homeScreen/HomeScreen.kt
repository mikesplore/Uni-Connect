package com.mike.uniadmin.homeScreen

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.DeviceTheme
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.attendance.ManageAttendanceScreen
import com.mike.uniadmin.announcements.AnnouncementsScreen
import com.mike.uniadmin.assignments.AssignmentScreen
import com.mike.uniadmin.clearAllPreferences
import com.mike.uniadmin.dashboard.Dashboard
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserStateEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.getUpdate
import com.mike.uniadmin.model.Screen
import com.mike.uniadmin.model.Update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
)
@Composable
fun HomeScreen(
    navController: NavController,
    context: Context,
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

    // Local state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf(Update()) }

    val signedInUserLoading by userViewModel.isLoading.observeAsState()
    val pagerState = rememberPagerState(pageCount = {screens.size})


    // Side effects
    LaunchedEffect(signedInUser, fetchedUserDetails) {
        // 1. Get signed-in user and fetch details (if needed)
        userViewModel.getSignedInUser()
        signedInUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) { user -> Log.d("Fetched User details", "$user") }
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
                    modifier = Modifier.padding(innerPadding),
                    flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
                ) { page ->
                    when (screens[page]) {
                        Screen.Home -> Dashboard(navController, context)
                        Screen.Assignments -> AssignmentScreen(context)
                        Screen.Announcements -> AnnouncementsScreen(context)
                        Screen.Attendance -> ManageAttendanceScreen(context)
                    }
                }
            }
        }
    }
}