package com.mike.uniadmin

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mike.uniadmin.courseContent.AddTimetableItem
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetable
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableViewModel
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableViewModelFactory
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.UniAdmin

import com.mike.uniadmin.ui.theme.CommonComponents as CC

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
    var refresh by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val timetables by timetableViewModel.courseTimetables.observeAsState(emptyList())
    val courses by courseViewModel.courses.observeAsState(emptyList())
    var courseCode by remember { mutableStateOf("") }

    LaunchedEffect(refresh) {
        
        courseViewModel.fetchCourses()
        timetableViewModel.getAllCourseTimetables()

    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Timetable",
                            tint = CC.textColor())
                    }
                    IconButton(onClick = { refresh = !refresh }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh",
                            tint = CC.textColor())
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
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Timetable", style = CC.titleTextStyle(context).copy(fontSize = 30.sp, fontWeight = FontWeight.Bold))
            }
            AnimatedVisibility(visible) {
                Column(modifier = Modifier
                    .border(
                        1.dp, CC.textColor(), RoundedCornerShape(10.dp)
                    )
                    .fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Add Timetable", style = CC.titleTextStyle(context).copy(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                CC.SingleLinedTextField(
                    value = courseCode,
                    onValueChange = { newText ->
                        courseCode = newText
                    },
                    label = "Course Code",
                    context = context,
                    singleLine = true
                )
                AddTimetableItem(courseCode, timetableViewModel, context, expanded = visible, onExpandedChange = {visible = !visible})
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            if (timetables.isNotEmpty()) {
                val groupedTimetables = timetables.groupBy { fetchedTimetable ->
                    fetchedTimetable.day }
                LazyColumn {
                    groupedTimetables.forEach { (day, timetablesForDay) ->
                        if (day != null) { // Check for null day
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = day, style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                            items(timetablesForDay) { timetable ->
                                val courseName = courses.find { it.courseCode == timetable.courseID }?.courseName
                                if (courseName != null) { // Check for null courseName
                                    TimetableCard(timetable = timetable, courseName = courseName, context = context)
                                }
                            }
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
                    style = CC.titleTextStyle(context).copy(textAlign = TextAlign.Center)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Timelapse, contentDescription = "Time", tint = CC.textColor())
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Time: ${timetable.startTime} - ${timetable.endTime}",
                    style = CC.descriptionTextStyle(context)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Day", tint = CC.textColor())
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Day: ${timetable.venue}",
                    style = CC.descriptionTextStyle(context)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Day", tint = CC.textColor())
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lecturer: ${timetable.lecturer?:"No Lecturer"}",
                    style = CC.descriptionTextStyle(context)
                )
            }
        }
    }
}

