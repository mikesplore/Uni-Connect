package com.mike.uniadmin.authentication

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.mike.uniadmin.dataModel.users.User
import com.mike.uniadmin.dataModel.users.UserRepository
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.ui.theme.GlobalColors
import com.mike.uniadmin.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreDetails(context: Context, navController: NavController) {
    val userRepository = remember { UserRepository() }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))

    var addloading by remember { mutableStateOf(false) }
    val loggedInUser = FirebaseAuth.getInstance().currentUser
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val email  = loggedInUser?.email
    val imageLink = loggedInUser?.photoUrl


    val brush  = Brush.verticalGradient(
        colors = listOf(
            CC.primary(),
            CC.secondary()
        )
    )

    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        userViewModel.findUserByEmail(loggedInUser?.email!!) {}

        }

    Scaffold(topBar = {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(
                    onClick = { navController.navigate("login") },
                    modifier = Modifier.absolutePadding(left = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = CC.textColor(),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary())
        )
    }, containerColor = CC.primary()) {
        // main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Row(modifier = Modifier
                    .padding(start = 20.dp)
                    .height(100.dp)
                    .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start) {
                    Text("Details", style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp))
                }

                Column(modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center) {
                CC.SingleLinedTextField(
                    value = firstName,
                    onValueChange = { first ->
                        firstName = first
                    },
                    label = "First name",
                    context = context,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))
                CC.SingleLinedTextField(
                    value = lastName,
                    onValueChange = { last ->
                        lastName = last

                    },
                    label = "Last name",
                    context = context,
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        addloading = true
                        MyDatabase.generateIndexNumber { id ->
                            val newUser = User(
                                id = id,
                                email = email.toString(),
                                firstName = firstName,
                                lastName = lastName,
                                profileImageLink = imageLink.toString()
                            )
                            userViewModel.writeUser(
                                newUser,
                                onSuccess = {
                                    addloading = false
                                    navController.navigate("homescreen")
                                }
                            )
                        }

                    }, modifier = Modifier
                        .width(275.dp),
                    colors = ButtonDefaults.buttonColors(CC.extraColor2()),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (addloading) {
                            CircularProgressIndicator(
                                color = CC.secondary(),
                                trackColor = CC.textColor()
                            )
                        } else {
                            Text("Save details")
                        }
                    }
                }}

        }

    }
}

@Preview
@Composable
fun Extra() {
    MoreDetails(
        navController = rememberNavController(), context = LocalContext.current
    )
}