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
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModel
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModelFactory
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableViewModel
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableViewModelFactory
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.notifications.NotificationViewModel
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import kotlinx.coroutines.delay
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun Dashboard(navController: NavController, context: Context) {
    val application = context.applicationContext as UniAdmin
    val chatRepository = remember { application.chatRepository }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.ChatViewModelFactory(chatRepository)
    )

    val dashboardAdmin = context.applicationContext as? UniAdmin
    val userRepository = remember { dashboardAdmin?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("UserRepository is null")
        )
    )


    val courseRepository = remember { dashboardAdmin?.courseRepository }
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(
            courseRepository ?: throw IllegalStateException("CourseRepository is null")
        )
    )

    val announcementRepository = remember { dashboardAdmin?.announcementRepository }
    val announcementViewModel: AnnouncementViewModel = viewModel(
        factory = AnnouncementViewModelFactory(
            announcementRepository ?: throw IllegalStateException("AnnouncementRepository is null")
        )
    )

    val timetableRepository = remember { dashboardAdmin?.courseTimetableRepository }
    val timetableViewModel: CourseTimetableViewModel = viewModel(
        factory = CourseTimetableViewModelFactory(
            timetableRepository ?: throw IllegalStateException("TimetableRepository is null")
        )
    )

    val notificationAdmin = context.applicationContext as UniAdmin
    val notificationRepository = remember { notificationAdmin.notificationRepository }
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.NotificationViewModelFactory(notificationRepository)
    )

    val fetchedCourse by courseViewModel.fetchedCourse.observeAsState()
    val timetable by timetableViewModel.timetablesToday.observeAsState()
    val announcements by announcementViewModel.announcements.observeAsState()
    val user by userViewModel.user.observeAsState()
    val courses by courseViewModel.courses.observeAsState(emptyList())
    val signedInUser by userViewModel.signedInUser.observeAsState()
    var currentUser by remember { mutableStateOf(UserEntity()) }
    val announcementsLoading by announcementViewModel.isLoading.observeAsState()
    val coursesLoading by courseViewModel.isLoading.observeAsState()
    val isOnline = remember { mutableStateOf(isDeviceOnline(context)) }
    val courseName by remember { mutableStateOf(fetchedCourse?.courseName) }



    LaunchedEffect(user) {

        userViewModel.checkAllUserStatuses()
        chatViewModel.fetchGroups()
        userViewModel.getSignedInUser()
        timetableViewModel.getTimetableByDay(CC.currentDay())

        timetable?.let {
            Log.d("TIMETABLE", it.toString())
            courseViewModel.getCourseDetailsByCourseID(it.courseID)
            Log.d("TIMETABLE", courseName.toString())
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
            userViewModel = userViewModel,
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
                    "Courses",
                    style = CC.titleTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            if (coursesLoading == true) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(5) {
                        Box(
                            modifier = Modifier
                                .padding(start = 15.dp)
                                .height(100.dp)
                        ) {
                            LoadingCourseItem()
                        }
                    }
                }
            } else if (courses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No courses found", style = CC.descriptionTextStyle(context))
                }

            } else {
                LazyRow(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // Add spacing between items
                ) {
                    items(courses) { course ->
                        CourseItem(course, context, navController)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {

                Text(
                    "Course Resources",
                    style = CC.titleTextStyle(context)
                        .copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (coursesLoading == true) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(5) {
                        Box(
                            modifier = Modifier
                                .padding(start = 15.dp)
                                .height(200.dp)
                        ) {
                            LoadingCourseBox()
                        }
                    }
                }
            } else if (courses.isNotEmpty()) {
                val sortedCourses =
                    courses.sortedByDescending { it.visits } // Sort by courseVisits in descending order

                LazyRow(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedCourses) { course -> // Use the sorted list
                        CourseBox(course, context, navController, onClicked = {
                            courseViewModel.saveCourse(
                                course.copy(
                                    visits = course.visits.plus(
                                        1
                                    )
                                )
                            )
                        })
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No courses found", style = CC.descriptionTextStyle(context))
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


