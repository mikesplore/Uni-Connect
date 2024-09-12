package com.mike.uniadmin.authentication

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.backEnd.notifications.NotificationEntity
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.getNotificationViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase
import com.mike.uniadmin.notification.showNotification
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreDetails(context: Context, navController: NavController) {

    val userViewModel = getUserViewModel(context)
    val notificationViewModel = getNotificationViewModel(context)

    var addLoading by remember { mutableStateOf(false) }
    val loggedInUser = FirebaseAuth.getInstance().currentUser
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val email = loggedInUser?.email
    val imageLink = if (loggedInUser?.photoUrl != null) loggedInUser.photoUrl else ""


    val brush = Brush.verticalGradient(
        colors = listOf(
            CC.primary(), CC.secondary()
        )
    )

    LaunchedEffect(Unit) {
        userViewModel.findUserByEmail(loggedInUser?.email!!) {}
    }

    Scaffold(topBar = {
        TopAppBar(title = {}, navigationIcon = {
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
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = CC.primary().copy(alpha = 0.2f))
        )
    }, containerColor = CC.primary()) {
        // main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .background(brush)
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 20.dp)
                    .height(100.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    "Details",
                    style = CC.titleTextStyle()
                        .copy(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CC.SingleLinedTextField(
                    value = firstName,
                    onValueChange = { first ->
                        firstName = first
                    },
                    label = "First name",
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))
                CC.SingleLinedTextField(
                    value = lastName,
                    onValueChange = { last ->
                        lastName = last

                    },
                    label = "Last name",
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (firstName.isEmpty() || lastName.isEmpty()) {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        else{
                        addLoading = true
                        MyDatabase.generateIndexNumber { userId ->
                            val newUser = UserEntity(
                                userType = "student",
                                id = userId,
                                email = email.toString(),
                                firstName = firstName,
                                lastName = lastName,
                                profileImageLink = imageLink.toString()
                            )
                            userViewModel.writeUser(newUser, onSuccess = {
                                MyDatabase.generateNotificationID { id ->
                                    notificationViewModel.writeNotification(
                                        notificationEntity = NotificationEntity(
                                            name = firstName,
                                            userId = userId,
                                            id = id,
                                            category = "New User",
                                            title = "$firstName Joined Uni Connect!",
                                            description = "Say hi and get the conversation started!",
                                            date = CC.getTimeStamp(),
                                            time = CC.getTimeStamp()
                                        )
                                    )
                                    notificationViewModel.fetchNotifications()
                                    showNotification(context, "Welcome", "Welcome to Uni Connect, $firstName!")
                                }
                                navController.navigate("courses") {
                                    popUpTo("moreDetails") {
                                        inclusive = true
                                    }
                                }
                                addLoading = false
                            })
                        }}

                    },
                    modifier = Modifier.width(275.dp),
                    colors = ButtonDefaults.buttonColors(CC.extraColor2()),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier, verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (addLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CC.textColor())
                        } else {
                            Text("Save details", style = CC.descriptionTextStyle())
                        }
                    }
                }
            }
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