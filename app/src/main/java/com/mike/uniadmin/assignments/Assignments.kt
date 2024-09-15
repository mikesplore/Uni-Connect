package com.mike.uniadmin.assignments

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mike.uniadmin.getModuleAssignmentViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignment
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignmentViewModel
import com.mike.uniadmin.model.modules.ModuleEntity
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(context: Context) {

    val moduleViewModel = getModuleViewModel(context)
    val assignmentViewModel = getModuleAssignmentViewModel(context)
    val assignments by assignmentViewModel.assignments.observeAsState()
    val modules by moduleViewModel.modules.observeAsState()
    val isLoading by assignmentViewModel.isLoading.observeAsState()
    var toggleLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        moduleViewModel.fetchModules()
    }
    LaunchedEffect(toggleLoading) {
        modules?.forEach { module ->
            assignmentViewModel.getModuleAssignments(module.moduleCode)
        }
    }

    val dateToday = CC.getDateFromTimeStamp(CC.getTimeStamp())
    val assignmentsDueToday = assignments?.filter { assignment ->
        assignment.dueDate.contains(dateToday)
    }
    val assignmentsCount = assignmentsDueToday?.size ?: 0


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Assignments",
                        style = CC.titleTextStyle().copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                actions = {
                    IconButton(onClick = {toggleLoading = !toggleLoading }) {
                        Icon(Icons.Default.Refresh,null,
                            tint = CC.extraColor2())
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(),
                    titleContentColor = CC.textColor()
                )
            )
        },
        containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier.padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "$assignmentsCount assignment(s) due today ($dateToday)",
                style = CC.descriptionTextStyle().copy(fontSize = 12.sp, color = CC.secondary())
            )

            modules?.let { modules -> isLoading?.let { it1 ->
                ModuleList(modules, assignmentViewModel,
                    it1
                )
            } }
        }
    }
}


@Composable
fun AssignmentCard(assignment: ModuleAssignment) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(8.dp), colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ), elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = assignment.title, style = CC.titleTextStyle()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = assignment.description, style = CC.descriptionTextStyle()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Due Date: ${assignment.dueDate}",
                style = CC.descriptionTextStyle(),
                color = CC.textColor()
            )
        }
    }
}


@Composable
fun ModuleCard(module: ModuleEntity, moduleAssignmentViewModel: ModuleAssignmentViewModel, loading: Boolean) {
    val moduleAssignment = moduleAssignmentViewModel.assignments.observeAsState()
    var assignmentsCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(module) {
        moduleAssignmentViewModel.getModuleAssignments(module.moduleCode)
        assignmentsCount = moduleAssignment.value?.size ?: 0
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.primary()
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (loading) CC.extraColor2() else CC.secondary(),
                            shape = CircleShape // Use CircleShape for a circular indicator
                        )
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = module.moduleName,
                        style = CC.descriptionTextStyle().copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "$assignmentsCount Assignments Posted",
                        style = CC.descriptionTextStyle()
                            .copy(color = CC.textColor().copy(alpha = 0.5f))
                    )
                }
                Spacer(modifier = Modifier.width(16.dp)) // Add space before the button
                OutlinedButton(
                    onClick = { /* TODO: Handle button click */ },
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                    border = null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (loading) CC.primary() else CC.secondary())
                ) {
                    Text(
                        "Open",
                        style = CC.descriptionTextStyle()
                            .copy(fontSize = 11.sp, color = CC.primary())
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleList(modules: List<ModuleEntity>, assignmentViewModel: ModuleAssignmentViewModel, loading: Boolean) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(modules) { module ->
            ModuleCard(module, assignmentViewModel, loading)
        }
    }
}