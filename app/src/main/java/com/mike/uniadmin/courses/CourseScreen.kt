package com.mike.uniadmin.courses

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.CourseManager
import com.mike.uniadmin.R
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.courses.CourseEntity
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.getAnnouncementViewModel
import com.mike.uniadmin.getCourseViewModel
import com.mike.uniadmin.getModuleTimetableViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(context: Context, navController: NavController) {
    val courseViewModel = getCourseViewModel(context)
    val userViewModel = getUserViewModel(context)
    val timetableViewModel = getModuleTimetableViewModel(context)
    val moduleViewModel = getModuleViewModel(context)
    val announcementViewModel = getAnnouncementViewModel(context)


    val currentUser by userViewModel.user.observeAsState()
    val courses by courseViewModel.courses.observeAsState(emptyList())
    val isLoading by courseViewModel.isLoading.observeAsState(false)
    var showAddCourse by remember { mutableStateOf(false) }
    val userTypes = UniAdminPreferences.userType.value

    LaunchedEffect(Unit) {
        userViewModel.findUserByEmail(FirebaseAuth.getInstance().currentUser?.email ?: "") {}
    }

    fun startListeners() {
        Toast.makeText(context, "Starting listeners", Toast.LENGTH_SHORT).show()
        announcementViewModel.startAnnouncementsListener()
        moduleViewModel.fetchModulesFromFirebase()
        timetableViewModel.getAllModuleTimetables()
        Log.d("UniAdminPreferences","Course Code in this screen: ${CourseManager.courseCode.value}")
    }


    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Courses", style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.Bold, fontSize = 24.sp
                    )
                )
            }, actions = {
                if (userTypes == "admin") {
                    IconButton(onClick = { showAddCourse = !showAddCourse }) {
                        Icon(
                            if (showAddCourse) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Add Course",
                            tint = CC.textColor()
                        )
                    }
                }
                IconButton(onClick = { courseViewModel.fetchCourses() }) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Add Course",
                        tint = CC.textColor()
                    )
                }
            },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(), titleContentColor = CC.textColor()
                )
            )
        }, containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = showAddCourse) {
                AddCourse(context = context, onCourseAdded = { newCourse ->
                    courseViewModel.saveCourse(newCourse) { success ->
                        showAddCourse = false
                        if (success) {
                            Toast.makeText(
                                context, "Course added successfully", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(context, "Failed to add course", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                })
            }
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CC.ColorProgressIndicator()
                }
            }
            if (courses?.isEmpty() == true) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No courses available", style = CC.titleTextStyle(context).copy(
                            fontWeight = FontWeight.Bold, fontSize = 24.sp
                        )
                    )
                }
            } else {
                LazyColumn {
                    items(courses ?: emptyList()) { course ->
                        CourseItem(
                            currentUser, course, context
                        ) {
                            startListeners()
                            if (!course.participants.contains(currentUser?.id)) {
                                currentUser?.id?.let { userId ->
                                    courseViewModel.saveCourse(
                                        course.copy(participants = course.participants + userId)
                                    ) { onSuccess ->
                                        if (onSuccess) {
                                            Toast.makeText(
                                                context,
                                                "Course joined successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            //get the course code
                                            CourseManager.updateCourseCode(course.courseCode)
                                            if (CourseManager.courseCode.value.isNotEmpty()) {
                                                userViewModel.fetchUsers()

                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "course code is empty",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Failed to join course",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                //get the course code
                                CourseManager.updateCourseCode(course.courseCode)
                                if (CourseManager.courseCode.value.isNotEmpty()) {
                                    userViewModel.fetchUsers()
                                } else {
                                    Toast.makeText(
                                        context, "course code is empty", Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CourseItem(
    currentUser: UserEntity?,
    courseEntity: CourseEntity?,
    context: Context,
    onCourseClicked: () -> Unit = {}
) {


    val buttonText = if (courseEntity?.participants?.contains(currentUser?.id) == true) {
        "Open Course"
    } else {
        "Join Course"
    }
    BoxWithConstraints {
        val columnWidth = maxWidth
        val height = columnWidth * 0.65f
        val density = LocalDensity.current
        val textSize = with(density) { (columnWidth * 0.045f).toSp() }

        val titleStyle = CC.titleTextStyle(context).copy(
            fontWeight = FontWeight.Bold,
            fontSize = textSize,
            textAlign = TextAlign.Center
        )

        val buttonStyle = CC.titleTextStyle(context).copy(
            fontWeight = FontWeight.Bold,
            fontSize = textSize * 0.8f,
        )

        Box( // Use Box to overlay components
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CC.secondary(), RoundedCornerShape(16.dp))
                .background(CC.primary())
                .padding(10.dp)
                .width(columnWidth * 0.85f)
                .height(height)
        ) {
            // Add the background as the first layer
            CourseBackground()

            // Overlay the content on top of the background
            Column(
                modifier = Modifier
                    .padding(8.dp) // Add padding so content is not flush against the edges
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = courseEntity?.courseImageLink,
                        contentScale = ContentScale.Crop,
                        contentDescription = "Module Image",
                        placeholder = painterResource(R.drawable.newcourse),
                        error = painterResource(R.drawable.newcourse),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier

                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    courseEntity?.courseName?.let {
                        Text(
                            it.uppercase(),
                            style = titleStyle,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onCourseClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = randomColor.random(),
                        contentColor = CC.textColor()
                    ),
                ) {
                    Text(buttonText, style = buttonStyle)
                }
            }
        }
    }
}


@Composable
fun CourseBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        val rows = 10 // Number of rows
        val columns = 10 // Number of columns

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(columns) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = randomColor.random().copy(alpha = 0.3f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AddCourse(
    context: Context, onCourseAdded: (CourseEntity) -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var courseImageLink by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }



    Column(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CC.secondary(), RoundedCornerShape(12.dp))
            .background(CC.primary()) // Optional: Set a background color
            .padding(16.dp) // Inner padding
            .fillMaxWidth(0.95f)
            .wrapContentHeight()
    ) {
        Text(
            text = "Add Course", style = CC.titleTextStyle(context).copy(
                fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center
            ), modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        CC.SingleLinedTextField(
            value = courseName,
            onValueChange = { newText -> courseName = newText },
            label = "Course Name",
            context = context,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CC.secondary(), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        )

        CC.SingleLinedTextField(
            value = courseImageLink,
            onValueChange = { newText -> courseImageLink = newText },
            label = "Course Image Link",
            context = context,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CC.secondary(), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        )

        Button(
            onClick = {

                loading = true
                if (courseName.isEmpty() || courseImageLink.isEmpty()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    loading = false
                    return@Button
                }
                MyDatabase.generateCourseID { newID ->
                    val newCourse = CourseEntity(
                        participants = emptyList(),
                        courseCode = newID,
                        courseName = courseName,
                        courseImageLink = courseImageLink
                    )
                    onCourseAdded(newCourse)
                    loading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp), // Add padding around the button
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.secondary(), contentColor = CC.textColor()
            )
        ) {
            if (loading) {
                CC.ColorProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                Text(
                    text = "Add Course", style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                )
            }
        }
    }
}


