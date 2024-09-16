package com.mike.uniadmin.courses

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavController
import com.mike.uniadmin.CourseManager
import com.mike.uniadmin.UniConnectPreferences
import com.mike.uniadmin.getCourseViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.model.courses.AcademicYear
import com.mike.uniadmin.model.courses.Course
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectYourCourse(context: Context, navController: NavController) {
    val userViewModel = getUserViewModel(context)
    val currentUser by userViewModel.user.observeAsState()

    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedYear by remember { mutableStateOf<AcademicYear?>(null) }
    var selectedSemester by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val courseViewModel = getCourseViewModel(context)
    val courses by courseViewModel.courses.observeAsState(emptyList())
    val academicYears by courseViewModel.academicYears.observeAsState(emptyList())

    var loading by remember { mutableStateOf(true) }

    // Effect to load data
    LaunchedEffect(Unit, loading) {
        if (loading) {
            courseViewModel.loadCourses()
            courseViewModel.getAllAcademicYears()
            // Set loading to false after data is loaded
            loading = false
        }
    }

    // Filter courses based on search query
    val filteredCourses = remember(searchQuery, courses) {
        if (searchQuery.isEmpty()) {
            courses
        } else {
            courses.filter { it.courseName.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Your Course", color = CC.tertiary()) },
                actions = {
                    IconButton(onClick = {
                        loading = true // Trigger reload
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CC.tertiary())
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary())
            )
        },
        containerColor = CC.primary()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Course") },
                modifier = Modifier.fillMaxWidth(),
                colors = CC.appTextFieldColors(),
                shape = RoundedCornerShape(10.dp)
            )

            // Show loading indicator while data is being loaded
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                ) {
                    CircularProgressIndicator(color = CC.textColor())
                }
            } else {
                // Display course list or message
                if (filteredCourses.isEmpty()) {
                    Text("No courses found", color = CC.secondary())
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(filteredCourses) { course ->
                            CourseItem(
                                course = course,
                                isSelected = course == selectedCourse,
                                onSelect = {
                                    selectedCourse = course
                                    selectedYear = null
                                    selectedSemester = null
                                }
                            )
                        }
                    }
                }
            }

            // Year selection
            if (selectedCourse != null) {
                CourseSelectionCard(
                    title = "Academic Year",
                    selectedItem = selectedYear?.year,
                    items = academicYears.map { it.year },
                    onItemSelected = { year ->
                        selectedYear = academicYears.find { it.year == year }
                        selectedSemester = null
                    }
                )
            }

            // Semester selection
            if (selectedYear != null) {
                CourseSelectionCard(
                    title = "Semester",
                    selectedItem = selectedSemester,
                    items = selectedYear!!.semesters,
                    onItemSelected = { semester ->
                        selectedSemester = semester
                    }
                )
            }

            // Confirm button
            Button(
                onClick = { showConfirmDialog = true },
                enabled = selectedCourse != null && selectedYear != null && selectedSemester != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = CC.surfaceContainer(),
                    containerColor = CC.secondary())
            ) {
                Text("Confirm Selection", style = CC.descriptionTextStyle())
            }
        }

        // Confirmation dialog
        if (showConfirmDialog) {
            AlertDialog(
                containerColor = CC.primary(),
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Confirm Selection") },
                text = {
                    Text("Selected Course Code: ${selectedCourse!!.courseCode}-${selectedYear!!.year}-$selectedSemester",
                        style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold))
                },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog = false
                        val selectedCourseCode =
                            "${selectedCourse!!.courseCode}-${selectedYear!!.year}-$selectedSemester"
                        CourseManager.updateCourseCode(selectedCourseCode)
                        navController.navigate("homeScreen"){
                            popUpTo("courseSelection"){
                                inclusive = true
                            }
                        }
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = CC.extraColor2())
                    ) {
                        Text("Confirm", style = CC.descriptionTextStyle())
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("Cancel", style = CC.descriptionTextStyle())
                    }
                }
            )
        }
    }
}


@Composable
fun CourseItem(
    course: Course,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CC.secondary() else CC.surfaceContainer()
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.courseName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) CC.primary() else CC.textColor()
                )
                Text(
                    text = course.courseCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) CC.primary() else CC.surfaceContainer()
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = CC.extraColor2()
                )
            }
        }
    }
}

@Composable
fun CourseSelectionCard(
    title: String,
    selectedItem: String?,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CC.surfaceContainer())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = CC.secondary()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box {
                TextButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        selectedItem ?: "Select $title",
                        color = CC.tertiary(),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CC.surfaceContainer())
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            },
                            text = { Text(item) }
                        )
                    }
                }
            }
        }
    }
}


