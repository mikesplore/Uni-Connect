package com.mike.uniadmin.programs

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
import androidx.compose.runtime.MutableState
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mike.uniadmin.R
import com.mike.uniadmin.backEnd.programs.ProgramEntity
import com.mike.uniadmin.backEnd.programs.ProgramViewModel
import com.mike.uniadmin.backEnd.programs.ProgramViewModelFactory
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.CommonComponents as CC

object ProgramCode {
    // Define the shared preferences key for storing the value
    private const val PREF_KEY_PROGRAM_CODE = "program_code_key"
    private lateinit var preferences: SharedPreferences

    // MutableState to hold the program code value
    val programCode: MutableState<String> = mutableStateOf("")

    // Function to initialize shared preferences and load the initial value
    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // Load the value from SharedPreferences when initializing
        programCode.value = preferences.getString(PREF_KEY_PROGRAM_CODE, "") ?: ""
        Log.d("ProgramCode", "Program code initialized: ${programCode.value}")
    }

    // Save the value to SharedPreferences whenever it changes
    fun saveProgramCode(newProgramCode: String) {
        programCode.value = newProgramCode
        preferences.edit().putString(PREF_KEY_PROGRAM_CODE, newProgramCode).apply()
        Log.d("ProgramCode", "Program code saved: $newProgramCode")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(context: Context, navController: NavController) {
    val application = context.applicationContext as UniAdmin
    val programRepository = remember { application.programRepository }
    val programViewModel: ProgramViewModel = viewModel(
        factory = ProgramViewModelFactory(programRepository)
    )
    val userRepository = remember { application.userRepository }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))

    val currentUser by userViewModel.user.observeAsState()
    val programs by programViewModel.programs.observeAsState(emptyList())
    val isLoading by programViewModel.isLoading.observeAsState(false)
    var showAddProgram by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.findUserByEmail(FirebaseAuth.getInstance().currentUser?.email ?: "") {}
    }

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
                AddProgram(context = context,
                    onProgramAdded = { newProgram ->
                        programViewModel.saveProgram(newProgram) { success ->
                            showAddProgram = false
                            if (success) {
                                Toast.makeText(
                                    context, "Program added successfully", Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "Failed to add program", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    })
            }
            if (programs?.isEmpty() == true) {
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
                    items(programs ?: emptyList()) { program ->
                        ProgramItem(
                            currentUser, program, context
                        ) {
                            if (!program.participants.contains(currentUser?.id)) {
                                currentUser?.id?.let { userId ->
                                    programViewModel.saveProgram(
                                        program.copy(participants = program.participants + userId)
                                    ) { onSuccess ->
                                        if (onSuccess) {
                                            Toast.makeText(
                                                context,
                                                "Program joined successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            //get the program code
                                            ProgramCode.saveProgramCode(program.programCode)
                                            if (ProgramCode.programCode.value.isNotEmpty()) {
                                                navController.navigate("homeScreen")
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "program code is empty",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Failed to join program",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                //get the program code
                                ProgramCode.saveProgramCode(program.programCode)
                                if (ProgramCode.programCode.value.isNotEmpty()) {
                                    navController.navigate("homeScreen")
                                } else {
                                    Toast.makeText(
                                        context,
                                        "program code is empty",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProgramItem(
    currentUser: UserEntity?,
    programEntity: ProgramEntity?,
    context: Context,
    onProgramClicked: () -> Unit = {}
) {
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
                    "Participants: ${programEntity?.participants?.size}",
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
                ),
                // enabled = programEntity?.participants?.contains(currentUser?.id) == false
            ) {
                if (programEntity?.participants?.contains(currentUser?.id) == true) {
                    Text(
                        "Open Program", style = CC.titleTextStyle(context).copy(
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    )
                } else {
                    Text(
                        "Join Program", style = CC.titleTextStyle(context).copy(
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun AddProgram(
    context: Context, onProgramAdded: (ProgramEntity) -> Unit
) {
    var programName by remember { mutableStateOf("") }
    var programImageLink by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }



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
                loading = true
                if (programName.isEmpty() || programImageLink.isEmpty()) {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    loading = false
                    return@Button
                }
                MyDatabase.generateProgramID { newID ->
                    val newProgram = ProgramEntity(
                        participants = emptyList(),
                        programCode = newID,
                        programName = programName,
                        programImageLink = programImageLink
                    )
                    onProgramAdded(newProgram)
                    loading = false
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
                CC.ColorProgressIndicator(modifier = Modifier.fillMaxWidth())
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




