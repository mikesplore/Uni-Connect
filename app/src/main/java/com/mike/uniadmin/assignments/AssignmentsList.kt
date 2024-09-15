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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
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
import androidx.navigation.NavController
import com.mike.uniadmin.getModuleAssignmentViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignmentViewModel
import com.mike.uniadmin.model.modules.ModuleEntity
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(context: Context, navController: NavController) {

    val moduleViewModel = getModuleViewModel(context)
    val assignmentViewModel = getModuleAssignmentViewModel(context)
    val modules by moduleViewModel.modules.observeAsState()
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        moduleViewModel.fetchModules()
    }
    LaunchedEffect(isLoading) {
        modules?.forEach { module ->
            assignmentViewModel.getModuleAssignments(module.moduleCode)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Assignments",
                        style = CC.titleTextStyle().copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {navController.navigate("homeScreen")}) {
                        Icon(
                            Icons.Default.ArrowBackIosNew, null,
                            tint = CC.textColor()
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isLoading = !isLoading }) {
                        Icon(
                            Icons.Default.Refresh, null,
                            tint = CC.extraColor2()
                        )
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
            modules?.let { modules ->
                ModuleList(
                    modules, assignmentViewModel,
                    isLoading, navController
                )
            }
        }
    }
}


@Composable
fun ModuleCard(
    module: ModuleEntity,
    moduleAssignmentViewModel: ModuleAssignmentViewModel,
    loading: Boolean,
    navController: NavController
) {
    val moduleAssignment = moduleAssignmentViewModel.assignments.observeAsState()
    var assignmentsCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(module) {
        moduleAssignmentViewModel.getModuleAssignments(module.moduleCode)
        assignmentsCount = moduleAssignment.value?.size ?: 0
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = CC.surfaceContainer()
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
                        text = "Code: ${module.moduleCode}",
                        style = CC.descriptionTextStyle()
                            .copy(color = CC.textColor().copy(alpha = 0.5f))
                    )
                }
                Spacer(modifier = Modifier.width(16.dp)) // Add space before the button
                OutlinedButton(
                    onClick = { navController.navigate("moduleAssignments/${module.moduleCode}") },
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                    border = null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (loading) CC.primary() else CC.secondary()
                    )
                ) {
                    Text(
                        "View",
                        style = CC.descriptionTextStyle()
                            .copy(fontSize = 11.sp, color = CC.surfaceContainer())
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleList(
    modules: List<ModuleEntity>,
    assignmentViewModel: ModuleAssignmentViewModel,
    loading: Boolean,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(modules) { module ->
            ModuleCard(module, assignmentViewModel, loading, navController)
        }
    }
}