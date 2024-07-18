package com.mike.uniadmin

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.Course
import com.mike.uniadmin.ui.theme.GlobalColors
import com.mike.uniadmin.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(navController: NavController, context: Context) {
    val courses = remember { mutableStateListOf<Course>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(loading) {
        GlobalColors.loadColorScheme(context)
        MyDatabase.fetchCourses { fetchedCourses ->
            courses.clear()
            courses.addAll(fetchedCourses)
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(
                            Icons.Default.ArrowBackIosNew, "Back", tint = CC.textColor()
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { loading = true }) {
                        Icon(
                            Icons.Default.Refresh, "refresh", tint = CC.textColor()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(),
                    titleContentColor = CC.textColor()
                )
            )
        },
        containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(CC.primary())
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Text(
                    "Courses",
                    style = CC.titleTextStyle(context),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Flip a course to view its details", style = CC.descriptionTextStyle(context)
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (loading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = CC.secondary(), trackColor = CC.textColor()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading...", style = CC.descriptionTextStyle(context)
                    )
                }
            } else {
                courses.forEach { course ->
                    CourseCard(course = course, context = context, navController)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun CoursesScreenPreview() {
    CoursesScreen(rememberNavController(), LocalContext.current)
}

@Composable
fun CourseCard(course: Course, context: Context, navController: NavController) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = ""
    )

    Column {
        Text("Click here", style = CC.descriptionTextStyle(context),
            modifier = Modifier.clickable {
                CourseName.courseID.value = course.courseCode
                CourseName.name.value = course.courseName
                navController.navigate("courseContent/${course.courseCode}")
            })
    Box(
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { isFlipped = !isFlipped }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clip(RoundedCornerShape(12.dp))
            .background(CC.extraColor2())
    ) {
        if (rotation <= 90f) {
            FrontCardContent(course.courseName,  context)
        } else {
            BackCardContent(course.courseCode, course.visits.toString(), context)
        }
    }}
}

@Composable
fun FrontCardContent(courseTitle: String, context: Context) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = courseTitle,
            style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }


}

@Composable
fun BackCardContent(courseCode: String, visits: String, context: Context) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .graphicsLayer {
                rotationY = 180f
            }
    ) {
        Text(
            text = "Course Code: $courseCode",
            style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Visits: $visits",
            style = CC.descriptionTextStyle(context).copy(fontSize = 16.sp)
        )
    }
}
