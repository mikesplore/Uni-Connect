package com.mike.uniadmin.courseContent


import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncementViewModel
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncementViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignmentViewModel
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignmentViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetailViewModel
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetailViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetableViewModel
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetableViewModelFactory
import com.mike.uniadmin.backEnd.courses.CourseViewModel
import com.mike.uniadmin.backEnd.courses.CourseViewModelFactory
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.model.randomColor
import kotlinx.coroutines.launch
import com.mike.uniadmin.ui.theme.CommonComponents as CC

var background = randomColor.random()


@Composable
fun CourseContent(context: Context, targetCourseID: String) {

    val courseResource = context.applicationContext as? UniAdmin
    val courseRepository = remember { courseResource?.courseRepository }
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(
            courseRepository ?: throw IllegalStateException("CourseRepository is null")
        )
    )


    val courseAnnouncementRepository = remember { courseResource?.courseAnnouncementRepository }
    val courseAnnouncementViewModel: CourseAnnouncementViewModel = viewModel(
        factory = CourseAnnouncementViewModelFactory(
            courseAnnouncementRepository
                ?: throw IllegalStateException("CourseAnnouncementRepository is null")
        )
    )

    val courseAssignmentRepository = remember { courseResource?.courseAssignmentRepository }
    val courseAssignmentViewModel: CourseAssignmentViewModel = viewModel(
        factory = CourseAssignmentViewModelFactory(
            courseAssignmentRepository
                ?: throw IllegalStateException("CourseAssignmentRepository is null")
        )
    )

    val courseDetailRepository = remember { courseResource?.courseDetailRepository }
    val courseDetailViewModel: CourseDetailViewModel = viewModel(
        factory = CourseDetailViewModelFactory(
            courseDetailRepository ?: throw IllegalStateException("CourseDetailsRepository is null")
        )
    )

    val courseTimetableRepository = remember { courseResource?.courseTimetableRepository }
    val courseTimetableViewModel: CourseTimetableViewModel = viewModel(
        factory = CourseTimetableViewModelFactory(
            courseTimetableRepository
                ?: throw IllegalStateException("CourseTimetableRepository is null")
        )
    )

    val userRepository = remember { courseResource?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("CourseTimetableRepository is null")
        )
    )

    val announcementsLoading by courseAnnouncementViewModel.isLoading.observeAsState(initial = false)
    val assignmentsLoading by courseAssignmentViewModel.isLoading.observeAsState(initial = false)
    val timetablesLoading by courseTimetableViewModel.isLoading.observeAsState(initial = false)
    val detailsLoading by courseDetailViewModel.isLoading.observeAsState(initial = false)

    val coroutineScope = rememberCoroutineScope()
    val courseInfo by courseViewModel.fetchedCourse.observeAsState(initial = null)

    LaunchedEffect(targetCourseID) {
        background = randomColor.random()

        courseViewModel.getCourseDetailsByCourseID(targetCourseID)
        courseAnnouncementViewModel.getCourseAnnouncements(targetCourseID)
        courseAssignmentViewModel.getCourseAssignments(targetCourseID)
        courseTimetableViewModel.getCourseTimetables(targetCourseID)
        courseDetailViewModel.getCourseDetails(targetCourseID)
    }

    Scaffold(
        containerColor = CC.primary(),
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .padding(it)
                .fillMaxSize()
                .background(CC.primary()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .requiredHeight(200.dp)
                    .fillMaxWidth(0.9f)
                    .background(Color.Black.copy(alpha = 0.5f)), // Semi-transparent background
                contentAlignment = Alignment.Center
            ) {
                courseInfo?.courseName?.let { courseName ->
                    AsyncImage(
                        model = courseInfo!!.courseImageLink,
                        contentDescription = null,
                        modifier = Modifier
                            .blur(5.4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(
                                CC
                                    .primary()
                                    .copy(0.5f), RoundedCornerShape(10.dp)
                            )
                    ) {
                        Text(
                            text = courseName,
                            modifier = Modifier
                                .padding(10.dp)
                                .padding(16.dp), // Added padding
                            style = CC.titleTextStyle(context).copy(
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                brush = Brush.linearGradient(
                                    colors = listOf(CC.extraColor2(), CC.textColor(), CC.tertiary())
                                ),
                                shadow = Shadow( // Added shadow
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                ),
                                fontSize = 30.sp
                            )
                        )
                    }
                }
            }
            //the tabs column starts here
            Column(
                modifier = Modifier
                    .background(CC.primary())
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                var selectedTabIndex by remember { mutableIntStateOf(0) }
                val configuration = LocalConfiguration.current
                val screenWidth = configuration.screenWidthDp.dp
                val tabRowHorizontalScrollState by remember { mutableStateOf(ScrollState(0)) }
                val tabTitles = listOf(
                    "Announcements",
                    "Assignments",
                    "Timetable",
                    "Details",
                )
                val indicator = @Composable { tabPositions: List<TabPosition> ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(4.dp)
                            .width(screenWidth / tabTitles.size) // Divide by the number of tabs
                            .background(CC.textColor(), CircleShape)
                    )
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    indicator = indicator,
                    edgePadding = 0.dp,
                    modifier = Modifier.imePadding(),
                    containerColor = CC.primary()

                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(selected = selectedTabIndex == index, onClick = {
                            selectedTabIndex = index
                            coroutineScope.launch {
                                tabRowHorizontalScrollState.animateScrollTo(
                                    (screenWidth.value / tabTitles.size * index).toInt()
                                )
                            }
                        }, text = {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedTabIndex == index) background else CC.extraColor2(),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(8.dp), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    style = CC.descriptionTextStyle(context),
                                    color = if (selectedTabIndex == index) CC.textColor() else CC.secondary()
                                )
                            }
                        })
                    }
                }

                when (selectedTabIndex) {
                    0 -> {
                        if (announcementsLoading) {
                            LoadingIndicator()
                        } else {
                            AnnouncementsItem(
                                targetCourseID, courseAnnouncementViewModel, context, userViewModel
                            )
                        }
                    }

                    1 -> {
                        if (assignmentsLoading) {
                            LoadingIndicator()
                        } else {
                            AssignmentsItem(
                                targetCourseID, courseAssignmentViewModel, context, userViewModel

                            )
                        }
                    }

                    2 -> {
                        if (timetablesLoading) {
                            LoadingIndicator()
                        } else {
                            TimetableItem(
                                targetCourseID, courseTimetableViewModel, context
                            )
                        }
                    }

                    3 -> {
                        if (detailsLoading) {
                            LoadingIndicator()
                        } else {
                            DetailsItem(targetCourseID, courseDetailViewModel, context)
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        CircularProgressIndicator(color = CC.textColor())
    }
}


@Composable
internal fun AddTextField(
    label: String, value: String, onValueChange: (String) -> Unit, context: Context, singleLine: Boolean = true, maxLines: Int = 1
) {
    TextField(
        value = value,
        textStyle = CC.descriptionTextStyle(context),
        onValueChange = onValueChange,
        placeholder = { Text(label, style = CC.descriptionTextStyle(context)) },
        modifier = Modifier
            .heightIn(min = 20.dp, max = 100.dp)
            .fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CC.primary(),
            focusedPlaceholderColor = CC.textColor().copy(0.5f),
            unfocusedContainerColor = CC.primary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedIndicatorColor = CC.textColor(),
            unfocusedIndicatorColor = CC.textColor(),
            cursorColor = CC.textColor()

        ),
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(10.dp)
    )

}








