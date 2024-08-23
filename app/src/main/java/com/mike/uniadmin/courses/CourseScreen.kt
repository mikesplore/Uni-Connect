package com.mike.uniadmin.courses

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.R
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.courses.CourseEntity
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.getCourseViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(context: Context, navController: NavController) {
    val courseViewModel = getCourseViewModel(context)
    val userViewModel = getUserViewModel(context)

    val currentUser by userViewModel.user.observeAsState()
    val courses by courseViewModel.courses.observeAsState(emptyList())
    val isLoading by courseViewModel.isLoading.observeAsState(false)
    var showAddCourse by remember { mutableStateOf(false) }
    val userTypes = UniAdminPreferences.userType.value

    LaunchedEffect(Unit) {
      //  uploadCoursesData()
        userViewModel.findUserByEmail(FirebaseAuth.getInstance().currentUser?.email ?: "") {}
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
                if (userTypes == "admin"){
                IconButton(onClick = { showAddCourse = !showAddCourse }) {
                    Icon(
                        if (showAddCourse) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add Course",
                        tint = CC.textColor()
                    )
                }}
                IconButton(onClick = {courseViewModel.fetchCourses()}) {
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
                ){
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
                                            UniAdminPreferences.saveCourseCode(course.courseCode)
                                            if (UniAdminPreferences.courseCode.value.isNotEmpty()) {
                                                navController.navigate("homeScreen")
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
                                UniAdminPreferences.saveCourseCode(course.courseCode)
                                if (UniAdminPreferences.courseCode.value.isNotEmpty()) {
                                    navController.navigate("homeScreen")
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
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp)) // Smoothen corners for the entire card
            .border(
                1.dp, CC.secondary(), RoundedCornerShape(16.dp)
            ) // Add a border with rounded corners
            .background(CC.primary()) // Set a background color
            .padding(10.dp) // Inner padding for content
            .fillMaxWidth(0.85f)
            .wrapContentHeight()
    ) {
        // Module Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp)) // Clip the image with rounded corners
        ) {
            AsyncImage(
                model = courseEntity?.courseImageLink,
                contentScale = ContentScale.Crop,
                contentDescription = "Module Image",
                placeholder = painterResource(R.drawable.logo),
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp)) // Add spacing between image and text

        // Module Title and Details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            courseEntity?.courseName?.let {
                Text(
                    it, style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center
                    ), modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // Space between title and details

            Text(
                "Participants: ${courseEntity?.participants?.size}",
                style = CC.descriptionTextStyle(context).copy(
                    fontWeight = FontWeight.Medium, fontSize = 16.sp, textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // Space before button

        // Open Course Button
        Button(
            onClick = {
                onCourseClicked()

            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)), // Rounded button corners
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = randomColor.random(), contentColor = CC.textColor()
            ),
            // enabled = courseEntity?.participants?.contains(currentUser?.id) == false
        ) {
            if (courseEntity?.participants?.contains(currentUser?.id) == true) {
                Text(
                    "Open Course", style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                )
            } else {
                Text(
                    "Join Course", style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                )
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
