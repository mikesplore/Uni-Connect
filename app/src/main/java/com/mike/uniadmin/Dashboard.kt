package com.mike.uniadmin


import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.dataModel.announcements.Announcement
import com.mike.uniadmin.dataModel.announcements.AnnouncementRepository
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModel
import com.mike.uniadmin.dataModel.announcements.AnnouncementViewModelFactory
import com.mike.uniadmin.dataModel.courses.Course
import com.mike.uniadmin.dataModel.courses.CourseRepository
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.ChatRepository
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.User
import com.mike.uniadmin.dataModel.users.UserRepository
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.ui.theme.GlobalColors
import com.mike.uniadmin.CommonComponents as CC


@Composable
fun Dashboard(navController: NavController, context: Context) {
    val currentPerson = FirebaseAuth.getInstance().currentUser
    val courseRepository = remember { CourseRepository() }
    val userRepository = remember { UserRepository() }
    val application = context.applicationContext as UniAdmin
    val chatRepository = remember { application.chatRepository }
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
    val users by userViewModel.users.observeAsState(emptyList())
    val groups by chatViewModel.groups.observeAsState(emptyList())
    val announcements by announcementViewModel.announcements.observeAsState(emptyList())
    val courses by courseViewModel.courses.observeAsState(emptyList())
    val signedInUser = remember { mutableStateOf<User?>(null) }
    val announcement by remember { mutableStateOf(Announcement()) }

    //this user state will return the state of a user based on their userID
    //it is defined as val userStates: Map<String, UserState>


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
        }
    }

    Column(
        modifier = Modifier
            .background(CC.primary())
            .fillMaxSize()
    ) {
        signedInUser.value?.let { user ->
            TopAppBarContent(user.profileImageLink, user, context, navController)
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(1f)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Courses",
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 15.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
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
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Course Resources",
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 15.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                val sortedCourses =
                    courses.sortedByDescending { it.visits } // Sort by courseVisits in descending order

                LazyRow(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedCourses) { course -> // Use the sorted list
                        CourseBox(course,
                            context,
                            navController,
                            onClicked = { courseViewModel.saveCourse(course.copy(visits = course.visits + 1)) })
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Latest Announcement",
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 15.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                announcements.firstOrNull()?.let { announcement ->
                    AnnouncementCard(announcement, context, navController)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        } ?: run {
            // Show a loading indicator while user details are loading
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

}

@Composable
fun CourseItem(course: Course, context: Context, navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier
            .background(CC.tertiary(), CircleShape)
            .clip(CircleShape)
            .clickable { navController.navigate("courseContent/${course.courseCode}") }
            .size(70.dp),
            contentAlignment = Alignment.Center) {
            AsyncImage(
                model = course.courseImageLink,
                contentDescription = course.courseName,
                modifier = Modifier
                    .clip(CircleShape)
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Icon(
                Icons.Default.School, "Icon", tint = CC.textColor()
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = if (course.courseName.length > 10) {
                course.courseName.substring(0, 10) + "..."
            } else {
                course.courseName
            }, style = CC.descriptionTextStyle(context), maxLines = 1
        )
    }
}


@Composable
fun CourseBox(
    course: Course,
    context: Context,
    navController: NavController,
    onClicked: (Course) -> Unit
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
                Text(
                    course.courseName,
                    style = CC.titleTextStyle(context)
                        .copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
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
                    navController.navigate("courseContent/${course.courseCode}")
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
fun AnnouncementCard(announcement: Announcement, context: Context, navController: NavController) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .heightIn(min = 100.dp, max = 250.dp)
                .fillMaxWidth(0.9f)
                .padding(8.dp),
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
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.notification),
                        "",
                        tint = CC.textColor(),
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = announcement.title,
                        style = CC.titleTextStyle(context),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = announcement.description, style = CC.descriptionTextStyle(context)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = announcement.date,
                        style = CC.descriptionTextStyle(context)
                            .copy(color = CC.textColor().copy(alpha = 0.7f))
                    )

                    Text(
                        text = announcement.author,
                        style = CC.descriptionTextStyle(context)
                            .copy(color = CC.textColor().copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContent(
    profileImageUrl: String, signedInUser: User, context: Context, navController: NavController
) {
    var expanded by remember { mutableStateOf(true) }

    TopAppBar(title = {
        Row(
            modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(CC.secondary(), CircleShape)
                    .clip(CircleShape)
                    .size(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (profileImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(R.drawable.student), contentDescription = ""
                    )
                }
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
                Text(
                    text = signedInUser.firstName,
                    style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = CC.primary(),
    ))
}



