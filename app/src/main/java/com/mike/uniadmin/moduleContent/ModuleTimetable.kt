package com.mike.uniadmin.moduleContent

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetable
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetableViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun TimetableItem(
    moduleID: String, timetableViewModel: ModuleTimetableViewModel, context: Context
) {
    var expanded by remember { mutableStateOf(false) }
    val timetables = timetableViewModel.timetables.observeAsState(initial = emptyList())
    val userType = UniAdminPreferences.userType.value

    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .background(
                    CC
                        .tertiary()
                        .copy(0.1f), RoundedCornerShape(10.dp)
                )
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { timetableViewModel.getModuleTimetables(moduleID) },

                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = background,
                )
            ) {
                Icon(
                    Icons.Default.Refresh, contentDescription = "Refresh", tint = CC.textColor()
                )
            }
            Text("${timetables.value.size} timetables", style = CC.descriptionTextStyle(context).copy(textAlign = TextAlign.Center),
                modifier = Modifier.weight(1f))

            if (userType == "admin"){
            FloatingActionButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(35.dp),
                containerColor = background,
                contentColor = CC.textColor()
            ) {
                Icon(Icons.Default.Add, "Add timetable")
            }}
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = expanded) {
                AddTimetableItem(moduleID,
                    timetableViewModel,
                    context,
                    onExpandedChange = { expanded = !expanded })
            }
            //timetable card
            if (timetables.value.isEmpty()) {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                ){
                    Text("No Timetables",
                        style = CC.descriptionTextStyle(context),
                        modifier = Modifier.wrapContentSize(Alignment.Center)
                    )}
            } else {
                LazyColumn {
                    items(timetables.value) { timetable ->
                        Text("${timetable.day}s", style = CC.titleTextStyle(context))
                        Spacer(modifier = Modifier.height(20.dp))
                        TimetableCard(timetable, context)
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableCard(
    timetable: ModuleTimetable, context: Context
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = "Start time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Start: ${timetable.startTime}", style = CC.descriptionTextStyle(context)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = "End time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "End: ${timetable.endTime}", style = CC.descriptionTextStyle(context)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Start time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Venue: ${timetable.venue}", style = CC.descriptionTextStyle(context)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Start time Icon",
                    tint = CC.textColor(),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Lecturer: ${timetable.lecturer}",
                    style = CC.descriptionTextStyle(context)
                )
            }
        }
    }
}

@Composable
fun AddTimetableItem(
    moduleID: String,
    timetableViewModel: ModuleTimetableViewModel,
    context: Context,
    onExpandedChange: (Boolean) -> Unit
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var day by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .imePadding()
            .fillMaxWidth(0.95f)
            .border(
                width = 2.dp,
                color = CC.extraColor2().copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Add Timetable Item",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = CC.textColor()
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
        )

        AddTextField(
            label = "Day", value = day, onValueChange = { day = it }, context = context,
        )

        Spacer(modifier = Modifier.height(10.dp))

        AddTextField(
            label = "Start Time", value = startTime, onValueChange = { startTime = it }, context = context,

        )

        Spacer(modifier = Modifier.height(10.dp))

        AddTextField(
            label = "End Time", value = endTime, onValueChange = { endTime = it }, context = context,

        )

        Spacer(modifier = Modifier.height(10.dp))

        AddTextField(
            label = "Venue", value = venue, onValueChange = { venue = it }, context = context,

        )

        Spacer(modifier = Modifier.height(10.dp))

        AddTextField(
            label = "Lecturer", value = lecturer, onValueChange = { lecturer = it }, context = context,

        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (day.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || venue.isEmpty() || lecturer.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    loading = true
                    MyDatabase.generateTimetableID { iD ->
                        val newTimetable = ModuleTimetable(
                            day = day,
                            moduleID = moduleID,
                            timetableID = iD,
                            startTime = startTime,
                            endTime = endTime,
                            venue = venue,
                            lecturer = lecturer
                        )
                        timetableViewModel.saveModuleTimetable(
                            moduleID = moduleID,
                            timetable = newTimetable,
                            onCompletion = { success ->
                                loading = false
                                if (success) {
                                    timetableViewModel.getModuleTimetables(moduleID)
                                    onExpandedChange(false)
                                    day = ""
                                    startTime = ""
                                    endTime = ""
                                    venue = ""
                                    lecturer = ""
                                } else {
                                    Toast.makeText(context, "Failed to post", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                ),
                modifier = Modifier.width(120.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = CC.textColor(), modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Post", style = CC.descriptionTextStyle(context))
                }
            }

            Button(
                onClick = { onExpandedChange(false) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2()
                ),
                modifier = Modifier.width(120.dp)
            ) {
                Text("Cancel", style = CC.descriptionTextStyle(context))
            }
        }
    }
}
