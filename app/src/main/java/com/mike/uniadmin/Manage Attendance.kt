package com.mike.uniadmin

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mike.uniadmin.model.AttendanceState
import com.mike.uniadmin.model.Course
import com.mike.uniadmin.model.MyDatabase.fetchAttendanceState
import com.mike.uniadmin.model.MyDatabase.fetchCourses
import com.mike.uniadmin.model.MyDatabase.saveAttendanceState
import com.mike.uniadmin.ui.theme.GlobalColors
import kotlinx.coroutines.delay
import com.mike.uniadmin.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAttendanceScreen(navController: NavController, context: Context) {
    val courses = remember { mutableStateListOf<Course>() }
    val attendanceStates = remember { mutableStateMapOf<String, Boolean>() }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(loading) {
        GlobalColors.loadColorScheme(context)
        // Initial fetch of courses and attendance states
        fetchCourses { fetchedCourses ->
            courses.clear()
            courses.addAll(fetchedCourses)

            fetchedCourses.forEach { course ->
                fetchAttendanceState(course.courseCode) { fetchedState ->
                    fetchedState?.let {
                        attendanceStates[course.courseCode] = it.state
                    } ?: run {
                        attendanceStates[course.courseCode] = false
                    }
                }
            }
            loading = false
        }

        // Continuous fetching of attendance states
        while (true) {
            delay(10)
            courses.forEach { course ->
                fetchAttendanceState(course.courseCode) { fetchedState ->
                    fetchedState?.let {
                        attendanceStates[course.courseCode] = it.state
                    } ?: run {
                        attendanceStates[course.courseCode] = false
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Manage Attendance Sign-ins",
                        style = CC.titleTextStyle(context)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary()
                ),
            )

        }, containerColor = CC.primary()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(CC.primary())
                .padding(innerPadding)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center){
                    CircularProgressIndicator(
                        color = CC.primary(),
                        trackColor = CC.textColor()
                    )
                }
            }else
                if(courses.isEmpty()){
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center) {
                        Text("No courses found", style = CC.descriptionTextStyle(context))
                    }

                }else{
                    courses.forEach { course ->
                        val isChecked = attendanceStates[course.courseCode] ?: false
                        val backgroundColor by animateColorAsState(
                            targetValue = if (isChecked) CC.extraColor2() else CC.primary(),
                            animationSpec = tween(durationMillis = 300),
                            label = ""
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(backgroundColor, shape = MaterialTheme.shapes.medium)
                                .padding(16.dp)
                                .animateContentSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                course.courseName,
                                modifier = Modifier.weight(1f),
                                style = CC.descriptionTextStyle(context),
                                fontSize = 18.sp
                            )
                            Switch(
                                checked = isChecked, onCheckedChange = { isChecked ->
                                    attendanceStates[course.courseCode] = isChecked
                                    saveAttendanceState(
                                        AttendanceState(
                                            courseID = course.courseCode,
                                            courseName = course.courseName,
                                            state = isChecked
                                        )
                                    )
                                }, colors = SwitchDefaults.colors(
                                    checkedThumbColor = CC.primary(),
                                    checkedTrackColor = CC.secondary(),
                                    uncheckedThumbColor = CC.secondary(),
                                    uncheckedTrackColor = CC.primary()
                                )
                            )
                        }
                    }
                }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewManageAttendanceScreen() {
    ManageAttendanceScreen(rememberNavController(), context = LocalContext.current)
}
