package com.mike.uniadmin.homeScreen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.announcements.AnnouncementsScreen
import com.mike.uniadmin.assignments.AssignmentScreen
import com.mike.uniadmin.attendance.ManageAttendanceScreen
import com.mike.uniadmin.dashboard.Dashboard
import com.mike.uniadmin.dashboard.Sidebar
import com.mike.uniadmin.getGroupChatViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.helperFunctions.MyDatabase.getUpdate
import com.mike.uniadmin.helperFunctions.Screen
import com.mike.uniadmin.helperFunctions.Update
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
) {

    val screens = listOf(
        Screen.Home, Screen.Announcements, Screen.Assignments, Screen.Attendance
    )
    val coroutineScope = rememberCoroutineScope()

    val userViewModel = getUserViewModel(context)
    val chatViewModel = getGroupChatViewModel(context)

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
    val pagerState = rememberPagerState(pageCount = { screens.size })


    // Side effects
    LaunchedEffect(signedInUser, fetchedUserDetails) {
        //MyDatabase.setUpdate(update = Update(version = "1.0.0", updateLink = ""))
        userViewModel.getSignedInUser()
        signedInUser?.email?.let { email ->
            userViewModel.findUserByEmail(email) { user -> Log.d("Fetched User details", "$user") }
        }

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

    LaunchedEffect(Sidebar.showSideBar.value) {
        if (Sidebar.showSideBar.value) {
            scope.launch {
                drawerState.open()
            }
            Sidebar.showSideBar.value = false
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
            showBottomSheet = { value -> showBottomSheet = value },
            userStatus = userStatus,
            update = update

        )

    }) {
        Scaffold(
            bottomBar = {
                BottomBar(context,
                    screens,
                    pagerState,
                    coroutineScope,
                    drawerState,
                    userViewModel,
                    chatViewModel,
                    scope,
                    showBottomSheet = { showBottomSheet = true })
            }, containerColor = CC.primary()
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