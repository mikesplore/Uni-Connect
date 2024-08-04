package com.mike.uniadmin.home


import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.courseResources.CourseName
import com.mike.uniadmin.dataModel.announcements.AnnouncementEntity
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModel
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModelFactory
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetable
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableViewModel
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableViewModelFactory
import com.mike.uniadmin.dataModel.courses.CourseEntity
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.notifications.NotificationEntity
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

    val userAdmin = context.applicationContext as? UniAdmin
    val userRepository = remember { userAdmin?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("UserRepository is null")
        )
    )

    val announcementAdmin = context.applicationContext as? UniAdmin

    val courseRepository = remember { announcementAdmin?.courseRepository }
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(
            courseRepository ?: throw IllegalStateException("CourseRepository is null")
        )
    )

    val announcementRepository = remember { announcementAdmin?.announcementRepository }
    val announcementViewModel: AnnouncementViewModel = viewModel(
        factory = AnnouncementViewModelFactory(
            announcementRepository ?: throw IllegalStateException("AnnouncementRepository is null")
        )
    )
    val timetableRepository = remember { announcementAdmin?.courseTimetableRepository }
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

    val timetables by timetableViewModel.timetablesToday.observeAsState()
    val announcements by announcementViewModel.announcements.observeAsState()
    val user by userViewModel.user.observeAsState()
    val courses by courseViewModel.courses.observeAsState(emptyList())
    val signedInUser by userViewModel.signedInUser.observeAsState()
    var currentUser by remember { mutableStateOf(UserEntity()) }
    val announcementsLoading by announcementViewModel.isLoading.observeAsState()
    val coursesLoading by courseViewModel.isLoading.observeAsState()
    val timetablesLoading by timetableViewModel.isLoading.observeAsState()

    val isOnline = remember { mutableStateOf(isDeviceOnline(context)) }



    LaunchedEffect(user) {

        userViewModel.checkAllUserStatuses()
        chatViewModel.fetchGroups()
        userViewModel.getSignedInUser()
        timetableViewModel.getTimetableByDay(CC.currentDay())
        timetables?.let { timetableList ->
            timetableList.forEach { timetable ->
                timetable.courseID?.let { courseViewModel.getCourseDetailsByCourseID(courseCode = it) }
                Log.d("Course", timetable.courseID.toString())
            }
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
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold),
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
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold),
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
            } else if(courses.isNotEmpty()) {
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
                                    visits = course.visits?.plus(
                                        1
                                    )
                                )
                            )
                        })
                    }
                }
            }else{
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
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (announcementsLoading == true) {

                LoadingAnnouncementCard()

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
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${CC.currentDay()}'s timetable",
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (timetablesLoading == true) {
                LoadingTodayTimetable()
            } else if (timetables==null) {
                Row(
                    modifier = Modifier
                        .background(CC.extraColor1(), RoundedCornerShape(10.dp))
                        .height(200.dp)
                        .fillMaxWidth(0.9f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("No timetable found for today", style = CC.descriptionTextStyle(context))
                }
            } else {
                LazyRow {
                    items(timetables!!) { timetable ->
                        TodayTimetable("", timetable, context)
                    }
                }
            }
        }
    }
}

@Composable
fun TodayTimetable(courseName: String, timetable: CourseTimetable, context: Context) {
    Card(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp)
            .border(
                1.dp,
                CC.extraColor2().copy(0.5f),
                RoundedCornerShape(10.dp)
            )
            .height(200.dp)
            .padding(start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = CC.extraColor1()
        )
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
        ) {
            Text(
                courseName,
                style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Timelapse, "time", tint = CC.textColor()
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "${timetable.startTime} to ${timetable.endTime}",
                    style = CC.descriptionTextStyle(context)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn, "Location", tint = CC.textColor()
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("${timetable.venue}", style = CC.descriptionTextStyle(context))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxSize(1f), verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(10.dp))
                Text("Please Keep Time!", style = CC.titleTextStyle(context))
            }

        }
    }

}

@Composable
fun LoadingTodayTimetable() {
    Card(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp)
            .border(
                1.dp,
                CC
                    .extraColor2()
                    .copy(0.5f),
                RoundedCornerShape(10.dp)
            )
            .height(200.dp)
            .width(350.dp), elevation = CardDefaults.elevatedCardElevation(
            4.dp
        ), colors = CardDefaults.cardColors(
            containerColor = CC.extraColor1()
        )
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
        ) {
            CC.ColorProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                CC.ColorProgressIndicator(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                CC.ColorProgressIndicator(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxSize(1f), verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(10.dp))
                CC.ColorProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

        }
    }

}


@Composable
fun CourseItem(course: CourseEntity, context: Context, navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier
            .border(
                1.dp, CC.textColor(), CircleShape
            )
            .background(CC.tertiary(), CircleShape)
            .clip(CircleShape)
            .clickable {
                navController.navigate("courseContent/${course.courseCode}")
            }
            .size(70.dp), contentAlignment = Alignment.Center) {
            if (course.courseImageLink?.isNotEmpty() == true) {
                AsyncImage(
                    model = course.courseImageLink,
                    contentDescription = course.courseName,
                    modifier = Modifier
                        .clip(CircleShape)
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.School, "Icon", tint = CC.textColor()
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        (if (course.courseName?.length!! > 10) {
            course.courseName.substring(0, 10) + "..."
        } else {
            course.courseName
        }).let {
            Text(
                text = it, style = CC.descriptionTextStyle(context), maxLines = 1
            )
        }
    }
}

@Composable
fun LoadingCourseItem() {
    Column(
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .border(
                    1.dp, CC.textColor(), CircleShape
                )
                .background(CC.primary(), CircleShape)
                .clip(CircleShape)
                .size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())

        }
        Spacer(modifier = Modifier.height(5.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .width(90.dp)
                .height(15.dp)
        ) {
            CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
        }

    }
}


@Composable
fun CourseBox(
    course: CourseEntity,
    context: Context,
    navController: NavController,
    onClicked: (CourseEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .border(
                1.dp, CC.secondary(), RoundedCornerShape(16.dp)
            )
            .width(200.dp)
            .height(230.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.4f)
                .background(CC.extraColor2(), RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = course.courseImageLink,
                contentDescription = course.courseName,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp, 16.dp))
                    .fillMaxSize(),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(CC.extraColor1(), RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp))
                .fillMaxWidth(), verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 10.dp, top = 5.dp)
                    .fillMaxWidth()
            ) {
                Text(course.courseCode, style = CC.descriptionTextStyle(context))
            }
            Row(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .fillMaxWidth()
            ) {
                course.courseName?.let {
                    Text(
                        it,
                        style = CC.titleTextStyle(context)
                            .copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val visits = when (course.visits) {
                    0 -> {
                        "Never visited"
                    }

                    1 -> {
                        "Visited once"
                    }

                    else -> {
                        "Visited ${course.visits} times"
                    }
                }

                Text(visits, style = CC.descriptionTextStyle(context))
                IconButton(onClick = {
                    onClicked(course)
                    CourseName.name.value = course.courseName.toString()
                    navController.navigate("courseResource/${course.courseCode}")
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos, "", tint = CC.textColor()
                    )
                }
            }
        }
    }
}


@Composable
fun LoadingCourseBox() {
    Column(
        modifier = Modifier
            .border(
                1.dp, CC.secondary(), RoundedCornerShape(16.dp)
            )
            .width(200.dp)
            .height(230.dp), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.4f)
                .background(CC.primary(), RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                .fillMaxWidth()
        ) {
            CC.ColorProgressIndicator(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
                    .fillMaxSize()
            )
        }
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(CC.extraColor1(), RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp))
                .fillMaxWidth(), verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier
                    .height(20.dp)
                    .width(100.dp)
                    .padding(start = 10.dp)
                    .fillMaxWidth()
            ) {
                CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
            }
            Row(
                modifier = Modifier
                    .height(25.dp)
                    .padding(start = 10.dp, end = 10.dp)
                    .fillMaxWidth()
            ) {
                CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
            }
            Row(
                modifier = Modifier
                    .width(100.dp)
                    .height(25.dp)
                    .padding(start = 10.dp, bottom = 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun AnnouncementCard(announcement: AnnouncementEntity, context: Context) {
    Card(
        modifier = Modifier
            .heightIn(min = 200.dp)
            .fillMaxWidth()
            .padding(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.extraColor1()
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(CC.extraColor1())
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            1.dp, CC.textColor(), CircleShape
                        )
                        .clip(CircleShape)
                        .background(CC.secondary(), CircleShape)
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (announcement.imageLink?.isNotEmpty() == true) {
                        AsyncImage(
                            model = announcement.imageLink,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            "${announcement.authorName?.get(0)}",
                            style = CC.descriptionTextStyle(context)
                                .copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                announcement.title?.let {
                    Row(modifier = Modifier.fillMaxWidth(1f)) {
                        Text(
                            text = it,
                            style = CC.titleTextStyle(context),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                announcement.description?.let {
                    Text(
                        text = it,
                        style = CC.descriptionTextStyle(context),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }

            }


            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                announcement.date?.let {
                    Text(
                        text = it,
                        style = CC.descriptionTextStyle(context)
                            .copy(color = CC.textColor().copy(alpha = 0.7f))
                    )
                }

                announcement.authorName?.let {
                    Text(
                        text = it,
                        style = CC.descriptionTextStyle(context)
                            .copy(color = CC.textColor().copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}


@Composable
fun LoadingAnnouncementCard() {
    Card(
        modifier = Modifier
            .heightIn(min = 200.dp)
            .fillMaxWidth()
            .padding(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.extraColor1()
        ),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(CC.extraColor1())
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            1.dp, CC.textColor(), CircleShape
                        )
                        .clip(CircleShape)
                        .size(40.dp), contentAlignment = Alignment.Center
                ) {
                    CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(8.dp))
                CC.ColorProgressIndicator(
                    modifier = Modifier
                        .width(150.dp)
                        .height(30.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
            ) {
                CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())

            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CC.ColorProgressIndicator(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .height(20.dp)
                        .width(100.dp)
                )

                CC.ColorProgressIndicator(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .height(20.dp)
                        .width(100.dp)
                )
            }
        }
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContent(
    signedInUser: UserEntity,
    context: Context,
    navController: NavController,
    userViewModel: UserViewModel,
    notificationViewModel: NotificationViewModel
) {
    val loading by userViewModel.isLoading.observeAsState()
    val notifications by notificationViewModel.notifications.observeAsState()
    val isOnline = remember { mutableStateOf(isDeviceOnline(context)) }
    var expanded by remember { mutableStateOf(false) }

    // periodically check the network status
    LaunchedEffect(Unit) {
        while (true) {
            isOnline.value = isDeviceOnline(context)
            delay(10000L) // Check every 10 seconds
        }
    }

    TopAppBar(title = {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BoxWithConstraints(
                modifier = Modifier.padding(end = 10.dp)
            ) {
                val size = 50.dp
                Box(
                    modifier = Modifier
                        .border(
                            1.dp, CC.textColor(), CircleShape
                        )
                        .background(CC.secondary(), CircleShape)
                        .clip(CircleShape)
                        .size(size),
                    contentAlignment = Alignment.Center
                ) {

                    if (loading == true) {
                        CircularProgressIndicator(color = CC.textColor())
                    } else if (signedInUser.firstName.isNullOrEmpty()) {
                        Icon(
                            Icons.Default.AccountCircle, "Location", tint = CC.textColor()
                        )
                    } else {
                        if (signedInUser.profileImageLink?.isNotEmpty() == true) {
                            AsyncImage(
                                model = signedInUser.profileImageLink,
                                contentDescription = signedInUser.firstName,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                "${signedInUser.firstName[0]}${signedInUser.lastName?.get(0)}",
                                style = CC.titleTextStyle(context)
                                    .copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                val onlineStatus by animateColorAsState(
                    animationSpec = tween(500, easing = LinearEasing),
                    targetValue = if (isOnline.value) Color.Green else Color.Red,
                    label = ""
                )
                // the small dot
                Box(
                    modifier = Modifier
                        .border(
                            1.dp, CC.secondary(), CircleShape
                        )
                        .size(12.dp)
                        .background(
                            onlineStatus, CircleShape
                        )
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = (-6).dp)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 10.dp, end = 20.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = CC.getGreetingMessage(),
                    style = CC.descriptionTextStyle(context)
                        .copy(color = CC.textColor().copy(alpha = 0.5f))
                )
                if (signedInUser.firstName != null) {
                    Text(
                        text = signedInUser.firstName,
                        style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .height(20.dp)
                            .width(150.dp)
                    ) {
                        CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
                    }
                }

            }
        }
    }, actions = {
        BoxWithConstraints(modifier = Modifier.padding(end = 5.dp)) {
            IconButton(onClick = {
                notificationViewModel.fetchNotifications()
                expanded = !expanded
            }, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = CC.secondary(),
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .border(
                            1.dp, CC.secondary(), CircleShape
                        )
                        .size(10.dp)
                        .background(
                            if (notifications?.isNotEmpty() == true) Color.Green else Color.Red,
                            CircleShape
                        )
                        .align(Alignment.TopCenter)
                        .offset(y = (10).dp, x = (8).dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .heightIn(max = 500.dp)
                    .width(160.dp)
                    .background(CC.extraColor1())
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    if (notifications != null && notifications!!.isNotEmpty()) {
                        notifications!!.take(5).forEach { notification ->
                            NotificationTitleContent(notification, context)
                        }
                        HorizontalDivider()
                        TextButton(
                            onClick = {
                                navController.navigate("notifications")
                                expanded = false
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View More", style = CC.descriptionTextStyle(context))
                        }
                    } else {
                        Text("No new notifications", style = CC.descriptionTextStyle(context))
                    }
                }
            }
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = CC.primary(),
    )
    )
}


@Composable
fun NotificationTitleContent(
    notification: NotificationEntity, context: Context
) {
    Row(modifier = Modifier
        .height(30.dp)
        .padding(5.dp)
        .fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        notification.title?.let {
            Text(
                text = it,
                style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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


