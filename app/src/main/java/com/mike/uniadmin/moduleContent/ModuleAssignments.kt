package com.mike.uniadmin.moduleContent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mike.uniadmin.backEnd.moduleContent.moduleAssignments.ModuleAssignment
import com.mike.uniadmin.backEnd.moduleContent.moduleAssignments.ModuleAssignmentViewModel
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.ui.theme.CommonComponents as CC
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AssignmentsItem(
    moduleID: String, assignmentViewModel: ModuleAssignmentViewModel, context: Context, userViewModel: UserViewModel
) {
    var expanded by remember { mutableStateOf(false) }
    val assignment = assignmentViewModel.assignments.observeAsState(initial = emptyList())


    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .background(CC.tertiary().copy(0.1f), RoundedCornerShape(10.dp))
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { assignmentViewModel.getModuleAssignments(moduleID) },

                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = background,
                    )) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = CC.textColor()
                    )
                }
                Text("${assignment.value.size} assignments", style = CC.descriptionTextStyle(context))
                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(35.dp),
                    containerColor = background,
                    contentColor = CC.textColor()
                ) {
                    Icon(Icons.Default.Add, "Add assignment")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            AnimatedVisibility(visible = expanded) {
                AddAssignmentItem(moduleID,
                    assignmentViewModel,
                    userViewModel,
                    context,
                    expanded,
                    onExpandedChange = { expanded = !expanded })
            }
            //assignmentCard
            if (assignment.value.isEmpty()) {
                Text("No Assignments", style = CC.descriptionTextStyle(context))
            } else {
                LazyColumn {
                    items(assignment.value) { assignment ->
                        AssignmentCard(assignment, context, userViewModel)
                    }
                }
            }
        }
    }
}


@Composable
fun AssignmentCard(
    assignment: ModuleAssignment, context: Context, userViewModel: UserViewModel
) {
    var senderName by remember { mutableStateOf("") }
    var profileImageLink by remember { mutableStateOf("") }
    val currentDate = Date()
    val formatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()) // Use default locale
    val formattedDate = formatter.format(currentDate)

    LaunchedEffect (Unit){
        userViewModel.findUserByAdmissionNumber(assignment.authorID){ user ->
        if (user != null) {
            senderName = user.firstName
            profileImageLink = user.profileImageLink
        }}
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CC.secondary()),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = profileImageLink,
                    contentDescription = "Sender Profile Image",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = senderName,
                        style = CC.descriptionTextStyle(context)
                            .copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = assignment.publishedDate,
                        style = CC.descriptionTextStyle(context)
                            .copy(fontSize = 12.sp, color = CC.textColor().copy(0.7f))
                    )
                }
            }
            Text(
                text = assignment.title,
                style = CC.titleTextStyle(context)
            )
            Text(
                text = assignment.description,
                style = CC.descriptionTextStyle(context)
                    .copy(color = CC.textColor().copy(0.8f))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (assignment.dueDate < formattedDate) "Past Due"
                    else if (assignment.dueDate == formattedDate) "Due Today"
                    else "Due: ${assignment.dueDate}",
                    fontSize = 12.sp,
                    style = CC.descriptionTextStyle(context),
                    color = if (assignment.dueDate < formattedDate) Color.Red else CC.textColor()
                )
            }
        }
    }
}

@Composable
fun AddAssignmentItem(
    moduleID: String,
    assignmentViewModel: ModuleAssignmentViewModel,
    userViewModel: UserViewModel,
    context: Context,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val user by userViewModel.signedInUser.observeAsState()
    var senderName by remember { mutableStateOf("") }
    var profileImageLink by remember { mutableStateOf("") }
    var currentUser by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        userViewModel.getSignedInUser()
        user?.let { signedInUser ->
            signedInUser.email.let {
                userViewModel.findUserByEmail(it) { fetchedUser ->
                    senderName = fetchedUser!!.firstName
                    profileImageLink = fetchedUser.profileImageLink
                    currentUser = fetchedUser.id
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .border(
                1.dp, CC.secondary(), RoundedCornerShape(10.dp)
            )
            .imePadding()
            .fillMaxWidth(0.9f)
            .padding(16.dp)
    ) {
        // Sender Information
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            AsyncImage(
                model = profileImageLink,
                contentDescription = "Sender Profile Image",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = senderName,
                style = CC.descriptionTextStyle(context).copy(
                    fontWeight = FontWeight.Bold, fontSize = 18.sp
                )
            )
        }

        AddTextField(
            label = "Title", value = title, onValueChange = { title = it }, context = context
        )
        Spacer(modifier = Modifier.height(10.dp))
        AddTextField(
            label = "Description",
            value = description,
            onValueChange = { description = it },
            context = context,
            singleLine = false,
            maxLines = 20
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Date Picker
        Button(
            onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        dueDate = dateFormatter.format(calendar.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CC.extraColor1())
        ) {
            Text(
                text = if (dueDate.isEmpty()) "Pick Date" else "Date: $dueDate",
                style = CC.descriptionTextStyle(context)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Time Picker
        Button(
            onClick = {
                TimePickerDialog(
                    context, { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        dueTime = timeFormatter.format(calendar.time)
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
                ).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = CC.extraColor1())
        ) {
            Text(
                text = if (dueTime.isEmpty()) "Pick Time" else "Time: $dueTime",
                style = CC.descriptionTextStyle(context)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    if (title.isEmpty() || description.isEmpty() || dueDate.isEmpty() || dueTime.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    // Save assignment
                    loading = true
                    val completeDueDate = "$dueDate at $dueTime"
                    MyDatabase.generateAssignmentID { iD ->
                        val newAssignment = ModuleAssignment(
                            authorID = currentUser,
                            moduleCode = moduleID,
                            assignmentID = iD,
                            title = title,
                            description = description,
                            dueDate = completeDueDate,
                            publishedDate = CC.getCurrentDate(CC.getTimeStamp())
                        )
                        assignmentViewModel.saveModuleAssignment(
                            moduleID = moduleID,
                            assignment = newAssignment,
                            onComplete = { success ->
                                if (success) {
                                    assignmentViewModel.getModuleAssignments(moduleID)
                                    loading = false
                                    onExpandedChange(expanded)
                                } else {
                                    loading = false
                                    onExpandedChange(expanded)
                                    Log.e("Error", "Failed to save assignment")
                                    Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CC.extraColor2()),
                modifier = Modifier.width(100.dp)
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
                onClick = { onExpandedChange(expanded) },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CC.extraColor2())
            ) {
                Text("Cancel", style = CC.descriptionTextStyle(context))
            }
        }
    }
}
