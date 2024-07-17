package com.mike.uniadmin

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BasicAlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mike.uniadmin.model.Assignment
import com.mike.uniadmin.model.Course
import com.mike.uniadmin.model.Details
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.notification.showNotification
import kotlinx.coroutines.launch
import com.mike.uniadmin.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(navController: NavController, context: Context) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var loading by remember { mutableStateOf(true) }
    val courses = remember { mutableStateListOf<Course>() }
    var assignmentDialog by remember { mutableStateOf(false) }
    var showaddSubject by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }



    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Assignments", style = CC.titleTextStyle(context)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(Icons.Default.ArrowBackIosNew, "Back", tint = CC.textColor())
                    }
                },
                actions = {
                    IconButton(onClick = {
                        loading = true
                        MyDatabase.fetchCourses { fetchedCourses ->
                            courses.clear()
                            courses.addAll(fetchedCourses ?: emptyList())
                            loading = false
                        }
                    }) {
                        Icon(Icons.Default.Refresh, "Refresh", tint = CC.textColor())
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.MoreVert, "Add", tint = CC.textColor())
                    }
                    DropdownMenu(
                        onDismissRequest = { expanded = false },
                        expanded = expanded,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = CC.textColor(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(CC.primary())
                    ) {
                        DropdownMenuItem(text = {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    Icons.Default.AddCircleOutline,
                                    "Add Assignment",
                                    tint = CC.textColor()
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Add Assignment", style = CC.descriptionTextStyle(context))

                            }
                        }, onClick = {
                            assignmentDialog = true
                            expanded = false
                        })
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(),
                    titleContentColor = CC.textColor(),
                )

            )

        },
        containerColor = CC.primary(),

        ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        ) {

            LaunchedEffect(Unit) {
                MyDatabase.fetchCourses { fetchedCourses ->
                    courses.clear()
                    courses.addAll(fetchedCourses ?: emptyList())
                    loading = false
                }
            }


            val indicator = @Composable { tabPositions: List<TabPosition> ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .height(4.dp)
                        .width(screenWidth / (courses.size.coerceAtLeast(1))) // Avoid division by zero
                        .background(CC.secondary(), CircleShape)
                )
            }

            val coroutineScope = rememberCoroutineScope()

            if (loading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = CC.secondary(), trackColor = CC.textColor()
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Loading Units", style = CC.descriptionTextStyle(context))

                }

            } else if (courses.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Text("No units found", style = CC.descriptionTextStyle(context))
                }
            } else {

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.background(Color.LightGray),
                    contentColor = Color.Black,
                    indicator = indicator,
                    edgePadding = 0.dp,
                    containerColor = CC.primary()
                ) {
                    courses.forEachIndexed { index, course ->

                        Tab(selected = selectedTabIndex == index, onClick = {
                            selectedTabIndex = index
                            coroutineScope.launch {
                                // Load assignments for the selected subject
                            }
                        }, text = {

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selectedTabIndex == index) CC.secondary() else CC.primary(),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = course.courseName,
                                    color = if (selectedTabIndex == index) CC.textColor() else CC.tertiary(),
                                )
                            }
                        }, modifier = Modifier.background(CC.primary())
                        )
                    }
                }

                when (selectedTabIndex) {
                    in courses.indices -> {
                        AssignmentsList(subjectId = courses[selectedTabIndex].courseCode, context)
                    }
                }
            }
            if (assignmentDialog) {
                BasicAlertDialog(onDismissRequest = { assignmentDialog = false }) {
                    Column(
                        modifier = Modifier
                            .width(250.dp)
                            .background(
                                color = CC.primary(), shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Add Assignment",
                            style = CC.titleTextStyle(context),
                            color = CC.textColor()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = title,
                            onValueChange = { title = it },
                            label = { Text("Assignment Name", color = CC.tertiary()) },
                            colors = TextFieldDefaults.colors(
                                unfocusedIndicatorColor = CC.tertiary(),
                                focusedTextColor = CC.textColor(),
                                unfocusedTextColor = CC.textColor(),
                                focusedContainerColor = CC.primary(),
                                unfocusedContainerColor = CC.primary()
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(value = description,
                            onValueChange = { description = it },
                            label = { Text("Description", color = CC.tertiary()) },
                            colors = TextFieldDefaults.colors(
                                unfocusedIndicatorColor = CC.tertiary(),
                                focusedTextColor = CC.textColor(),
                                unfocusedTextColor = CC.textColor(),
                                focusedContainerColor = CC.primary(),
                                unfocusedContainerColor = CC.primary()
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { assignmentDialog = false },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CC.secondary(),
                                    contentColor = CC.primary()
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    "Cancel",
                                    style = CC.descriptionTextStyle(context),
                                    color = CC.primary()
                                )
                            }
                            Button(
                                onClick = {
                                    MyDatabase.generateAssignmentID { assignmentID ->
                                        val assignment = Assignment(
                                            id = assignmentID,
                                            courseCode = courses[selectedTabIndex].courseCode,
                                            name = title,
                                            description = description

                                        )
                                        MyDatabase.writeAssignment(assignment, onComplete = {
                                            Toast.makeText(
                                                context, "Assignment Added", Toast.LENGTH_SHORT

                                            ).show()
                                            assignmentDialog = false
                                            showNotification(
                                                context,
                                                title = "New Assignment",
                                                message = "${Details.firstName.value} added an assignment.  "
                                            )
                                        })}
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CC.secondary(),
                                    contentColor = CC.primary()
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    "Add",
                                    style = CC.descriptionTextStyle(context),
                                    color = CC.primary()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AssignmentsList(subjectId: String, context: Context) {
    var assignments by remember { mutableStateOf<List<Assignment>?>(null) }
    LaunchedEffect(subjectId) {
        MyDatabase.getAssignments(subjectId) { fetchedAssignments ->
            assignments = fetchedAssignments
        }
    }

    if (assignments == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = CC.secondary(), trackColor = CC.textColor()
            )
            Text("Loading Assignments...Please wait", style = CC.descriptionTextStyle(context))
        }
    } else {

        LazyColumn {
            if (assignments!!.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No assignments found.", style = CC.descriptionTextStyle(context))
                    }
                }
            }
            items(assignments!!) { assignment ->

                AssignmentCard(assignment = assignment, onEdit = {
                    MyDatabase.editAssignment(it) { isSuccess ->
                        if (isSuccess) {
                            Toast.makeText(
                                context, "Assignment Edited", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context, "Failed to edit assignment", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }, onDelete = {
                    MyDatabase.deleteAssignment(it) { isSuccess ->
                        if (isSuccess) {
                            assignments = assignments?.filter { it.id != assignment.id }
                            Toast.makeText(
                                context, "Assignment Deleted", Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context, "Failed to delete assignment", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }, context)

            }
        }
    }
}


@Composable
fun AssignmentCard(
    assignment: Assignment,
    onEdit: (Assignment) -> Unit = {},
    onDelete: (String) -> Unit = {},
    context: Context
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(assignment.name) }
    var editedDescription by remember { mutableStateOf(assignment.description) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(tween(1000)),
        exit = slideOutVertically(tween(1000))
    ) {


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), colors = CardDefaults.cardColors(
                containerColor = CC.secondary(), contentColor = CC.textColor()
            ), elevation = CardDefaults.elevatedCardElevation(), shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = CC.textColor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isEditing) {
                        OutlinedTextField(value = editedName,
                            onValueChange = { editedName = it },
                            label = { Text("Assignment Name", color = CC.tertiary()) },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = CC.primary(),
                                unfocusedIndicatorColor = CC.tertiary(),
                                focusedTextColor = CC.textColor(),
                                unfocusedTextColor = CC.textColor(),
                                focusedContainerColor = CC.primary(),
                                unfocusedContainerColor = CC.primary()
                            )
                        )
                    } else {
                        Text(
                            text = assignment.name,
                            style = CC.titleTextStyle(context).copy(fontSize = 18.sp),
                            color = CC.textColor()
                        )
                    }
                    Row {
                        IconButton(
                            onClick = {
                                if (isEditing) {
                                    // Save the edited assignment
                                    isSaving = true
                                    val updatedAssignment = assignment.copy(
                                        name = editedName, description = editedDescription
                                    )
                                    MyDatabase.editAssignment(updatedAssignment) { isSuccess ->
                                        isSaving = false
                                        if (isSuccess) {
                                            onEdit(updatedAssignment)
                                            isEditing = false
                                        } else {
                                            errorMessage =
                                                "Failed to save changes. Please try again."
                                        }
                                    }
                                } else {
                                    isEditing = true
                                }
                            }, modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Save Assignment" else "Edit Assignment",
                                tint = CC.textColor()
                            )
                        }
                        if (isEditing) {
                            IconButton(
                                onClick = {
                                    // Cancel the edit
                                    editedName = assignment.name
                                    editedDescription = assignment.description
                                    isEditing = false
                                    errorMessage = null
                                }, modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cancel Edit",
                                    tint = CC.primary()
                                )
                            }
                        } else {
                            IconButton(
                                onClick = { onDelete(assignment.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Assignment",
                                    tint = CC.textColor()
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Author: ${Details.firstName.value} ${Details.lastName.value}",
                    style = CC.descriptionTextStyle(context),
                    color = CC.tertiary()
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (isEditing) {
                    OutlinedTextField(value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Description", color = CC.tertiary()) },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = CC.primary(),
                            unfocusedIndicatorColor = CC.tertiary(),
                            focusedTextColor = CC.textColor(),
                            unfocusedTextColor = CC.textColor(),
                            focusedContainerColor = CC.primary(),
                            unfocusedContainerColor = CC.primary()
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = assignment.description,
                        style = CC.descriptionTextStyle(context),
                        color = CC.textColor()
                    )
                }
                if (isSaving) {
                    CircularProgressIndicator(
                        color = CC.textColor(),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun AssignmentScreenPreview() {
    AssignmentScreen(rememberNavController(), LocalContext.current)
}