package com.mike.uniadmin


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Announcement
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.model.Announcement
import com.mike.uniadmin.model.Course
import com.mike.uniadmin.model.Details
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.fetchUserDataByEmail
import com.mike.uniadmin.model.MyDatabase.getAnnouncements
import com.mike.uniadmin.model.Timetable
import com.mike.uniadmin.model.User
import com.mike.uniadmin.notification.showNotification
import com.mike.uniadmin.ui.theme.GlobalColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import com.mike.uniadmin.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(navController: NavController, context: Context) {
    var visible by remember { mutableStateOf(true) }
    var user by remember { mutableStateOf(User())}
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var currentName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }), // Slide in from right
        exit = slideOutHorizontally(targetOffsetX = { -it }) // Slide out to left
    ) {
        fun getGreetingMessage(): String {
            val currentTime = LocalTime.now()
            return when (currentTime.hour) {
                in 5..11 -> "Good Morning"
                in 12..17 -> "Good Afternoon"
                in 18..21 -> "Good Evening"
                else -> "Good Night"
            }
        }

        var expanded by remember { mutableStateOf(false) }
        val horizontalScrollState = rememberScrollState()

        @Composable
        fun FirstBox() {

        }

        @Composable
        fun SecondBox() {


        }

        @Composable
        fun ThirdBox() {

        }

        val totalDuration = 10000
        val delayDuration = 5000L
        val boxCount = 3
        val boxScrollDuration = (totalDuration / boxCount)

        LaunchedEffect(Unit) {
            while (true) {
                for (i in 0 until boxCount) {
                    val targetScrollPosition = i * (horizontalScrollState.maxValue / (boxCount - 1))
                    horizontalScrollState.animateScrollTo(
                        targetScrollPosition, animationSpec = tween(
                            durationMillis = boxScrollDuration, easing = EaseInOut
                        )
                    )
                    delay(delayDuration)
                }
                horizontalScrollState.scrollTo(0)
            }
        }
        LaunchedEffect(key1 = Unit) { // Use a stable key
            while (true) {
                delay(1000L) // Delay for 10 seconds
                auth.currentUser?.email?.let { email ->
                    fetchUserDataByEmail(email) { fetchedUser ->
                        fetchedUser?.let {
                            user = it
                            currentName = it.firstName
                            // You might want to add a mechanism to notify the UI about the updated data
                        }
                    }
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        "${getGreetingMessage()}, $currentName",
                        style = CC.descriptionTextStyle(context),
                        fontSize = 20.sp
                    )
                }, actions = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = CC.textColor()
                        )
                    }
                    DropdownMenu(
                        expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(CC.primary())
                    ) {
                        DropdownMenuItem(text = {
                            Row {
                                Icon(
                                    Icons.Default.ManageAccounts,
                                    contentDescription = "",
                                    tint = CC.textColor()
                                )
                                Text(" Users", style = CC.descriptionTextStyle(context))
                            }
                        }, onClick = {
                            navController.navigate("users")
                            expanded = false
                        })
                        DropdownMenuItem(text = {
                            Row {
                                Icon(
                                    Icons.Default.AssignmentInd,
                                    contentDescription = "",
                                    tint = CC.textColor()
                                )
                                Text(
                                    " Assignments", style = CC.descriptionTextStyle(context)
                                )
                            }
                        }, onClick = {
                            navController.navigate("assignments")
                            expanded = false
                        })

                        DropdownMenuItem(text = {
                            Row {
                                Icon(
                                    Icons.Default.PendingActions,
                                    contentDescription = "",
                                    tint = CC.textColor()
                                )
                                Text(
                                    " Attendance", style = CC.descriptionTextStyle(context)
                                )
                            }
                        }, onClick = {
                            navController.navigate("attendance")
                            expanded = false
                        })

                        DropdownMenuItem(text = {
                            Row {
                                Icon(
                                    Icons.AutoMirrored.Filled.Announcement,
                                    contentDescription = "",
                                    tint = CC.textColor()
                                )
                                Text(
                                    " Announcements",
                                    style = CC.descriptionTextStyle(context)
                                )
                            }
                        }, onClick = {
                            navController.navigate("announcements")
                            expanded = false
                        })
                        DropdownMenuItem(text = {
                            Row {
                                Icon(
                                    Icons.Filled.Book,
                                    contentDescription = "",
                                    tint = CC.textColor()
                                )
                                Text(
                                    " Courses",
                                    style = CC.descriptionTextStyle(context)
                                )
                            }
                        }, onClick = {
                            navController.navigate("courses")
                            expanded = false
                        })
                        DropdownMenuItem(text = {
                            Row {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "",
                                    tint = CC.textColor()
                                )
                                Text(" Timetable", style = CC.descriptionTextStyle(context))
                            }
                        }, onClick = {
                            navController.navigate("timetable")
                            expanded = false
                        })
                        DropdownMenuItem(text = {
                            Row {
                                Icon(
                                    Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "",
                                    tint = CC.textColor()
                                )
                                Text(" Discussion", style = CC.descriptionTextStyle(context))
                            }
                        }, onClick = {
                            navController.navigate("discussion")
                            expanded = false
                        })

                        DropdownMenuItem(text = {
                            Row {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "",
                                    tint = CC.textColor()
                                )
                                Text(
                                    " Settings", style = CC.descriptionTextStyle(context)
                                )
                            }
                        }, onClick = {
                            navController.navigate("settings")
                            expanded = false
                        })
                        DropdownMenuItem(text = {
                            Row {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "",
                                    tint = CC.textColor()
                                )
                                Text(" Logout", style = CC.descriptionTextStyle(context))
                            }
                        }, onClick = {
                            auth.signOut()
                            navController.navigate("login")
                            expanded = false
                        })
                    }
                }, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(),
                    titleContentColor = CC.textColor()
                )
                )
            },
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
                        .padding(start = 10.dp, end = 10.dp)
                        .requiredHeight(200.dp)
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(modifier = Modifier.width(10.dp))
                    SecondBox()
                    Spacer(modifier = Modifier.width(10.dp))
                    FirstBox()
                    Spacer(modifier = Modifier.width(10.dp))
                    ThirdBox()
                }
                //the tabs column starts here
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    var selectedTabIndex by remember { mutableIntStateOf(0) }
                    val configuration = LocalConfiguration.current
                    val screenWidth = configuration.screenWidthDp.dp
                    val tabRowHorizontalScrollState by remember { mutableStateOf(ScrollState(0)) }
                    val tabTitles = listOf(
                        "Announcements",
                        "Attendance",
                        "Timetable",
                        "Assignments",
                        "Manage Users",
                        "Documentation",

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
                    val coroutineScope = rememberCoroutineScope()

                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        modifier = Modifier.background(CC.secondary()),
                        contentColor = CC.primary(),
                        indicator = indicator,
                        edgePadding = 0.dp,

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
                                            if (selectedTabIndex == index) CC.primary() else CC.secondary(),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp), contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        style = CC.descriptionTextStyle(context),
                                        color = if (selectedTabIndex == index) CC.textColor() else CC.tertiary(),
                                    )
                                }
                            }, modifier = Modifier.background(CC.primary())
                            )
                        }
                    }

                    when (selectedTabIndex) {
                        0 -> AnnouncementItem(currentName, context)
                        1 -> ManageAttendanceScreen(navController, context)
                        2 -> TimetableItem(context)
                        3 -> AssignmentsItem(context)
                        4 -> ManageUsersItem(context)
                        5 -> DocumentationItem()
                        else -> {}
                    }
                }
            }
        }
    }
}



@Composable
fun AnnouncementItem(sender: String, context: Context) {
    var title by remember { mutableStateOf("") }
    val date = "Today"
    var description by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    val announcements = remember { mutableStateListOf<Announcement>() }

    LaunchedEffect(Unit) {
        getAnnouncements { fetchedAnnouncements ->
            announcements.addAll(fetchedAnnouncements ?: emptyList())
            loading = false
        }
    }

    Spacer(modifier = Modifier.height(10.dp))


    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Latest Announcement",
            style = CC.descriptionTextStyle(context = context),
            fontWeight = FontWeight.Bold
        )
        Column(
            modifier = Modifier
                .background(Color.Transparent, RoundedCornerShape(10.dp))
                .padding(10.dp)
                .fillMaxWidth(0.9f)
                .height(200.dp)
                .border(
                    width = 1.dp, color = CC.textColor(), shape = RoundedCornerShape(10.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (loading) {
                Column(
                    modifier = Modifier
                        .background(Color.Transparent, RoundedCornerShape(10.dp))
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = CC.secondary(), trackColor = CC.textColor()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Fetching data...", style = CC.descriptionTextStyle(context))
                }

            } else if (announcements.isNotEmpty()) {
                val firstAnnouncement = announcements[announcements.lastIndex]
                Box(modifier = Modifier.background(Color.Transparent, RoundedCornerShape(10.dp))) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "", // Provide a meaningful content description
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop // Fill the Box while maintaining aspect ratio
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                firstAnnouncement.title,
                                style = CC.titleTextStyle(context),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        // Content column with vertical scrolling
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                                .fillMaxHeight(1f)
                                .background(
                                    CC.primary().copy(alpha = 0.5f),
                                    RoundedCornerShape(10.dp)
                                )  // Adding a background color for better contrast
                        ) {
                            // Author and date row
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween  // Space items evenly across the row
                            ) {
                                Text(
                                    firstAnnouncement.author,
                                    style = CC.descriptionTextStyle(context),
                                    // Adding color for better visual separation
                                )
                                Text(
                                    firstAnnouncement.date,
                                    style = CC.descriptionTextStyle(context),
                                )
                            }

                            // Description column
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.Start  // Align text to the start (left)
                            ) {
                                Text(
                                    firstAnnouncement.description,
                                    style = CC.descriptionTextStyle(context),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.padding(8.dp)  // Adding padding around the text
                                )
                            }
                        }
                    }
                }

            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No announcements available", style = CC.descriptionTextStyle(context)
                    ) // Handle the case of an empty list
                }

            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Make Quick Announcement",
            style = CC.descriptionTextStyle(context),
            fontWeight = FontWeight.Bold
        )



        Box(modifier = Modifier.fillMaxHeight(1f)) {

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(10.dp)
                    .border(
                        width = 1.dp,
                        color = CC.textColor(),
                        shape = RoundedCornerShape(10.dp)
                    ), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Image now inside the Column
                Spacer(modifier = Modifier.height(10.dp))
                QuickInput(value = title, label = "Title", singleLine = true, onValueChange = {
                    title = it
                })
                Spacer(modifier = Modifier.height(10.dp))
                QuickInput(
                    value = description,
                    label = "Description",
                    singleLine = false,
                    onValueChange = { description = it },
                )
                Row(
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            loading = true
                            if (title.isEmpty() && description.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "Please enter a title and description",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loading = false
                            } else {
                                MyDatabase.generateAnnouncementID { announcementID ->
                                    val newAnnouncement = Announcement(
                                        id = announcementID,
                                        author = sender,
                                        date = date,
                                        title = title,
                                        description = description,

                                        )
                                    Log.d("Announcement:", "announcemt details: $newAnnouncement")
                                    MyDatabase.writeAnnouncement(newAnnouncement)
                                    showNotification(
                                        context, title = title, message = description
                                    )
                                    title = ""
                                    description = ""

                                    Toast.makeText(context, "Announcement posted", Toast.LENGTH_SHORT)
                                        .show()
                                    getAnnouncements { fetchedAnnouncements ->
                                        announcements.clear()
                                        announcements.addAll(fetchedAnnouncements ?: emptyList())
                                    }}
                                loading = false
                            }
                        }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(
                            containerColor = CC.primary(),
                            contentColor = CC.textColor()
                        )
                    ) {
                        Text("Post", style = CC.descriptionTextStyle(context))
                    }
                }
            }

        }
    }
}

@Composable
fun QuickInput(
    value: String, label: String, singleLine: Boolean, onValueChange: (String) -> Unit

) {
    TextField(
        value = value,
        textStyle = CC.descriptionTextStyle(context = LocalContext.current),
        onValueChange = onValueChange,
        label = { Text(label, style = CC.descriptionTextStyle(context = LocalContext.current)) },
        modifier = Modifier
            .padding(10.dp)
            .width(250.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CC.primary(),
            unfocusedLabelColor = CC.tertiary(),
            focusedIndicatorColor = CC.textColor(),
            unfocusedContainerColor = Color.Transparent,
            unfocusedTextColor = CC.textColor(),
            focusedTextColor = CC.textColor(),
            focusedLabelColor = CC.primary(),
            unfocusedIndicatorColor = CC.textColor()
        ),
        singleLine = singleLine
    )


}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableItem(context: Context) {
    var loading by remember { mutableStateOf(true) }
    val currentDay = CC.currentDay()
    var timetable by remember { mutableStateOf<List<Timetable>?>(null) }

    LaunchedEffect(loading) {

        if (loading) {
            MyDatabase.getCurrentDayTimetable(currentDay) { fetchedTimetable ->
                timetable = fetchedTimetable
                loading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { loading = true } // for triggering refresh
                .height(50.dp)
                .padding(top = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Events Today : $currentDay", style = CC.titleTextStyle(context))
        }

        if (loading) {
            // Show a loading indicator while fetching data
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = CC.textColor(),
                    trackColor = CC.primary(),
                    modifier = Modifier
                        .padding(16.dp)
                )
                Text(
                    "Loading activity for $currentDay",
                    style = CC.descriptionTextStyle(context)
                )
            }
        } else {
            if (timetable.isNullOrEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("No events planned for today", style = CC.descriptionTextStyle(context))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .height(300.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(timetable ?: emptyList()) { timetableItem ->
                        // Display each timetable item here
                        Card(
                            colors = CardDefaults.cardColors(
                                CC.secondary()
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = timetableItem.unitName, // Assuming you have a unitName property
                                    style = CC.titleTextStyle(context)
                                )
                                Text(
                                    text = "Time: ${timetableItem.startTime} - ${timetableItem.endTime}", // Assuming startTime and endTime properties
                                    style = CC.descriptionTextStyle(context)
                                )
                                Text(
                                    text = "Location: ${timetableItem.venue}", // Assuming venue property
                                    style = CC.descriptionTextStyle(context)
                                )
                            }
                        }
                    }
                }
            }
            var dayId by remember { mutableStateOf("") }
            var venue by remember { mutableStateOf("") }
            var lecturer by remember { mutableStateOf("") }
            var startTime by remember { mutableStateOf("") }
            var endTime by remember { mutableStateOf("") }
            var unitName by remember { mutableStateOf("") }

            LaunchedEffect(currentDay) {
                MyDatabase.getDayIdByName(currentDay) { fetchedDayId ->
                    if (fetchedDayId != null) {
                        dayId = fetchedDayId
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Make quick Event", style = CC.titleTextStyle(context), fontSize = 18.sp)
                Button(
                    onClick = {
                        if (lecturer.isNotEmpty() && venue.isNotEmpty()
                            && startTime.isNotEmpty() && endTime.isNotEmpty() && unitName.isNotEmpty()
                        ) {
                            MyDatabase.generateTimetableID { timetableID ->

                                val newtimetable = Timetable(
                                    id = timetableID,
                                    dayId = dayId,
                                    unitName = unitName,
                                    lecturer = lecturer,
                                    venue = venue,
                                    startTime = startTime,
                                    endTime = endTime
                                )
                                MyDatabase.writeTimetable(newtimetable, onComplete = {
                                    Toast.makeText(
                                        context, "Timetable item Added", Toast.LENGTH_SHORT

                                    ).show()
                                    showNotification(
                                        context,
                                        title = "New Timetable Item",
                                        message = "${Details.firstName.value} added an Event."
                                    )
                                })}
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CC.secondary()
                    )
                ) {
                    Text("Post", style = CC.descriptionTextStyle(context))
                }
            }

            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = CC.textColor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MyOutlinedTextField(
                    value = unitName,
                    onValueChange = { unitName = it },
                    label = "Unit Name",
                    context
                )
                MyOutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = "Start time",
                    context
                )
                MyOutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = "End time",
                    context
                )
                MyOutlinedTextField(
                    value = venue,
                    onValueChange = { venue = it },
                    label = "Venue",
                    context
                )
                MyOutlinedTextField(
                    value = lecturer,
                    onValueChange = { lecturer = it },
                    label = "Lecturer Name",
                    context
                )
            }
        }
    }
}



@Composable
fun AssignmentsItem(context: Context) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var loading by remember { mutableStateOf(true) }
    val courses = remember { mutableStateListOf<Course>() }
    LaunchedEffect(Unit) {
        MyDatabase.fetchCourses { fetchedCourses ->
            courses.clear()
            courses.addAll(fetchedCourses ?: emptyList())
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) { Text("Available Assignments", style = CC.titleTextStyle(context))
        }
        val indicator = @Composable { tabPositions: List<TabPosition> ->
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedTabIndex])
                    .height(4.dp)
                    .width(screenWidth / (courses.size.coerceAtLeast(1))) // Avoid division by zero
                    .background(CC.secondary(), CircleShape)
            )
        }
        val coroutineScope = rememberCoroutineScope()

        if (loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = CC.secondary(),
                    trackColor = CC.textColor()
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Loading Units", style = CC.descriptionTextStyle(context))

            }

        } else {
            if(courses.isEmpty()){
                Row (modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically){
                    Text("No courses available", style = CC.descriptionTextStyle(context))}
            } else{

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.background(Color.LightGray),
                    contentColor = Color.Black,
                    indicator = indicator,
                    edgePadding = 0.dp,
                    containerColor = CC.primary()
                ) {
                    courses.forEachIndexed { index, subject ->

                        Tab(selected = selectedTabIndex == index, onClick = {
                            selectedTabIndex = index
                            coroutineScope.launch {
                                // Load assignments for the selected subject
                            }
                        }, text = {

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedTabIndex == index) CC.secondary() else CC.primary(),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = subject.courseName,
                                    color = if (selectedTabIndex == index) CC.textColor() else CC.tertiary(),
                                )
                            }
                        }, modifier = Modifier.background(CC.primary())
                        )
                    }
                }}

            when (selectedTabIndex) {
                in courses.indices -> {
                    AssignmentsList(subjectId = courses[selectedTabIndex].courseCode, context)
                }
            }
        }

    }
}

@Composable
fun DocumentationItem() {
    Column(modifier = Modifier
        .background(CC.primary())
        .fillMaxSize()) {
        WebViewScreen("https://github.com/mikesplore/My-Class")
    }

}

@Composable
fun ManageUsersItem(context: Context) {
    var users by remember { mutableStateOf<List<User>?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        MyDatabase.getUsers { fetchedUsers ->
            users = fetchedUsers
            loading = false
        }
    }

    if (loading) {
        // Show a loading indicator
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = CC.textColor(),
                trackColor = CC.primary(),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading users...", style = CC.descriptionTextStyle(context))
        }
    } else {
        users?.let { userList ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(userList) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(CC.secondary()),
                        elevation = CardDefaults.elevatedCardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = user.firstName +" "+ user.lastName,
                                style = CC.titleTextStyle(context)
                            )
                            Text(
                                text = user.email,
                                style = CC.descriptionTextStyle(context)
                            )
                        }
                    }
                }
            }
        } ?: run {
            // Handle the case where users is null (e.g., no users found)
            Text("No users found.", style = CC.descriptionTextStyle(context))
        }
    }
}



@Composable
fun MyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    TextField(
        label = { Text(label, style = CC.descriptionTextStyle(context)) },
        singleLine = true,
        value = value,
        onValueChange = onValueChange,
        textStyle = CC.descriptionTextStyle(context),
        modifier = modifier
            .padding(top = 10.dp)
            .fillMaxWidth(0.8f),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = CC.textColor(),
            unfocusedLabelColor = CC.secondary(),
            focusedLabelColor = CC.textColor(),
            unfocusedTextColor = CC.secondary(),
            unfocusedIndicatorColor = CC.secondary()
        )
    )
}

@Composable
fun Background(context: Context) {
    val icons = listOf(
        Icons.Outlined.Home,
        Icons.AutoMirrored.Outlined.Assignment,
        Icons.Outlined.School,
        Icons.Outlined.AccountCircle,
        Icons.Outlined.BorderColor,
        Icons.Outlined.Book,
    )
    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)

    }
    // Calculate the number of repetitions needed to fill the screen
    val repetitions = 1000 // Adjust this value as needed
    val repeatedIcons = mutableListOf<ImageVector>()
    repeat(repetitions) {
        repeatedIcons.addAll(icons.shuffled())
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        modifier = Modifier
            .fillMaxSize()
            .background(CC.primary())
            .padding(10.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(repeatedIcons) { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CC.secondary().copy(0.5f),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


@Preview
@Composable
fun DashboardPreview() {
    Dashboard(navController = rememberNavController(), LocalContext.current)
}
