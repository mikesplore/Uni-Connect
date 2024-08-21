package com.mike.uniadmin.authentication

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.backEnd.notifications.NotificationEntity
import com.mike.uniadmin.backEnd.notifications.NotificationViewModel
import com.mike.uniadmin.backEnd.users.SignedInUser
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.getNotificationViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.model.Details
import com.mike.uniadmin.model.Fcm
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.generateFCMID
import com.mike.uniadmin.model.MyDatabase.generateIndexNumber
import com.mike.uniadmin.model.MyDatabase.writeFcmToken
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, context: Context) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isSigningUp by remember { mutableStateOf(false) }
    var isGithubLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }

    val firebaseAuth = FirebaseAuth.getInstance()

    val userViewModel = getUserViewModel(context)
    val notificationViewModel = getNotificationViewModel(context)

    val brush = Brush.verticalGradient(
        colors = listOf(
            CC.primary(), CC.secondary()
        )
    )

    LaunchedEffect(Unit) {
        auth.currentUser?.email?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            userViewModel.findUserByEmail(it) {} }
        visible = true
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {}, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary()
                )
            )
        }, containerColor = CC.primary()
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .padding(it)
                .background(brush)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSigningUp) "Sign Up" else "Sign In",
                style = CC.titleTextStyle(context)
                    .copy(fontSize = 40.sp, fontWeight = FontWeight.Bold)
            )

            Column(
                modifier = Modifier
                    .width(350.dp)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Continue with one of the following options",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GoogleAuth(firebaseAuth = firebaseAuth, onSignInSuccess = {
                        handleAuthSuccess(navController, userViewModel)
                    }, onSignInFailure = { failure ->
                        Toast.makeText(context, "Sign-in failed: $failure", Toast.LENGTH_SHORT)
                            .show()
                        isGoogleLoading = false
                    })

                    GitAuth(firebaseAuth = firebaseAuth, onSignInSuccess = {
                        handleAuthSuccess(navController, userViewModel)
                    }, onSignInFailure = { failure ->
                        Toast.makeText(context, "Sign-in failed: $failure", Toast.LENGTH_SHORT)
                            .show()
                        isGithubLoading = false
                    })
                }

                Text(
                    text = "Or",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    if (isSigningUp) "Sign up with your email and password" else "Sign in with your email and password",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold)
                )

                AnimatedContent(targetState = isSigningUp, label = "") { targetState ->
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .imePadding()
                            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (targetState) {
                            Spacer(modifier = Modifier.height(20.dp))
                            CC.SingleLinedTextField(
                                value = firstName, onValueChange = { newValue ->
                                    firstName = newValue
                                }, label = "First Name", singleLine = true, context = context
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            CC.SingleLinedTextField(
                                value = lastName, onValueChange = { newValue ->
                                    lastName = newValue
                                }, label = "Last Name", singleLine = true, context = context
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        CC.SingleLinedTextField(
                            value = email, onValueChange = { newValue ->
                                email = newValue
                            }, label = "Email", singleLine = true, context = context
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        CC.PasswordTextField(
                            value = password, onValueChange = { newValue ->
                                password = newValue
                            }, label = "Password", context = context
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .border(
                        width = 1.dp, color = CC.textColor(), shape = RoundedCornerShape(10.dp)
                    )
                    .background(
                        CC.secondary(), shape = RoundedCornerShape(10.dp)
                    )
                    .height(50.dp)
                    .width(300.dp)
            ) {
                Button(
                    onClick = {
                        loading = true
                        if (isSigningUp) handleSignUp(
                            notificationViewModel,
                            context,
                            firebaseAuth,
                            firstName,
                            lastName,
                            email,
                            password,
                            userViewModel,


                            ) {
                            loading = false
                        } else handleSignIn(
                            context, firebaseAuth, email, password, navController, userViewModel
                        ) {
                            loading = false
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    colors = ButtonDefaults.buttonColors(Color.Transparent)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(30.dp),
                            color = CC.primary(),
                            trackColor = CC.textColor()
                        )
                    } else {
                        Text(
                            if (isSigningUp) "Sign Up" else "Sign In",
                            style = CC.descriptionTextStyle(context = context)
                                .copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(visible = !isSigningUp) {
                TextButton(onClick = { navController.navigate("passwordReset") }) {
                    Text(
                        text = "Forgot Password? Reset",
                        style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(onClick = { isSigningUp = !isSigningUp }) {
                AnimatedContent(
                    targetState = isSigningUp, transitionSpec = {
                        if (targetState) {
                            // Sign Up animation
                            (slideInHorizontally { width -> width } + fadeIn()) togetherWith (slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            // Sign In animation
                            (slideInHorizontally { width -> -width } + fadeIn()) togetherWith (slideOutHorizontally { width -> width } + fadeOut())
                        }.using(
                            SizeTransform(clip = false)
                        )
                    }, label = ""
                ) { targetIsSigningUp ->
                    if (targetIsSigningUp) {
                        Text(
                            text = "Already have an account? Sign In",
                            style = CC.descriptionTextStyle(context),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )

                    } else {
                        Text(
                            text = "Don't have an account? Sign Up ",
                            style = CC.descriptionTextStyle(context),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
            }
        }
    }
}


fun handleAuthSuccess(navController: NavController, userViewModel: UserViewModel) {
    userViewModel.findUserByEmail(FirebaseAuth.getInstance().currentUser?.email ?: "") { user ->
        if (user != null) {
            userViewModel.setSignedInUser(
                SignedInUser(
                    id = "userID", email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                )
            )
            navController.navigate("programs") {
                popUpTo("login") { inclusive = true }
            }
        } else {
            navController.navigate("moreDetails") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
}


fun handleSignUp(
    notificationViewModel: NotificationViewModel,
    context: Context,
    firebaseAuth: FirebaseAuth,
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    userViewModel: UserViewModel,
    onComplete: (Boolean) -> Unit,

    ) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                generateIndexNumber { userID ->
                    val newUser = UserEntity(
                        id = userID,
                        email = email,
                        firstName = firstName,
                        lastName = lastName,
                        profileImageLink = "",
                        phoneNumber = "",
                    )
                    val signedInUser = SignedInUser(id = "userID", email = email)
                    userViewModel.setSignedInUser(signedInUser)

                    userViewModel.writeUser(newUser) {
                        Toast.makeText(context, "Details saved!", Toast.LENGTH_SHORT).show()
                    }
                    MyDatabase.generateNotificationID { id ->
                        notificationViewModel.writeNotification(
                            notificationEntity = NotificationEntity(
                                category = "New User",
                                name = firstName,
                                userId = userID,
                                id = id,
                                title = "$firstName $lastName has Joined Uni Admin!",
                                description = "Start a conversation by sending  a 👋",
                                date = CC.getTimeStamp(),
                                time = CC.getTimeStamp()
                            )
                        )
                        notificationViewModel.fetchNotifications()
                    }
                }
                onComplete(true)

            } else {
                Toast.makeText(context, "Wrong credentials.", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    } else {
        onComplete(false)
        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
    }
}

fun handleSignIn(
    context: Context,
    firebaseAuth: FirebaseAuth,
    email: String,
    password: String,
    navController: NavController,
    userViewModel: UserViewModel,
    onComplete: (Boolean) -> Unit
) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Details.email.value = email
                firebaseAuth.currentUser?.email?.let { current ->
                    userViewModel.findUserByEmail(current) { user ->
                        if (user != null) {
                            navController.navigate("programs")
                            Toast.makeText(
                                context,
                                "Welcome back to UniAdmin, ${user.firstName}!",
                                Toast.LENGTH_SHORT
                            ).show()
                            val signedInUser = SignedInUser(id = "userID", email = email)
                            userViewModel.setSignedInUser(signedInUser)
                        } else {
                            Toast.makeText(context, "No user found", Toast.LENGTH_SHORT).show()
                            navController.navigate("moreDetails")
                        }
                    }
                }

                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { tokenTask ->
                    if (!tokenTask.isSuccessful) {
                        Log.w("FCM", "Fetching FCM registration token failed", tokenTask.exception)
                        return@OnCompleteListener
                    }
                    val token = tokenTask.result
                    generateFCMID { id ->
                        val fcmToken = Fcm(id = id, token = token)
                        writeFcmToken(token = fcmToken)
                    }
                })
                onComplete(true)
            } else {
                Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
    } else {
        onComplete(false)
        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
    }
}

@Preview
@Composable
fun SignInScreenPreview() {
    LoginScreen(rememberNavController(), LocalContext.current)
}
