package com.mike.uniadmin


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalPostOffice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mike.uniadmin.model.Course
import com.mike.uniadmin.model.CourseAnnouncement
import com.mike.uniadmin.model.CourseAssignment
import com.mike.uniadmin.model.CourseDetails
import com.mike.uniadmin.model.CourseTimetable
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.randomColor
import com.mike.uniadmin.ui.theme.GlobalColors
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import com.mike.uniadmin.CommonComponents as CC

val background = randomColor.random()


@Composable
fun CourseContent(navController: NavController, context: Context, targetCourseID: String) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
    }

    Scaffold(
        containerColor = CC.primary(),
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(CC.primary())
        ) {
            Row(
                modifier = Modifier
                    .background(background)
                    .padding(start = 10.dp, end = 10.dp)
                    .requiredHeight(200.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val gradientColors = listOf(CC.extraColor2(), CC.textColor(), CC.extraColor1())

                Text(
                    text = "Advanced Database Management Systems",
                    style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        ),
                        fontSize = 30.sp
                    )
                )

            }
            //the tabs column starts here
            Column(
                modifier = Modifier
                    .background(background.copy(0.2f))
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
                    0 -> AnnouncementsItem(targetCourseID, navController, context)
                    1 -> AssignmentsItem(targetCourseID, navController, context)
                    2 -> TimetableItem(targetCourseID, navController, context)
                    3 -> DetailsItem(targetCourseID, navController, context)
                    else -> {}
                }
            }
        }
    }
}


@Composable
fun AnnouncementsItem(courseID: String, navController: NavController, context: Context) {
    var visible by remember { mutableStateOf(false) }
    var announcements by remember { mutableStateOf<List<CourseAnnouncement>?>(null) }

    LaunchedEffect(courseID) {
        MyDatabase.getCourseAnnouncements(courseID) { fetchedAnnouncements ->
            announcements = fetchedAnnouncements
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End
        ) {
            FloatingActionButton(
                onClick = { visible = !visible },
                modifier = Modifier.size(30.dp),
                containerColor = CC.extraColor2(),
                contentColor = CC.textColor()
            ) {
                Icon(Icons.Default.Add, "Add announcement")
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible) {
                AddAnnouncementItem(
                    courseID,
                    "Admin",
                    context,
                    visible,
                    onExpandedChange = { visible = !visible })
            }
            announcements?.let {
                LazyColumn {
                    items(it) { announcement ->
                        AnnouncementCard(announcement, context)
                    }
                }
            } ?: run {
                Text("No announcements found", style = CC.descriptionTextStyle(context))
            }
        }
    }
}


@Composable
fun AnnouncementCard(
    courseAnnouncement: CourseAnnouncement, context: Context
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = background.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = courseAnnouncement.title, style = CC.titleTextStyle(context)
            )
            Text(
                text = courseAnnouncement.description,
                style = CC.descriptionTextStyle(context).copy(
                    color = CC.textColor().copy(0.5f), textAlign = TextAlign.Center
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = courseAnnouncement.author,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CC.extraColor2()
                )
                Text(
                    text = courseAnnouncement.date, fontSize = 12.sp, color = Color.LightGray
                )
            }
        }
    }
}


@Composable
fun AddAnnouncementItem(
    courseID: String,
    senderName: String,
    context: Context,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    val currentDate = Date()
    val formatter =
        DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()) // Use default locale
    val date = formatter.format(currentDate)
    var description by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
        AddTextField(
            label = "Title", value = title, onValueChange = { title = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "Description",
            value = description,
            onValueChange = { description = it },
            context
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    loading = true
                    MyDatabase.generateAnnouncementID { iD ->
                        val newAnnouncement = CourseAnnouncement(
                            announcementID = iD,
                            title = title,
                            description = description,
                            author = senderName,
                            date = date
                        )
                        MyDatabase.writeCourseAnnouncement(courseID = courseID,
                            courseAnnouncement = newAnnouncement,
                            onResult = { success ->
                                if (success) {
                                    loading = false
                                    onExpandedChange(expanded)
                                } else {
                                    loading = false
                                    onExpandedChange(expanded)
                                    Log.e("Error", "Failed to write announcement")
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }

                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                ), modifier = Modifier.width(100.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = background, modifier = Modifier.size(20.dp))
                } else {
                    Text("Post", style = CC.descriptionTextStyle(context))
                }
            }
            Button(
                onClick = {
                    onExpandedChange(expanded)
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                )
            ) {
                Text("Cancel", style = CC.descriptionTextStyle(context))
            }

        }

    }
}

@Composable
fun AddTextField(
    label: String, value: String, onValueChange: (String) -> Unit, context: Context
) {
    TextField(
        value = value,
        textStyle = CC.descriptionTextStyle(context),
        onValueChange = onValueChange,
        label = { Text(label, style = CC.descriptionTextStyle(context)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CC.primary(),
            unfocusedContainerColor = CC.primary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedIndicatorColor = CC.textColor(),
            unfocusedIndicatorColor = CC.textColor()

        ),
        shape = RoundedCornerShape(10.dp)
    )

}

@Composable
fun AssignmentsItem(courseID: String, navController: NavController, context: Context) {
    var expanded by remember { mutableStateOf(false) }
    var assignments by remember { mutableStateOf<List<CourseAssignment>?>(null) }

    LaunchedEffect(courseID) {
        MyDatabase.getCourseAssignments(courseID) { fetchedAssignments ->
            assignments = fetchedAssignments
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End
            ) {
                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(30.dp),
                    containerColor = CC.extraColor2(),
                    content = {
                        Icon(
                            Icons.Default.Add,
                            "Add Assignment",
                            tint = CC.textColor(),
                            modifier = Modifier.size(30.dp)
                        )
                    },
                )

            }
            AnimatedVisibility(visible = expanded) {
                AddAssignmentItem(
                    courseID,
                    context,
                    expanded,
                    onExpandedChange = { expanded = !expanded })
            }
            //assignmentCard
            assignments?.let {
                LazyColumn {
                    items(it) { assignment ->
                        AssignmentCard(assignment, context)
                    }
                }
            } ?: run {
                Text("No announcements found", style = CC.descriptionTextStyle(context))
            }

        }
    }
}


@Composable
fun AssignmentCard(
    assignment: CourseAssignment, context: Context
) {
    val currentDate = Date()
    val formatter =
        DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()) // Use default locale
    val formattedDate = formatter.format(currentDate)
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = background.copy(0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = assignment.title, style = CC.titleTextStyle(context)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Published: ${assignment.publishedDate}",
                    style = CC.descriptionTextStyle(context)
                        .copy(fontSize = 12.sp, color = CC.secondary())
                )
                Text(
                    text = if (assignment.dueDate < formattedDate) "Past Due" else if (assignment.dueDate == formattedDate) "Due Today" else "Due: ${assignment.dueDate}",
                    fontSize = 12.sp,
                    color = if (assignment.dueDate < formattedDate) Color.Red else CC.textColor()
                )
            }
            Text(
                text = assignment.description, fontSize = 14.sp, color = Color.Black
            )
        }
    }
}

@Composable
fun AddAssignmentItem(
    courseID: String,
    context: Context,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
        AddTextField(
            label = "Title", value = title, onValueChange = { title = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "Description",
            value = description,
            onValueChange = { description = it },
            context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "Due date", value = dueDate, onValueChange = { dueDate = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    loading = true
                    MyDatabase.generateAssignmentID { iD ->
                        val newAssignment = CourseAssignment(
                            assignmentID = iD,
                            title = title,
                            description = description,
                            dueDate = dueDate,
                            publishedDate = CC.date
                        )
                        MyDatabase.writeCourseAssignments(courseID,
                            courseAssignment = newAssignment,
                            onResult = { success ->
                                if (success) {
                                    loading = false
                                    onExpandedChange(expanded)
                                } else {
                                    loading = false
                                    onExpandedChange(expanded)
                                    Log.e("Error", "Failed to write announcement")
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()

                                }
                            })
                    }
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                ), modifier = Modifier.width(100.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = background, modifier = Modifier.size(20.dp))
                } else {
                    Text("Post", style = CC.descriptionTextStyle(context))
                }
            }
            Button(
                onClick = {
                    onExpandedChange(expanded)
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                )
            ) {
                Text("Cancel", style = CC.descriptionTextStyle(context))
            }

        }

    }
}

@Composable
fun TimetableItem(courseID: String, navController: NavController, context: Context) {
    var loading by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var timetables by remember { mutableStateOf<List<CourseTimetable>?>(null) }

    LaunchedEffect(courseID) {
        loading = true
        MyDatabase.getCourseTimetable(courseID) { fetchedTimetables ->
            timetables = fetchedTimetables
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
            FloatingActionButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(30.dp),
                containerColor = CC.extraColor2(),
                contentColor = CC.textColor()
            ) {
                Icon(Icons.Default.Add, "add timetable")
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Every Monday", style = CC.titleTextStyle(context))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = expanded) {
                AddTimetableItem(
                    courseID,
                    context,
                    expanded,
                    onExpandedChange = { expanded = !expanded })
            }
            //timetable card
            timetables?.let {
                LazyColumn {
                    items(it) { timetable ->
                        TimetableCard(timetable, context)
                    }
                }
            } ?: run {
                Text("No timetable found", style = CC.descriptionTextStyle(context))
            }
        }
    }
}

@Composable
fun TimetableCard(
    timetable: CourseTimetable, context: Context
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = background.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = "Start time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Start: ${timetable.startTime}", style = CC.descriptionTextStyle(context)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = "End time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "End: ${timetable.endTime}", style = CC.descriptionTextStyle(context)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Start time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Venue: ${timetable.venue}", style = CC.descriptionTextStyle(context)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Start time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Lecturer: ${timetable.lecturer}",
                    style = CC.descriptionTextStyle(context)
                )
            }
        }
    }
}

@Composable
fun AddTimetableItem(
    courseID: String,
    context: Context,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth(0.9f)) {
        AddTextField(
            label = "Start", value = startTime, onValueChange = { startTime = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "End", value = endTime, onValueChange = { endTime = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "Venue", value = venue, onValueChange = { venue = it }, context
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    loading = true
                    MyDatabase.generateTimetableID { iD ->
                        val timetable = CourseTimetable(
                            timetableID = iD,
                            startTime = startTime,
                            endTime = endTime,
                            venue = venue,
                            lecturer = lecturer
                        )
                        MyDatabase.writeCourseTimetable(courseID,
                            courseTimetable = timetable,
                            onResult = { success ->
                                if (success) {
                                    loading = false
                                    onExpandedChange(expanded)
                                    startTime = ""
                                    endTime = ""
                                    venue = ""
                                    lecturer = ""
                                } else {
                                    loading = false
                                    onExpandedChange(expanded)
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                ), modifier = Modifier.width(100.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = background, modifier = Modifier.size(20.dp))
                } else {
                    Text("Post", style = CC.descriptionTextStyle(context))
                }
            }
            Button(
                onClick = {
                    onExpandedChange(expanded)
                }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                )
            ) {
                Text("Cancel", style = CC.descriptionTextStyle(context))
            }

        }

    }
}

@Composable
fun DetailsItem(courseID: String, navController: NavController, context: Context) {
    var expanded by remember { mutableStateOf(false) }
    var details by remember { mutableStateOf<CourseDetails?>(null) }
    LaunchedEffect(courseID) {
        MyDatabase.getCourseDetails(courseID) { fetchedDetails ->
            if (fetchedDetails != null) {
                details = fetchedDetails
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(0.9f), horizontalArrangement = Arrangement.End) {
            FloatingActionButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(30.dp),
                containerColor = CC.extraColor2(),
                contentColor = CC.textColor()
            ) {
                Icon(Icons.Default.Add, "add timetable")
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = expanded) {
                AddDetailsItem(
                    courseID,
                    context,
                    expanded,
                    onExpandedChange = { expanded = !expanded })
            }
            if (details != null){
            DetailsItemCard(details!!, context)
            }
            else {
                Text("No details found", style = CC.descriptionTextStyle(context))
            }
        }
    }

}


@Composable
fun DetailsItemCard(courseDetails: CourseDetails, context: Context) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = background.copy(0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = courseDetails.courseName,
                style = CC.titleTextStyle(context).copy(fontSize = 20.sp)
            )
            Text(
                text = "Course Code: ${courseDetails.courseCode}",
                style = CC.descriptionTextStyle(context)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "lecturer",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = courseDetails.lecturer,
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = "lecturer",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Visits: ${courseDetails.numberOfVisits}",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocalPostOffice,
                    contentDescription = "lecturer",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Department: ${courseDetails.courseDepartment}",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Overview",
                style = CC.descriptionTextStyle(context).copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CC.primary() )
            )
            Text(
                text = courseDetails.overview,
                style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium,
                    color = CC.textColor(), fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Learning Outcomes",
                style = CC.descriptionTextStyle(context).copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CC.primary() )
            )
            courseDetails.learningOutcomes.forEach { outcome ->
                Text(
                    text = "- $outcome",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium,
                        color = CC.textColor(), fontSize = 16.sp)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Schedule",
                style = CC.descriptionTextStyle(context).copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CC.primary() )
            )
            Text(
                text = courseDetails.schedule,
                style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium,
                    color = CC.textColor(), fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Required Materials",
                style = CC.descriptionTextStyle(context).copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = CC.primary() )
            )
            Text(
                text = courseDetails.requiredMaterials,
                style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Medium,
                    color = CC.textColor(), fontSize = 16.sp)
            )
        }
    }
}

@Composable
fun AddDetailsItem(
    courseID: String,context: Context, expanded: Boolean, onExpandedChange: (Boolean) -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var numberOfVisits by remember { mutableStateOf("") }
    var courseDepartment by remember { mutableStateOf("") }
    var courseOutcome by remember { mutableStateOf("") }
    var overview by remember { mutableStateOf("") }
    var learningOutcomes by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    var requiredMaterials by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var courseInfo by remember { mutableStateOf(Course())  }

    LaunchedEffect(courseID) {
        MyDatabase.getCourseDetailsByCourseID(courseID) { fetchedDetails ->
            if (fetchedDetails != null) {
                courseInfo = fetchedDetails
                courseName = fetchedDetails.courseName
                courseCode = fetchedDetails.courseCode
                numberOfVisits = fetchedDetails.visits.toString()
            }
            else {
                Log.e("Error", "Failed to fetch course details or empty database")
            }
        }

    }
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AddTextField(
                label = "Lecturer", value = lecturer, onValueChange = { lecturer = it }, context
            )
            AddTextField(
                label = "Course Department",
                value = courseDepartment,
                onValueChange = { courseDepartment = it },
                context
            )
            AddTextField(
                label = "Course Outcome",
                value = courseOutcome,
                onValueChange = { courseOutcome = it },
                context
            )
            Spacer(modifier = Modifier.size(8.dp))
            AddTextField(
                label = "Overview", value = overview, onValueChange = { overview = it }, context
            )
            Spacer(modifier = Modifier.size(8.dp))
            AddTextField(
                label = "Learning Outcomes",
                value = learningOutcomes,
                onValueChange = { learningOutcomes = it },
                context
            )
            Spacer(modifier = Modifier.size(8.dp))
            AddTextField(
                label = "Schedule", value = schedule, onValueChange = { schedule = it }, context
            )
            Spacer(modifier = Modifier.size(8.dp))
            AddTextField(
                label = "Required Materials",
                value = requiredMaterials,
                onValueChange = { requiredMaterials = it },
                context
            )
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                onClick = {
                    onExpandedChange(!expanded)
                    val newDetails = CourseDetails(
                        detailsID = "2024$courseID",
                        lecturer = lecturer,
                        courseDepartment = courseDepartment,
                        overview = overview,
                        learningOutcomes = learningOutcomes.split(",").map { it.trim() },
                        schedule = schedule,
                        requiredMaterials = requiredMaterials
                    )
                    MyDatabase.writeCourseDetails(courseID = courseID,
                        courseDetails = newDetails,
                        onResult = { success ->
                            if (success) {
                                loading = false
                                onExpandedChange(!expanded)
                                courseName = ""
                                courseCode = ""

                            } else {
                                loading = false
                                onExpandedChange(!expanded)
                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                    )
                }, modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = if (expanded) "Save" else "Edit")
            }
        }
    }
}

@Preview
@Composable
fun CourseContentPreview() {
    val courseDetails = CourseDetails(
        courseName = CourseName.name.value,
        courseCode = "JC101",
        lecturer = "Dr. John Doe",
        numberOfVisits = "25",
        courseDepartment = "Computer Science",
        overview = "This course provides a comprehensive introduction to web development, covering HTML, CSS, and JavaScript. Students will learn how to build interactive websites and web applications.",
        learningOutcomes = listOf(
            "Write JavaScript code to add interactivity to websites.",
            "Understand the principles of web development and best practices.",
            "Design and implement web pages using HTML and CSS."
        ),
        schedule = "Mondays and Wednesdays, 10:00 AM - 11:30 AM, Room 101",
        requiredMaterials = "\"Web Development for Beginners\" textbook"
    )

    DetailsItemCard(
        courseDetails = courseDetails,
        context = LocalContext.current
    )
}
