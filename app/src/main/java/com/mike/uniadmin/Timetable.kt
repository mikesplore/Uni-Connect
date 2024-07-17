package com.mike.uniadmin

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mike.uniadmin.model.Day
import com.mike.uniadmin.model.Details
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.Timetable
import com.mike.uniadmin.notification.showNotification
import com.mike.uniadmin.ui.theme.GlobalColors
import kotlinx.coroutines.launch
import com.mike.uniadmin.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(navController: NavController, context: Context) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var unitName by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableIntStateOf(CC.currentDayID()-1) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    var loading by remember { mutableStateOf(true) }
    val days = remember { mutableStateListOf<Day>() }
    var timetableDialog by remember { mutableStateOf(false) }
    var showaddDay by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Timetable", style = CC.titleTextStyle(context)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("dashboard") }) {
                        Icon(Icons.Default.ArrowBackIosNew, "Back", tint = CC.textColor())
                    }
                },
                actions = {
                    IconButton(onClick = {
                        loading = true
                        MyDatabase.getDays { fetchedDays ->
                            days.clear()
                            days.addAll(fetchedDays ?: emptyList())
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
                            Row(
                                modifier = Modifier
                                    .height(30.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.AddCircleOutline,
                                    "Add Day",
                                    tint = CC.textColor()
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Add day", style = CC.descriptionTextStyle(context))

                            }
                        }, onClick = {
                            showaddDay = true
                            expanded = false
                        })
                        DropdownMenuItem(text = {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    Icons.Default.AddCircleOutline,
                                    "Add timetable",
                                    tint = CC.textColor()
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text("Add Timetable", style = CC.descriptionTextStyle(context))

                            }
                        }, onClick = {
                            timetableDialog = true
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
                GlobalColors.loadColorScheme(context)
                MyDatabase.getDays { fetchedDays ->
                    days.clear()
                    days.addAll(fetchedDays ?: emptyList())
                    loading = false
                }
            }


            val indicator = @Composable { tabPositions: List<TabPosition> ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .height(4.dp)
                        .width(screenWidth / (days.size.coerceAtLeast(1))) // Avoid division by zero
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
                    Text("Loading Days...Please wait", style = CC.descriptionTextStyle(context))

                }

            } else if (days.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Text("No days found", style = CC.descriptionTextStyle(context))

                }

            } else {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.background(CC.primary()),
                    contentColor = Color.Black,
                    indicator = indicator,
                    edgePadding = 0.dp,
                    containerColor = CC.primary()
                ) {
                    days.forEachIndexed { index, day ->

                        Tab(selected = selectedTabIndex == index, onClick = {
                            selectedTabIndex = index
                            coroutineScope.launch {
                                //load days
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
                                    text = day.name,
                                    color = if (selectedTabIndex == index) CC.textColor() else CC.tertiary(),
                                )
                            }
                        }, modifier = Modifier.background(CC.primary())
                        )
                    }
                }

                when (selectedTabIndex) {
                    in days.indices -> {
                        DayList(dayid = days[selectedTabIndex].id, context)
                    }
                }
            }

            if (showaddDay) {
                BasicAlertDialog(onDismissRequest = { showaddDay = false }) {
                    Column(
                        modifier = Modifier
                            .width(250.dp)
                            .background(
                                color = CC.primary(), shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Add Day",
                            style = CC.titleTextStyle(context),
                            color = CC.textColor()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomOutlinedTextField(value = day, onValueChange = { day = it })
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { showaddDay = false },
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
                                    MyDatabase.generateDayID {  dayId ->
                                        val newDay = Day(
                                            id = dayId,
                                            name = day
                                        )
                                        MyDatabase.writeDays(newDay, onComplete = {
                                            Toast.makeText(
                                                context, "Day Added", Toast.LENGTH_SHORT
                                            ).show()
                                            showaddDay = false
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



            if (timetableDialog) {
                BasicAlertDialog(onDismissRequest = { timetableDialog = false }) {
                    Column(
                        modifier = Modifier
                            .width(250.dp)
                            .background(
                                color = CC.primary(), shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Add Timetable",
                            style = CC.titleTextStyle(context),
                            color = CC.textColor()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomOutlinedTextField(
                            value = unitName,
                            label = "Unit name",
                            onValueChange = { it -> unitName = it },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomOutlinedTextField(
                            value = venue,
                            label = "Venue",
                            onValueChange = { it -> venue = it },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomOutlinedTextField(
                            value = lecturer,
                            label = "Lecturer",
                            onValueChange = { it -> lecturer = it },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomOutlinedTextField(
                            value = startTime,
                            label = "Start time",
                            onValueChange = { it -> startTime = it },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomOutlinedTextField(
                            value = endTime,
                            label = "End time",
                            onValueChange = { it -> endTime = it },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { timetableDialog = false },
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
                                    MyDatabase.generateTimetableID { timetableID ->
                                        val timetable = Timetable(
                                            id = timetableID,
                                            dayId = days[selectedTabIndex].id,
                                            unitName = unitName,
                                            lecturer = lecturer,
                                            venue = venue,
                                            startTime = startTime,
                                            endTime = endTime
                                        )
                                        MyDatabase.writeTimetable(timetable, onComplete = {
                                            Toast.makeText(
                                                context, "Timetable item Added", Toast.LENGTH_SHORT

                                            ).show()
                                            timetableDialog = false
                                            showNotification(
                                                context,
                                                title = "New Timetable Item",
                                                message = "${Details.firstName.value} added an Event.  "
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

@Composable
fun DayList(dayid: String, context: Context) {
    var timetables by remember { mutableStateOf<List<Timetable>?>(null) }
    LaunchedEffect(dayid) {
        MyDatabase.getTimetable(dayid) { fetchedTimetable ->
            timetables = fetchedTimetable
        }
    }

    if (timetables == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = CC.secondary(), trackColor = CC.textColor()
            )
            Text("Loading Events...Please wait", style = CC.descriptionTextStyle(context))
            Text(
                "If this takes longer, please check your internet connection",
                style = CC.descriptionTextStyle(context),
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn {
            if (timetables!!.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No event found.", style = CC.descriptionTextStyle(context))
                    }
                }
            }
            items(timetables!!) { timetable ->
                AnimatedVisibility(
                    visible = true, enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                        animationSpec = tween(500)
                    ), exit = fadeOut(animationSpec = tween(500)) + slideOutVertically(
                        animationSpec = tween(500)
                    )
                ) {
                    TimetableCard(timetable = timetable, onEdit = {
                        MyDatabase.editTimetable(it) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(
                                    context, "Timetable Edited", Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context, "Failed to edit timetable", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }, onDelete = {
                        MyDatabase.deleteTimetable(it) { isSuccess ->
                            if (isSuccess) {
                                timetables = timetables?.filter { it.id != timetable.id }
                                Toast.makeText(
                                    context, "Event Deleted", Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context, "Failed to delete Event", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }, context = context)
                }
            }
        }
    }
}


@Composable
fun TimetableCard(
    timetable: Timetable,
    onEdit: (Timetable) -> Unit = {},
    onDelete: (String) -> Unit = {},
    context: Context
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedUnitName by remember { mutableStateOf(timetable.unitName) }
    var editedVenue by remember { mutableStateOf(timetable.venue) }
    var editedStartTime by remember { mutableStateOf(timetable.startTime) }
    var editedEndTime by remember { mutableStateOf(timetable.endTime) }
    var editedLecturer by remember { mutableStateOf(timetable.lecturer) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    width = 1.dp, color = CC.textColor(), shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (isEditing) {
                    CustomOutlinedTextField(
                        value = editedUnitName,
                        label = "Unit Name",
                        onValueChange = { editedUnitName = it },
                    )
                } else {
                    Text(
                        text = timetable.unitName,
                        style = CC.titleTextStyle(context).copy(fontSize = 18.sp),
                        color = CC.textColor()
                    )
                }
                Row {
                    IconButton(
                        onClick = {
                            if (isEditing) {
                                isSaving = true
                                val updatedTimetable = timetable.copy(
                                    unitName = editedUnitName,
                                    venue = editedVenue,
                                    lecturer = editedLecturer,
                                    startTime = editedStartTime,
                                    endTime = editedEndTime
                                )
                                MyDatabase.editTimetable(updatedTimetable) { isSuccess ->
                                    isSaving = false
                                    if (isSuccess) {
                                        onEdit(updatedTimetable)
                                        isEditing = false
                                    } else {
                                        errorMessage = "Failed to save changes. Please try again."
                                    }
                                }
                            } else {
                                isEditing = true
                            }
                        }, modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = if (isEditing) "Save Event" else "Edit Event",
                            tint = CC.textColor()
                        )
                    }
                    if (isEditing) {
                        IconButton(
                            onClick = {
                                editedLecturer = timetable.lecturer
                                editedVenue = timetable.venue
                                editedEndTime = timetable.endTime
                                editedStartTime = timetable.startTime
                                editedUnitName = timetable.unitName

                                isEditing = false
                                errorMessage = null
                            }, modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel Edit",
                                tint = CC.primary()
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { onDelete(timetable.id) }, modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Event",
                                tint = CC.textColor()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                CustomOutlinedTextField(value = editedVenue,
                    label = "Venue",
                    onValueChange = { editedVenue = it },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Venue",
                            tint = CC.textColor()
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))
                CustomOutlinedTextField(value = editedLecturer,
                    label = "Lecturer",
                    onValueChange = { editedLecturer = it },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Lecturer",
                            tint = CC.textColor()
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))
                CustomOutlinedTextField(value = editedStartTime,
                    label = "Start Time",
                    onValueChange = { editedStartTime = it },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Start Time",
                            tint = CC.textColor()
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))
                CustomOutlinedTextField(value = editedEndTime,
                    label = "End Time",
                    onValueChange = { editedEndTime = it },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "End Time",
                            tint = CC.textColor()
                        )
                    })
            } else {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Venue",
                            tint = CC.textColor()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timetable.venue,
                            style = CC.descriptionTextStyle(context),
                            color = CC.textColor()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Lecturer",
                            tint = CC.textColor()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timetable.lecturer,
                            style = CC.descriptionTextStyle(context),
                            color = CC.textColor()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Time",
                            tint = CC.textColor()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${timetable.startTime} - ${timetable.endTime}",
                            style = CC.descriptionTextStyle(context),
                            color = CC.textColor()
                        )
                    }
                }
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

@Composable
fun CustomOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = CC.textColor(),
            unfocusedIndicatorColor = CC.tertiary(),
            focusedTextColor = CC.textColor(),
            unfocusedTextColor = CC.textColor(),
            focusedContainerColor = CC.primary(),
            unfocusedContainerColor = CC.primary(),
            focusedLabelColor = CC.textColor(),
            unfocusedLabelColor = CC.textColor()
        ),
        modifier = modifier
    )
}


@Preview
@Composable
fun TimetableScreenPreview() {
    //TimetableScreen(rememberNavController(), LocalContext.current)
    TimetableCard(timetable = Timetable(
        lecturer = "Michael", startTime = "11:30", endTime = "12:30", venue = "Here"
    ), {}, {}, LocalContext.current
    )

}