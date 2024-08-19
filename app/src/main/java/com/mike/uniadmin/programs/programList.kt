package com.mike.uniadmin.programs

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.mike.uniadmin.R
import com.mike.uniadmin.dataModel.programs.ProgramEntity
import com.mike.uniadmin.dataModel.programs.ProgramViewModel
import com.mike.uniadmin.dataModel.programs.ProgramViewModelFactory
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.moveNodesToNewParent
import com.mike.uniadmin.ui.theme.CommonComponents as CC



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(context: Context, navController: NavController) {
    val application = context.applicationContext as UniAdmin
    val programRepository = remember { application.programRepository }
    val programViewModel: ProgramViewModel = viewModel(
        factory = ProgramViewModelFactory(programRepository)
    )

    val programs by programViewModel.programs.observeAsState(emptyList())
    val isLoading by programViewModel.isLoading.observeAsState(false)
    var showAddProgram by remember { mutableStateOf(false) }

//    LaunchedEffect(Unit) {
//        moveNodesToNewParent()
//    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Programs", style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.Bold, fontSize = 24.sp
                    )
                )
            }, actions = {
                IconButton(onClick = { showAddProgram = !showAddProgram }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Program",
                        tint = CC.textColor()
                    )
                }
            },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary(), titleContentColor = CC.textColor()
                )
            )
        }, containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = showAddProgram) {
                AddProgram(
                    context = context,
                    programViewModel = programViewModel,
                    onProgramAdded = { newProgram ->
                        programViewModel.saveProgram(newProgram) { success ->
                            showAddProgram = false
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "Program added successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "Failed to add program", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    })
            }
            if (programs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No programs available", style = CC.titleTextStyle(context).copy(
                            fontWeight = FontWeight.Bold, fontSize = 24.sp
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(it)
                ) {
                    items(programs) { program ->
                        ProgramItem(program, context, navController, onProgramClicked = {
                            navController.navigate("program/${program.programCode}")

                        })
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramItem(programEntity: ProgramEntity?, context: Context, navController: NavController, onProgramClicked:() -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Add padding around the box
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp)) // Smoothen corners for the entire card
                .border(
                    1.dp, CC.secondary(), RoundedCornerShape(16.dp)
                ) // Add a border with rounded corners
                .background(CC.primary()) // Set a background color
                .padding(16.dp) // Inner padding for content
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
        ) {
            // Course Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(16.dp)) // Clip the image with rounded corners
            ) {
                AsyncImage(
                    model = programEntity?.programImageLink,
                    contentScale = ContentScale.Crop,
                    contentDescription = "Course Image",
                    placeholder = painterResource(R.drawable.logo),
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp)) // Add spacing between image and text

            // Course Title and Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                programEntity?.programName?.let {
                    Text(
                        it, style = CC.titleTextStyle(context).copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        ), modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp)) // Space between title and details

                Text(
                    "Participants: ${programEntity?.participants}",
                    style = CC.descriptionTextStyle(context).copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space before button

            // Open Program Button
            Button(
                onClick = {
                    onProgramClicked()

                    },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)), // Rounded button corners
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CC.secondary(), contentColor = CC.textColor()
                )
            ) {
                Text(
                    "Open Course", style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                )
            }
        }
    }
}


@Composable
fun AddProgram(
    context: Context,
    onProgramAdded: (ProgramEntity) -> Unit,
    programViewModel: ProgramViewModel
) {
    var programName by remember { mutableStateOf("") }
    var programImageLink by remember { mutableStateOf("") }
    val loading by programViewModel.isLoading.observeAsState(false)



    Column(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CC.secondary(), RoundedCornerShape(12.dp))
            .background(CC.primary()) // Optional: Set a background color
            .padding(16.dp) // Inner padding
            .fillMaxWidth(0.95f)
            .wrapContentHeight()
    ) {
        Text(
            text = "Add Program", style = CC.titleTextStyle(context).copy(
                fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center
            ), modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        CC.SingleLinedTextField(
            value = programName,
            onValueChange = { newText -> programName = newText },
            label = "Program Name",
            context = context,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CC.secondary(), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        )

        CC.SingleLinedTextField(
            value = programImageLink,
            onValueChange = { newText -> programImageLink = newText },
            label = "Program Image Link",
            context = context,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, CC.secondary(), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        )

        Button(
            onClick = {
                if (programName.isEmpty() || programImageLink.isEmpty()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                MyDatabase.generateProgramID { newID ->
                    val newProgram = ProgramEntity(
                        programCode = newID,
                        programName = programName,
                        programImageLink = programImageLink
                    )
                    onProgramAdded(newProgram)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp), // Add padding around the button
            colors = ButtonDefaults.buttonColors(
                containerColor = CC.secondary(), contentColor = CC.textColor()
            )
        ) {
            if (loading) {
                CC.ColorProgressIndicator(modifier = Modifier.fillMaxSize())
            } else {
                Text(
                    text = "Add Program", style = CC.titleTextStyle(context).copy(
                        fontWeight = FontWeight.Bold, fontSize = 16.sp
                    )
                )
            }
        }
    }
}




