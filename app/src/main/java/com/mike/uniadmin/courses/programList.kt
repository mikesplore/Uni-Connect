package com.mike.uniadmin.courses

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
import androidx.compose.runtime.MutableState
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
import com.google.firebase.database.FirebaseDatabase
import com.mike.uniadmin.R
import com.mike.uniadmin.backEnd.modules.CourseEntity
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.getCourseViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.helperFunctions.randomColor
import com.mike.uniadmin.ui.theme.CommonComponents as CC

object CourseCode {
    // Define the shared preferences key for storing the value
    private const val PREF_KEY_PROGRAM_CODE = "course_code_key"
    private lateinit var preferences: SharedPreferences

    // MutableState to hold the course code value
    val courseCode: MutableState<String> = mutableStateOf("")

    // Function to initialize shared preferences and load the initial value
    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // Load the value from SharedPreferences when initializing
        courseCode.value = preferences.getString(PREF_KEY_PROGRAM_CODE, "") ?: ""
        Log.d("CourseCode", "Course code initialized: ${courseCode.value}")
    }

    // Save the value to SharedPreferences whenever it changes
    fun saveCourseCode(newCourseCode: String) {
        courseCode.value = newCourseCode
        preferences.edit().putString(PREF_KEY_PROGRAM_CODE, newCourseCode).apply()
        Log.d("CourseCode", "Course code saved: $newCourseCode")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(context: Context, navController: NavController) {
    val courseViewModel = getCourseViewModel(context)
    val userViewModel = getUserViewModel(context)


    val currentUser by userViewModel.user.observeAsState()
    val courses by courseViewModel.courses.observeAsState(emptyList())
    val isLoading by courseViewModel.isLoading.observeAsState(false)
    var showAddCourse by remember { mutableStateOf(false) }

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
                IconButton(onClick = { showAddCourse = !showAddCourse }) {
                    Icon(
                        if (showAddCourse) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add Course",
                        tint = CC.textColor()
                    )
                }
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
                .border(1.dp, CC.secondary(), RoundedCornerShape(16.dp))
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
                                            CourseCode.saveCourseCode(course.courseCode)
                                            if (CourseCode.courseCode.value.isNotEmpty()) {
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
                                CourseCode.saveCourseCode(course.courseCode)
                                if (CourseCode.courseCode.value.isNotEmpty()) {
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

fun renameNode(){
    val database = FirebaseDatabase.getInstance().reference
    val oldNodeRef = database.child("PR22024")
    val newNodeRef = database.child("CR12024")

    oldNodeRef.get().addOnSuccessListener { dataSnapshot ->
        if (dataSnapshot.exists()) {
            newNodeRef.setValue(dataSnapshot.value)
                .addOnSuccessListener {
                    oldNodeRef.removeValue()
                }
                .addOnFailureListener { e ->
                    // Handle failure to write to the new node
                }
        } else {
            // Handle case where old node doesn't exist
        }
    }
        .addOnFailureListener { e ->
            // Handle failure to read from the old node
        }
}



fun uploadCoursesData() {
    val database = FirebaseDatabase.getInstance()
    val coursesRef = database.getReference("CR12024").child("Modules")

    // Data to be added
    val coursesData = mapOf(
        "CCI 4301" to mapOf(
            "courseCode" to "CCI 4301",
            "courseImageLink" to "https://bs-uploads.toptal.io/blackfish-uploads/components/seo/5923698/og_image/optimized/0712-Bad_Practices_in_Database_Design_-_Are_You_Making_These_Mistakes_Dan_Social-754bc73011e057dc76e55a44a954e0c3.png",
            "courseName" to "Advanced Database Management Systems",
            "visits" to 45
        ),
        "CCS 4301" to mapOf(
            "courseCode" to "CCS 4301",
            "courseImageLink" to "https://t3.ftcdn.net/jpg/06/69/40/52/360_F_669405248_bH5WPZiAFElWP06vqlPvj2qWcShUR4o8.jpg",
            "courseName" to "Computer Architecture and Organization",
            "visits" to 16
        ),
        "CCS 4302" to mapOf(
            "courseCode" to "CCS 4302",
            "courseImageLink" to "https://incubator.ucf.edu/wp-content/uploads/2023/07/artificial-intelligence-new-technology-science-futuristic-abstract-human-brain-ai-technology-cpu-central-processor-unit-chipset-big-data-machine-learning-cyber-mind-domination-generative-ai-scaled-1-1500x1000.jpg",
            "courseName" to "Principles of Artificial Intelligence",
            "visits" to 8
        ),
        "CCS 4304" to mapOf(
            "courseCode" to "CCS 4304",
            "courseImageLink" to "https://cdn.analyticsvidhya.com/wp-content/uploads/2023/05/human-computer-interaction.webp",
            "courseName" to "Human Computer Interaction",
            "visits" to 6
        ),
        "CCS 4305" to mapOf(
            "courseCode" to "CCS 4305",
            "courseImageLink" to "https://static.javatpoint.com/definition/images/computer-graphics-definition.png",
            "courseName" to "Computer Graphics",
            "visits" to 29
        ),
        "CIT 4307" to mapOf(
            "courseCode" to "CIT 4307",
            "courseImageLink" to "https://www.daltco.com/sites/default/files/img/product-category/data-communication-products.jpg",
            "courseName" to "Data Communication",
            "visits" to 7
        ),
        "CSE 4301" to mapOf(
            "courseCode" to "CSE 4301",
            "courseImageLink" to "https://raygun.com/blog/images/oop-concepts-java/feature.png",
            "courseName" to "Object Oriented Application Development",
            "visits" to 12
        )
    )

    // Upload the data to the "Courses" node
    coursesRef.setValue(coursesData).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            println("Courses data uploaded successfully.")
        } else {
            println("Failed to upload courses data: ${task.exception?.message}")
        }
    }
}

