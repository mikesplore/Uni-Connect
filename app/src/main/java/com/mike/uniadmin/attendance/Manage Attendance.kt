package com.mike.uniadmin.attendance

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.model.modules.AttendanceState
import com.mike.uniadmin.model.modules.ModuleEntity
import com.mike.uniadmin.model.modules.ModuleViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAttendanceScreen(context: Context) {
    val moduleViewModel = getModuleViewModel(context)
    val modules by moduleViewModel.modules.observeAsState(emptyList())
    val attendanceStates by moduleViewModel.attendanceStates.observeAsState(emptyMap())
    var refresh by remember { mutableStateOf(false) }
    var showAddModule by remember { mutableStateOf(false) }

    LaunchedEffect(refresh) {
        moduleViewModel.fetchAttendanceStates()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = {
                        showAddModule = !showAddModule

                    }) {
                        Icon(
                            Icons.Default.Add, contentDescription = "Refresh", tint = CC.textColor()
                        )
                    }

                    IconButton(onClick = { refresh = !refresh }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = CC.textColor()
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary()
                ),
            )

        }, containerColor = CC.primary()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(CC.primary())
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Manage Attendance",
                    style = CC.titleTextStyle()
                        .copy(fontWeight = FontWeight.Bold, fontSize = 30.sp)
                )
            }

            AnimatedVisibility(visible = showAddModule) {
                AddModule(moduleViewModel) { success ->
                    showAddModule = false
                    if (success) {
                        Toast.makeText(context, "Module added successfully", Toast.LENGTH_SHORT)
                            .show()
                        refresh = !refresh
                    }
                }
            }

            if (modules.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Text("No modules found", style = CC.descriptionTextStyle())
                }
            } else {
                modules.forEach { module ->
                    val attendanceState = attendanceStates[module.moduleCode]
                    val isChecked = attendanceState?.state ?: false

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(vertical = 8.dp)
                            .background(CC.extraColor1(), shape = MaterialTheme.shapes.medium)
                            .padding(16.dp)
                            .animateContentSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            module.moduleName,
                            modifier = Modifier.weight(1f),
                            style = CC.descriptionTextStyle(),
                            fontSize = 18.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Switch(
                            checked = isChecked, onCheckedChange = { newState ->
                                val newAttendanceState = AttendanceState(
                                    moduleID = module.moduleCode,
                                    moduleName = module.moduleName,
                                    state = newState
                                )
                                moduleViewModel.saveAttendanceState(newAttendanceState)
                                refresh = !refresh
                            }, colors = SwitchDefaults.colors(
                                checkedThumbColor = CC.primary(),
                                checkedTrackColor = CC.secondary(),
                                uncheckedThumbColor = CC.secondary(),
                                uncheckedTrackColor = CC.primary()
                            )
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AddModule(
    moduleViewModel: ModuleViewModel,
    onModuleAdded: (Boolean) -> Unit = {}
) {
    val moduleCode = remember { mutableStateOf("") }
    val moduleName = remember { mutableStateOf("") }
    val moduleImageLink = remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add Module", style = CC.titleTextStyle())
        CC.CustomTextField(
            value = moduleCode.value,
            onValueChange = { moduleCode.value = it },
            label = "Module Code",
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        CC.CustomTextField(
            value = moduleName.value,
            onValueChange = { moduleName.value = it },
            label = "Module Name",
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))

        CC.CustomTextField(
            value = moduleImageLink.value,
            onValueChange = { moduleImageLink.value = it },
            label = "Module Image Link",
            singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val newModule = ModuleEntity(
                    moduleCode = moduleCode.value,
                    moduleName = moduleName.value,
                    moduleImageLink = moduleImageLink.value
                )
                moduleViewModel.saveModule(newModule) { success ->
                    if (success) {
                        onModuleAdded(true)
                    } else {
                        onModuleAdded(false)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.secondary(), contentColor = CC.primary()
            )
        ) {
            Text("Add Module", style = CC.descriptionTextStyle())
        }
    }
}