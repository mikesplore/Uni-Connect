package com.mike.uniadmin.dashboard

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.model.users.UserEntity
import com.mike.uniadmin.getAnnouncementViewModel
import com.mike.uniadmin.getModuleTimetableViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.getNotificationViewModel
import com.mike.uniadmin.getUserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Dashboard(navController: NavController, context: Context) {
    val moduleViewModel = getModuleViewModel(context)
    val userViewModel = getUserViewModel(context)
    val announcementViewModel = getAnnouncementViewModel(context)
    val notificationViewModel = getNotificationViewModel(context)
    val moduleTimetableViewModel = getModuleTimetableViewModel(context)

    val announcements by announcementViewModel.announcements.observeAsState()
    val todayTimetable by moduleTimetableViewModel.timetablesToday.observeAsState()

    val currentUser by userViewModel.user.observeAsState()
    val modules by moduleViewModel.modules.observeAsState(emptyList())
    val announcementsLoading by announcementViewModel.isLoading.observeAsState()
    val isOnline = remember { mutableStateOf(isDeviceOnline(context)) }
    val loggedInUserEmail = UniAdminPreferences.userEmail.value
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val state = rememberPullRefreshState(refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            moduleViewModel.fetchModules()
            moduleTimetableViewModel.getAllModuleTimetables()
            userViewModel.checkAllUserStatuses()
            userViewModel.findUserByEmail(loggedInUserEmail) {}
            announcementViewModel.fetchAnnouncements()
            notificationViewModel.fetchNotifications()
            scope.launch {
                delay(3000)
                if (isRefreshing) {
                    isRefreshing = false
                }
            }
        })


    LaunchedEffect(key1 = 1) {
        while (true) {
            isOnline.value = isDeviceOnline(context)
            delay(10000L) // Check every 10 seconds
        }
    }


    Column(
        modifier = Modifier
            .background(CC.primary())
            .fillMaxSize(),
    ) {
        TopAppBarContent(
            signedInUser = currentUser ?: UserEntity(),
            navController = navController,
            notificationViewModel = notificationViewModel
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .pullRefresh(state)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(visible = !isOnline.value) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, "Warning", tint = Color.Red)
                        Text(
                            "No internet detected",
                            style = CC.descriptionTextStyle()
                                .copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Modules",
                        style = CC.titleTextStyle()
                            .copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (modules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No modules found", style = CC.descriptionTextStyle())
                    }

                } else {
                    modules.forEach {
                        moduleTimetableViewModel.getModuleTimetables(it.moduleCode)
                        moduleTimetableViewModel.findUpcomingClass()
                    }
                    ModuleItemList(modules, navController)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth()) {

                    Text(
                        "Module Resources",
                        style = CC.titleTextStyle()
                            .copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (modules.isNotEmpty()) {
                    val sortedModules =
                        modules.sortedByDescending { it.visits } // Sort by moduleVisits in descending order

                    ModuleBoxList(sortedModules, navController, moduleViewModel)
                } else {
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No modules found", style = CC.descriptionTextStyle())
                    }
                }


                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Latest Announcement",
                        style = CC.titleTextStyle()
                            .copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (announcementsLoading == true) {

                    LoadingAnnouncementCard()

                } else if (announcements?.isEmpty() == true) {
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No announcements found", style = CC.descriptionTextStyle())
                    }
                } else {
                    announcements?.maxByOrNull { it.date }?.let { announcement ->
                        AnnouncementCard(announcement)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "My Next Class",
                        style = CC.titleTextStyle()
                            .copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                if (todayTimetable == null) {
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No timetable found", style = CC.descriptionTextStyle())
                    }
                } else {
                    ModuleTimetableCard(todayTimetable!!)
                }
            }
            PullRefreshIndicator(
                contentColor = CC.secondary(),
                backgroundColor = CC.extraColor2(),
                refreshing = isRefreshing,
                state = state,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}


fun isDeviceOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}


