package com.mike.uniadmin.assignments

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mike.uniadmin.getModuleAssignmentViewModel
import com.mike.uniadmin.getModuleViewModel
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignment
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleAssignments(context: Context, navController: NavController, moduleCode: String){
    val moduleAssignmentViewModel = getModuleAssignmentViewModel(context)
    val moduleViewModel = getModuleViewModel(context)
    val module by moduleViewModel.fetchedModule.observeAsState()
    val moduleAssignments by moduleAssignmentViewModel.assignments.observeAsState()

    LaunchedEffect(moduleCode) {
        moduleViewModel.fetchModules()
        moduleAssignmentViewModel.getModuleAssignments(moduleCode)
        moduleViewModel.getModuleDetailsByModuleID(moduleCode)

    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(),
                    titleContentColor = CC.textColor()
                )
            )
        },
        containerColor = CC.primary()
    ){
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier

                    .heightIn(min = 90.dp)
                    .fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val moduleName = module?.moduleName
                if (moduleName != null) {
                    Text(moduleName, style = CC.titleTextStyle().copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
                }
                    Text("${moduleAssignments?.size} assignment(s) posted.", style = CC.descriptionTextStyle().copy(color = CC.textColor().copy(0.5f)))

            }
            // Assignments list
            if (moduleAssignments.isNullOrEmpty()){
                Text("No assignments posted yet.", style = CC.descriptionTextStyle().copy(color = CC.textColor().copy(0.8f)))
                }else{
                AssignmentsCardList(moduleAssignment = moduleAssignments!!)
            }
        }
    }

}



@Composable
fun AssignmentCard(assignment: ModuleAssignment) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(vertical = 8.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = CC.surfaceContainer()  // Light elegant background
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Title with bold font and larger size
            Text(
                text = assignment.title,
                style = CC.titleTextStyle().copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = 0.15.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description with normal text styling
            Text(
                text = assignment.description,
                style = CC.descriptionTextStyle().copy(
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Due date with icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday, // Calendar icon resource
                    contentDescription = "Due date",
                    modifier = Modifier.size(16.dp),
                    tint = CC.secondary()  // Muted secondary color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Due: ${assignment.dueDate}",
                    style = CC.descriptionTextStyle().copy(
                        color = CC.textColor().copy(0.5f)  // Subtle text color
                    )
                )
            }
        }
    }
}


@Composable
fun AssignmentsCardList(moduleAssignment: List<ModuleAssignment>){
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
    ) {
        items(moduleAssignment) { assignment ->
            AssignmentCard(assignment)
        }
    }
}