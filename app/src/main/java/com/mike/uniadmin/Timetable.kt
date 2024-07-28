package com.mike.uniadmin

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetable
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableViewModel
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableViewModelFactory
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.ui.theme.GlobalColors
import com.mike.uniadmin.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(context: Context) {
    val announcementAdmin = context.applicationContext as? UniAdmin
    val courseRepository = remember { announcementAdmin?.courseRepository }
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(
            courseRepository ?: throw IllegalStateException("CourseRepository is null")
        )
    )
    val timetableRepository = remember { announcementAdmin?.courseTimetableRepository }
    val timetableViewModel: CourseTimetableViewModel = viewModel(
        factory = CourseTimetableViewModelFactory(
            timetableRepository ?: throw IllegalStateException("TimetableRepository is null")
        )
    )

    val timetables by timetableViewModel.courseTimetables.observeAsState(emptyList())
    val courses by courseViewModel.courses.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        courseViewModel.fetchCourses()
    }

    LaunchedEffect(courses) {
        timetableViewModel.getAllCourseTimetables()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timetable", style = CC.titleTextStyle(context)) },
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
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (timetables.isNotEmpty()) { // Check if timetables are not empty
                val groupedTimetables = timetables.groupBy { it.day } // Group by day
                LazyColumn {
                    groupedTimetables.forEach { (day, timetablesForDay) ->
                        item {
                            Text(text = day ?: "Unknown Day", style = CC.titleTextStyle(context))
                        }
                        items(timetablesForDay) { timetable ->
                            val courseName = courses.find { it.courseCode == timetable.courseID }?.courseName
                            TimetableCard(timetable = timetable, courseName = courseName ?: "Unknown Course", context = context)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CC.textColor())
                }
            }
        }
    }
}

@Composable
fun TimetableCard(timetable: CourseTimetable, courseName: String?, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            courseName?.let {
                Text(
                    text = "Course: $it",
                    style = CC.titleTextStyle(context)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            timetable.day?.let {
                Text(
                    text = "Day: $it",
                    style = CC.descriptionTextStyle(context)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            timetable.startTime?.let {
                Text(
                    text = "Start Time: $it",
                    style = CC.descriptionTextStyle(context)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            timetable.endTime?.let {
                Text(
                    text = "End Time: $it",
                    style = CC.descriptionTextStyle(context)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            timetable.venue?.let {
                Text(
                    text = "Venue: $it",
                    style = CC.descriptionTextStyle(context)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            timetable.lecturer?.let {
                Text(
                    text = "Lecturer: $it",
                    style = CC.descriptionTextStyle(context)
                )
            }
        }
    }
}

