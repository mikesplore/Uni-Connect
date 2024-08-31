package com.mike.uniadmin.assignments

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mike.uniadmin.backEnd.moduleContent.moduleAssignments.ModuleAssignment
import com.mike.uniadmin.getModuleAssignmentViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(context: Context) {

    val moduleViewModel = getModuleViewModel(context)
    val assignmentViewModel = getModuleAssignmentViewModel(context)

    val assignments by assignmentViewModel.assignments.observeAsState()
    val modules by moduleViewModel.modules.observeAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedModuleId by remember { mutableStateOf<String?>(null) }

    val isLoading by assignmentViewModel.isLoading.observeAsState()

    LaunchedEffect(Unit) {
        moduleViewModel.fetchModules()
    }

    LaunchedEffect(selectedTabIndex) {
        selectedModuleId = modules?.getOrNull(selectedTabIndex)?.moduleCode
        selectedModuleId?.let { assignmentViewModel.getModuleAssignments(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assignments", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(),
                    titleContentColor = CC.textColor()
                )
            )
        },
        containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                modules.isNullOrEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No modules available", style = CC.descriptionTextStyle(context))
                    }
                }
                isLoading == true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CC.textColor())
                    }
                }
                else -> {
                    modules?.let { moduleList ->
                        ScrollableTabRow(
                            containerColor = CC.primary(), selectedTabIndex = selectedTabIndex
                        ) {
                            moduleList.forEachIndexed { index, module ->
                                Tab(
                                    modifier = Modifier
                                        .height(40.dp)
                                        .background(if (selectedTabIndex == index) CC.secondary() else CC.primary(), RoundedCornerShape(8.dp)),
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            text = module.moduleName.take(10).plus(if (module.moduleName.length > 10) "..." else ""),
                                            style = CC.descriptionTextStyle(context).copy(
                                                color = if (selectedTabIndex == index) CC.textColor() else CC.textColor()
                                            )
                                        )
                                    }
                                )
                            }
                        }

                        assignments?.let { assignmentList ->
                            if (assignmentList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No assignments available", style = CC.descriptionTextStyle(context))
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn {
                                    items(assignmentList) { assignment ->
                                        AssignmentCard(assignment = assignment, context)
                                    }
                                }
                            }
                        } ?: run {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = CC.textColor())
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AssignmentCard(assignment: ModuleAssignment, context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(8.dp), colors = CardDefaults.cardColors(
            containerColor = CC.secondary()
        ), elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = assignment.title, style = CC.titleTextStyle(context)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = assignment.description, style = CC.descriptionTextStyle(context)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Due Date: ${assignment.dueDate}",
                style = CC.descriptionTextStyle(context),
                color = CC.textColor()
            )
        }
    }
}
