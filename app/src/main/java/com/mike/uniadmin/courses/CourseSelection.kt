package com.mike.uniadmin.courses

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mike.uniadmin.CourseManager
import com.mike.uniadmin.getCourseViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.model.courses.AcademicYear
import com.mike.uniadmin.model.courses.Course
import com.mike.uniadmin.model.courses.CourseViewModel
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
    var showAddCourse by remember { mutableStateOf(false) }

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
                title = { Text("Select Your Course", style = CC.titleTextStyle()) },
                actions = {
                    IconButton(onClick = {
                        loading = true // Trigger reload
                    }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = CC.tertiary()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        showAddCourse = !showAddCourse
                    }) {
                        Icon(
                            if (showAddCourse) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Add Course",
                            tint = CC.tertiary()
                        )
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
                placeholder = {
                    Text(
                        "Search Course by name or code",
                        style = CC.descriptionTextStyle()
                            .copy(color = CC.tertiary(), fontSize = 13.sp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CC.appTextFieldColors(),
                shape = RoundedCornerShape(10.dp)
            )
            AnimatedVisibility(showAddCourse) {
                AddCourse(courseViewModel){
                    showAddCourse = false
                }
            }

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
                    containerColor = CC.secondary()
                )
            ) {
                Text(
                    "Confirm Selection",
                    style = CC.descriptionTextStyle().copy(color = CC.tertiary())
                )
            }
        }

        // Confirmation dialog
        if (showConfirmDialog) {
            AlertDialog(
                containerColor = CC.primary(),
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Confirm Selection") },
                text = {
                    Text(
                        "Selected Course Code: ${selectedCourse!!.courseCode}-${selectedYear!!.year}-$selectedSemester",
                        style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            val selectedCourseCode =
                                "${selectedCourse!!.courseCode}-${selectedYear!!.year}-$selectedSemester"
                            CourseManager.updateCourseCode(selectedCourseCode)
                            navController.navigate("homeScreen") {
                                popUpTo("courseSelection") {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                    style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) CC.primary() else CC.textColor().copy(0.8f)
                )
                Text(
                    text = course.courseCode,
                    style = CC.descriptionTextStyle(),
                    color = if (isSelected) CC.textColor() else CC.textColor().copy(0.8f)
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


@Composable
fun AddCourse(courseViewModel: CourseViewModel, onCourseAdded:() -> Unit) {
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var academicYear by remember { mutableStateOf("") }
    var semesters by remember { mutableStateOf(listOf<String>()) }
    var semesterInput by remember { mutableStateOf("") }
    var academicYearsList by remember { mutableStateOf(mutableListOf<AcademicYear>()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = CC.surfaceContainer()),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .imePadding()
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Add Course",
                    style = CC.titleTextStyle(),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Course Name
                CC.CustomTextField(
                    value = courseName,
                    onValueChange = { newValue -> courseName = newValue },
                    label = "Course Name",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Course Code
                CC.CustomTextField(
                    value = courseCode,
                    onValueChange = { newValue -> courseCode = newValue },
                    label = "Course Code",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Academic Year
                CC.CustomTextField(
                    value = academicYear,
                    onValueChange = { newValue -> academicYear = newValue },
                    label = "Academic Year (e.g. 2023-2024)",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Semester Input
                CC.CustomTextField(
                    value = semesterInput,
                    onValueChange = { newValue -> semesterInput = newValue },
                    label = "Add Semester (e.g. Sem1)",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(5.dp))

                // Button to add semester
                Button(
                    onClick = {
                        if (semesterInput.isNotEmpty()) {
                            semesters = semesters + semesterInput
                            semesterInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Semester", style = CC.descriptionTextStyle())
                }

                // Display added semesters
                if (semesters.isNotEmpty()) {
                    Text(
                        text = "Semesters: ${semesters.joinToString(", ")}",
                        style = CC.descriptionTextStyle(),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }

                // Button to add academic year
                Button(
                    onClick = {
                        if (academicYear.isNotEmpty() && semesters.isNotEmpty()) {
                            academicYearsList.add(AcademicYear(academicYear, semesters))
                            academicYear = ""
                            semesters = emptyList()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Academic Year", style = CC.descriptionTextStyle())
                }

                // Display added academic years
                if (academicYearsList.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Text("Academic Years:", style = CC.descriptionTextStyle())
                        academicYearsList.forEach { year ->
                            Text(
                                text = "${year.year}: ${year.semesters.joinToString(", ")}",
                                style = CC.descriptionTextStyle()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Course Button
                Button(
                    onClick = {
                        if (courseName.isNotEmpty() && courseCode.isNotEmpty()) {
                            val newCourse = Course(courseCode, courseName, academicYearsList)
                            courseViewModel.addCourse(newCourse)
                            onCourseAdded()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CC.secondary())
                ) {
                    Text("Add Course", style = CC.descriptionTextStyle())
                }
            }
        }
    }
}

