package com.mike.uniadmin.attendance


import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.attendance.AttendanceEntity
import com.mike.uniadmin.backEnd.attendance.AttendanceViewModel
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.getAttendanceViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignAttendance(context: Context) {
    val moduleViewModel = getModuleViewModel(context)
    val userViewModel = getUserViewModel(context)
    val attendanceViewModel = getAttendanceViewModel(context)

    val attendanceState by moduleViewModel.attendanceStates.observeAsState()
    val signedInUser by userViewModel.user.observeAsState()
    val modules by moduleViewModel.modules.observeAsState()
    val attendanceRecords by attendanceViewModel.attendance.observeAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabNames = modules?.map { it.moduleCode } ?: emptyList()
    val tabsLoading by moduleViewModel.isLoading.observeAsState()
    val email = UniAdminPreferences.userEmail.value

    LaunchedEffect(email) {
        userViewModel.findUserByEmail(email){}
        moduleViewModel.fetchModules()
        Log.d("SignAttendance", "Fetching modules: $modules")

        Log.d("SignAttendance", "Fetching attendance for student: ${signedInUser?.id}")
        tabNames.forEach { tabName ->
            attendanceViewModel.getAttendanceForStudent(signedInUser?.id ?: "", tabName)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Attendance", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp )) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.secondary()
                )
            )
        },
        containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // Tabs for selecting modules
            if (tabsLoading == true) {
                CC.ColorProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp))
            } else if (tabNames.isEmpty()) {
                Text("No modules found", style = CC.descriptionTextStyle(context))
            } else {
                Tabs(
                    context = context,
                    tabs = tabNames,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        selectedTabIndex = index
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Attendance Card
            SignAttendanceCard(
                user = signedInUser,
                attendanceViewModel = attendanceViewModel,
                context = context,
                moduleCode = tabNames.getOrNull(selectedTabIndex))

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance Records LazyColumn
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(attendanceRecords ?: emptyList()) { attendanceRecord ->
                    AttendanceRecordCard(attendanceRecord, context)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SignAttendanceCard(user: UserEntity?, context: Context, moduleCode: String?, attendanceViewModel: AttendanceViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { /* Trigger attendance signing logic */ },
        colors = CardDefaults.cardColors(containerColor = CC.extraColor1())
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sign Attendance for ${moduleCode ?: "Unknown"}",
                style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    // Handle attendance sign logic
                    MyDatabase.generateAttendanceID { id ->
                    val attendance = AttendanceEntity(
                        id = id,
                        studentId = user?.id ?: "",
                        moduleId = moduleCode ?: "",
                        date = CC.getTimeStamp(),
                        isPresent = true
                    )
                    attendanceViewModel.signAttendance(attendance)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Attendance")
            }
        }
    }
}

@Composable
fun AttendanceRecordCard(attendance: AttendanceEntity, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CC.extraColor1())
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Course: ${attendance.moduleId}",
                style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Date: ${attendance.date}",
                style = CC.descriptionTextStyle(context)
            )
            Text(
                text = "Status: ${if (attendance.isPresent) "Present" else "Absent"}",
                style = CC.descriptionTextStyle(context)
            )
        }
    }
}



@Composable
fun Tabs(
    context: Context,
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    BoxWithConstraints {
        val screenWidth = maxWidth
        val boxWidth = screenWidth * 0.3f
        val density = LocalDensity.current
        val textSize = with(density) { (screenWidth * 0.03f).toSp() }

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = CC.secondary()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier
                        .height(boxWidth * 0.35f)
                        .width(boxWidth)
                        .background(
                            if (selectedTabIndex == index) CC.primary() else CC.secondary(),
                            RoundedCornerShape(20.dp)
                        ),
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            style = CC.descriptionTextStyle(context).copy(
                                color = if (selectedTabIndex == index) CC.textColor() else CC.primary(),
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