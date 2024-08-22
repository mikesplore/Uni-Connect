package com.mike.uniadmin.dashboard


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.getAnnouncementViewModel
import com.mike.uniadmin.getGroupChatViewModel
import com.mike.uniadmin.getModuleTimetableViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.getNotificationViewModel
import com.mike.uniadmin.getUserViewModel
import kotlinx.coroutines.delay
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun Dashboard(navController: NavController, context: Context) {
    val moduleViewModel = getModuleViewModel(context)
    val userViewModel = getUserViewModel(context)
    val announcementViewModel = getAnnouncementViewModel(context)
    val chatViewModel = getGroupChatViewModel(context)
    val notificationViewModel = getNotificationViewModel(context)
    val timetableViewModel = getModuleTimetableViewModel(context)

    val fetchedModule by moduleViewModel.fetchedModule.observeAsState()
    val timetable by timetableViewModel.timetablesToday.observeAsState()
    val announcements by announcementViewModel.announcements.observeAsState()
    val user by userViewModel.user.observeAsState()
    val modules by moduleViewModel.modules.observeAsState(emptyList())
    val signedInUser by userViewModel.signedInUser.observeAsState()
    var currentUser by remember { mutableStateOf(UserEntity()) }
    val announcementsLoading by announcementViewModel.isLoading.observeAsState()
    val modulesLoading by moduleViewModel.isLoading.observeAsState()
    val isOnline = remember { mutableStateOf(isDeviceOnline(context)) }
    val moduleName by remember { mutableStateOf(fetchedModule?.moduleName) }



    LaunchedEffect(user) {
        userViewModel.checkAllUserStatuses()
        chatViewModel.fetchGroups()
        userViewModel.getSignedInUser()
        timetableViewModel.getTimetableByDay(CC.currentDay())

        timetable?.let {
            Log.d("TIMETABLE", it.toString())
            moduleViewModel.getModuleDetailsByModuleID(it.moduleID)
            Log.d("TIMETABLE", moduleName.toString())
        }

        signedInUser?.email?.let {
            userViewModel.findUserByEmail(it) { fetchedUser ->
                if (fetchedUser != null) {
                    currentUser = fetchedUser
                }
            }
        }
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
            signedInUser = currentUser,
            context = context,
            navController = navController,
            notificationViewModel = notificationViewModel
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f),
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
                        "You are not connected to the internet",
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Modules",
                    style = CC.titleTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (modulesLoading == true) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(5) {
                        Box(
                            modifier = Modifier
                                .padding(start = 15.dp)
                                .height(100.dp)
                        ) {
                            LoadingModuleItem()
                        }
                    }
                }
            } else if (modules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No modules found", style = CC.descriptionTextStyle(context))
                }

            } else {
                ModuleItemList(modules, context, navController)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {

                Text(
                    "Module Resources",
                    style = CC.titleTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (modulesLoading == true) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(5) {
                        Box(
                            modifier = Modifier
                                .padding(start = 15.dp)
                                .height(200.dp)
                        ) {
                            LoadingModuleBox()
                        }
                    }
                }
            } else if (modules.isNotEmpty()) {
                val sortedModules =
                    modules.sortedByDescending { it.visits } // Sort by moduleVisits in descending order

                ModuleBoxList(sortedModules, context, navController, moduleViewModel)
            } else {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No modules found", style = CC.descriptionTextStyle(context))
                }
            }


            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Latest Announcement",
                    style = CC.titleTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (announcementsLoading == true) {

                LoadingAnnouncementCard(context)

            } else if (announcements == null) {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No announcements found", style = CC.descriptionTextStyle(context))
                }
            } else {
                announcements?.lastOrNull()?.let { announcement ->
                    AnnouncementCard(announcement, context)
                }
            }
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


