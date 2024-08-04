package com.mike.uniadmin.attendance

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mike.uniadmin.dataModel.courses.AttendanceState
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.UniAdmin

import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAttendanceScreen(context: Context) {
    val announcementAdmin = context.applicationContext as? UniAdmin
    val courseRepository = remember { announcementAdmin?.courseRepository }
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(
            courseRepository ?: throw IllegalStateException("CourseRepository is null")
        )
    )
    val courses by courseViewModel.courses.observeAsState(emptyList())
    val attendanceStates by courseViewModel.attendanceStates.observeAsState(emptyMap())
    var refresh by remember { mutableStateOf(false) }

    LaunchedEffect(refresh) {
        
        courseViewModel.fetchAttendanceStates()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = {refresh = !refresh}) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh",
                            tint = CC.textColor())
                    }
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
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center) {
                Text("Manage Attendance", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold, fontSize = 30.sp))
            }

            if (courses.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("No courses found", style = CC.descriptionTextStyle(context))
                }
            } else {
                courses.forEach { course ->
                    val attendanceState = attendanceStates[course.courseCode]
                    val isChecked = attendanceState?.state ?: false
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isChecked) CC.extraColor2() else CC.primary(),
                        animationSpec = tween(durationMillis = 300),
                        label = ""
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
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
                            fontSize = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Switch(
                            checked = isChecked,
                            onCheckedChange = { newState ->
                                val newAttendanceState = AttendanceState(
                                    courseID = course.courseCode,
                                    courseName = course.courseName,
                                    state = newState
                                )
                                courseViewModel.saveAttendanceState(newAttendanceState)
                            },
                            colors = SwitchDefaults.colors(
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
    ManageAttendanceScreen(context = LocalContext.current)
}