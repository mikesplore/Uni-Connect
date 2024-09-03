package com.mike.uniadmin.attendance


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.attendance.AttendanceEntity
import com.mike.uniadmin.backEnd.attendance.AttendanceViewModel
import com.mike.uniadmin.backEnd.modules.ModuleEntity
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.getAttendanceViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import java.util.Locale
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignAttendance(context: Context) {
    val moduleViewModel = getModuleViewModel(context)
    val userViewModel = getUserViewModel(context)
    val attendanceViewModel = getAttendanceViewModel(context)

    val signedInUser by userViewModel.user.observeAsState()
    val modules by moduleViewModel.modules.observeAsState(emptyList())
    val attendanceState by moduleViewModel.attendanceStates.observeAsState()
    val isLoading by moduleViewModel.isLoading.observeAsState(false)


    var attendanceRecords by remember { mutableStateOf(emptyList<AttendanceEntity>()) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val email = UniAdminPreferences.userEmail.value
    val tabNames = modules.map { it.moduleCode }
    val moduleNames = modules.map { it.moduleName }
    var refresh by remember { mutableStateOf(false) }

    // Load persisted data and then fetch fresh data from ViewModel
    LaunchedEffect(Unit) {
        // Load persisted data first (for quick UI access)
        val (_, persistedAttendance) = loadDataFromPreferences(context)
        attendanceRecords = persistedAttendance

        // Fetch fresh data from ViewModel (actual source of truth)
        userViewModel.findUserByEmail(email) {}
        moduleViewModel.fetchModules() // This will update the modules in the ViewModel
        moduleViewModel.fetchAttendanceStates()

        // Once ViewModel fetches fresh data, persist it again
        saveDataToPreferences(context, modules, attendanceRecords)
    }

    // Fetch attendance records whenever a module is selected or refreshed
    LaunchedEffect(modules, refresh, selectedTabIndex) {
        if (signedInUser != null && modules.isNotEmpty()) {
            val currentModuleCode = tabNames.getOrNull(selectedTabIndex) ?: ""
            if (currentModuleCode.isNotEmpty()) {
                attendanceViewModel.getAttendanceForStudent(
                    signedInUser!!.id,
                    currentModuleCode
                ) { freshAttendanceRecords ->
                    attendanceRecords = freshAttendanceRecords // Update the attendance records
                    saveDataToPreferences(context, modules, attendanceRecords) // Persist to JSON
                }
            }
        }
    }

    // UI for SignAttendance
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Attendance", style = CC.titleTextStyle().copy(fontWeight = FontWeight.Bold)) },
                actions = {
                    IconButton(onClick = { refresh = !refresh }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary())
            )
        },
        containerColor = CC.secondary()
    ) {
        Column(modifier = Modifier
            .background(CC.primary())
            .fillMaxSize()
            .padding(it)) {
            // Full-screen loading indicator when data is being fetched
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Show Tabs and attendance records
                Tabs(
                    tabs = tabNames,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        selectedTabIndex = index
                        refresh = !refresh
                    }
                )

                moduleNames.forEachIndexed { index, name ->
                    if (index == selectedTabIndex) {
                        Box(modifier = Modifier
                            .background(CC.primary())
                            .fillMaxWidth(), contentAlignment = Alignment.Center){
                            Text(name, style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))


                // Sign attendance or show "not open" message
                val attendanceStateForModule =
                    attendanceState?.get(tabNames.getOrNull(selectedTabIndex))
                if (attendanceStateForModule?.state == true) {
                    SignAttendanceCard(
                        user = signedInUser,
                        attendanceViewModel = attendanceViewModel,
                        moduleCode = tabNames.getOrNull(selectedTabIndex),
                        onSignAttendance = {success ->
                            if (success){
                                refresh = !refresh
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Attendance is not open for this module.",
                            style = CC.descriptionTextStyle()
                        )
                    }
                }

                // Filter attendance records for selected module
                val filteredAttendanceRecords = attendanceRecords.filter { attendance ->
                    attendance.moduleId == tabNames.getOrNull(selectedTabIndex)
                }

                // Display attendance records in a LazyColumn
                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    items(filteredAttendanceRecords) { attendanceRecord ->
                        AttendanceRecordCard(attendanceRecord)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

fun saveDataToPreferences(
    context: Context,
    modules: List<ModuleEntity>,
    attendance: List<AttendanceEntity>
) {
    val sharedPreferences = context.getSharedPreferences("UniAdminPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    val modulesJson = Gson().toJson(modules)
    val attendanceJson = Gson().toJson(attendance)
    editor.putString("modules", modulesJson)
    editor.putString("attendanceRecords", attendanceJson)
    editor.apply()
}


@Composable
fun SignAttendanceCard(
    user: UserEntity?,
    moduleCode: String?,
    attendanceViewModel: AttendanceViewModel,
    onSignAttendance: (Boolean) -> Unit
) {
    var loading by remember { mutableStateOf(false) }
    val attendance by attendanceViewModel.attendance.observeAsState(emptyList())

    // Get today's date
    val todayDate = CC.getDateFromTimeStamp(CC.getTimeStamp())

    // Filter attendance records for the current user and module, and check if the date matches today's date
    val hasSignedToday = attendance.any { record ->
        record.studentId == user?.id && record.moduleId == moduleCode && CC.getDateFromTimeStamp(record.date) == todayDate
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CC.extraColor1())
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (hasSignedToday) {
                Text(
                    text = "Good job! You have already signed today",
                    style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold, color = CC.textColor().copy(alpha = 0.8f)),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

            }else{
            Text(
                text = "Sign attendance for today",
                style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold)
            )}
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    // Handle attendance sign logic
                    MyDatabase.generateAttendanceID { id ->
                        val newAttendance = AttendanceEntity(
                            id = id,
                            studentId = user?.id ?: "",
                            moduleId = moduleCode ?: "",
                            date = CC.getTimeStamp(),
                            record = "signed"
                        )
                        attendanceViewModel.signAttendance(newAttendance){ success ->
                            loading = false
                            if (success) {
                                onSignAttendance(true)
                            }else{
                                onSignAttendance(false)
                            }

                        }
                    }

                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !hasSignedToday // Disable button if the user has already signed today
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = CC.textColor(),
                        strokeWidth = 1.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    if (hasSignedToday) {
                        Text("👍", style = CC.descriptionTextStyle())
                        return@Button
                    }
                    Text("Sign Attendance", style = CC.descriptionTextStyle())
                }
            }
        }
    }
}


@Composable
fun AttendanceRecordCard(attendance: AttendanceEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), // Add padding around the card
        colors = CardDefaults.cardColors(containerColor = CC.extraColor1()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Reduce elevation
        shape = RoundedCornerShape(8.dp) // Add rounded corners
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Add an icon or image (optional)
            Icon(
                imageVector = if (attendance.record == "signed") Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                contentDescription = "Attendance status",
                tint = if (attendance.record == "signed") Color.Green else Color.Red
            )

            Spacer(modifier = Modifier.width(16.dp)) // Add space between icon and text

            Column {
                Text(
                    text = CC.getDateFromTimeStamp(attendance.date),
                    style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = attendance.record.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    },
                    style = CC.descriptionTextStyle().copy(color = Color.Gray)
                )
            }
        }
    }
}

@Composable
fun Tabs(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    BoxWithConstraints {
        val screenWidth = maxWidth
        val boxWidth = screenWidth * 0.28f
        val density = LocalDensity.current
        val textSize = with(density) { (screenWidth * 0.03f).toSp() }

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = CC.primary()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier
                        .height(boxWidth * 0.3f)
                        .width(boxWidth)
                        .background(
                            if (selectedTabIndex == index) CC.tertiary() else CC.primary(),
                            RoundedCornerShape(10.dp)
                        ),
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            style = CC.descriptionTextStyle().copy(
                                color = if (selectedTabIndex == index) CC.textColor() else CC.extraColor2(),
                                fontSize = textSize
                            ),
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                )
            }
        }
    }
}


// Load data from JSON
fun loadDataFromPreferences(context: Context): Pair<List<ModuleEntity>, List<AttendanceEntity>> {
    val sharedPreferences = context.getSharedPreferences("UniAdminPrefs", Context.MODE_PRIVATE)
    val modulesJson = sharedPreferences.getString("modules", "[]")
    val attendanceJson = sharedPreferences.getString("attendanceRecords", "[]")

    val modules = Gson().fromJson(modulesJson, Array<ModuleEntity>::class.java).toList()
    val attendance = Gson().fromJson(attendanceJson, Array<AttendanceEntity>::class.java).toList()

    return Pair(modules, attendance)
}

fun deleteDataFromPreferences(context: Context) {
    val sharedPreferences = context.getSharedPreferences("UniAdminPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.remove("modules") // Remove modules data
    editor.remove("attendanceRecords") // Remove attendance data
    editor.apply()
}