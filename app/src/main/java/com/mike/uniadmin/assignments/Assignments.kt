package com.mike.uniadmin.assignments

import android.content.Context
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignment
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignmentViewModel
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignmentViewModelFactory
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(context: Context) {
    val announcementAdmin = context.applicationContext as? UniAdmin
    val courseRepository = remember { announcementAdmin?.courseRepository }
    val courseViewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(
            courseRepository ?: throw IllegalStateException("CourseRepository is null")
        )
    )
    val assignmentRepository = remember { announcementAdmin?.courseAssignmentRepository }
    val assignmentViewModel: CourseAssignmentViewModel = viewModel(
        factory = CourseAssignmentViewModelFactory(
            assignmentRepository ?: throw IllegalStateException("AssignmentRepository is null")
        )
    )

    val assignments by assignmentViewModel.assignments.observeAsState()
    val courses by courseViewModel.courses.observeAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCourseId by remember { mutableStateOf<String?>(null) }

    val isLoading by assignmentViewModel.isLoading.observeAsState()

    LaunchedEffect(Unit) {
        courseViewModel.fetchCourses()
    }

    LaunchedEffect(selectedTabIndex) {
        selectedCourseId = courses?.getOrNull(selectedTabIndex)?.courseCode
        selectedCourseId?.let { assignmentViewModel.getCourseAssignments(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignments", style = CC.titleTextStyle(context)) },
                navigationIcon = {},
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
            when {
                courses.isNullOrEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No courses available", color = CC.textColor())
                    }
                }
                isLoading == true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CC.textColor())
                    }
                }
                else -> {
                    courses?.let { courseList ->
                        ScrollableTabRow(
                            containerColor = CC.primary(), selectedTabIndex = selectedTabIndex
                        ) {
                            courseList.forEachIndexed { index, course ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            course.courseName, style = CC.descriptionTextStyle(context)
                                        )
                                    }
                                )
                            }
                        }

                        assignments?.let { assignmentList ->
                            if (assignmentList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No assignments available", color = CC.textColor())
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn {
                                    items(assignmentList) { assignment ->
                                        AssignmentCard(assignment = assignment, context)
                                    }
                                }
                            }
                        } ?: run {
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
        }
    }
}


@Composable
fun AssignmentCard(assignment: CourseAssignment, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(8.dp), colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ), elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = assignment.title, style = CC.titleTextStyle(context)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = assignment.description, style = CC.descriptionTextStyle(context)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Due Date: ${assignment.dueDate}",
                style = CC.descriptionTextStyle(context),
                color = CC.textColor()
            )
        }
    }
}
