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
import androidx.compose.foundation.layout.size
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
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignment
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
                title = {
                    Text("Assignments", style = CC.titleTextStyle().copy(fontWeight = FontWeight.ExtraBold))
                },
                actions = {

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
            modifier = Modifier.padding(it)
        ) {

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
