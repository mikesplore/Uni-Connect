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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.getUserViewModel
import com.mike.uniadmin.helperFunctions.Details
import com.mike.uniadmin.helperFunctions.Fcm
import com.mike.uniadmin.helperFunctions.MyDatabase.generateFCMID
import com.mike.uniadmin.helperFunctions.MyDatabase.writeFcmToken
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
    var loginfailed by remember { mutableStateOf(false) }
    var registerfailed by remember { mutableStateOf(false) }

    val firebaseAuth = FirebaseAuth.getInstance()
    val userViewModel = getUserViewModel(context)


    val brush = Brush.verticalGradient(
        colors = listOf(
            CC.primary(), CC.secondary()
        )
    )

    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
        auth.currentUser?.email?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            userViewModel.findUserByEmail(it) {}
        }
        visible = true
    }

    if(loginfailed){
        AuthFailed(onDismissRequest = {value -> loginfailed = value }, context)
    }

    if(registerfailed){
        RegistrationFailed(onDismissRequest = {value -> registerfailed = value }, context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {}, colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CC.primary()
                )
            )
        },
        containerColor = CC.primary()
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
                            context,
                            firebaseAuth,
                            firstName,
                            lastName,
                        ) { success ->
                            if (success){
                                isSigningUp = false
                            }else{
                                registerfailed = true
                            }
                            loading = false
                        } else handleSignIn(
                            context, firebaseAuth, email, password, navController, userViewModel
                        ) { success ->
                            if (!success){
                                loginfailed = true
                            }
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
    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
    userViewModel.findUserByEmail(email) { user ->
        if (user != null) {
            userViewModel.deleteAllTables()
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { tokenTask ->
                if (!tokenTask.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", tokenTask.exception)
                    return@OnCompleteListener
                }
                val token = tokenTask.result
                generateFCMID { id ->
                    val fcmToken = Fcm(id = id, token = token, userId = email)
                    writeFcmToken(token = fcmToken)
                }
            })

            UniAdminPreferences.saveUserEmail(email)
            UniAdminPreferences.saveUserType(user.userType.ifEmpty { "student" })
            navController.navigate("courses") {
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
    context: Context,
    firebaseAuth: FirebaseAuth,
    email: String,
    password: String,
    onComplete: (Boolean) -> Unit,

    ) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                onComplete(true)

            } else {
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
                userViewModel.findUserByEmail(email) { user ->
                    if (user != null) {
                        navController.navigate("courses")
                        Toast.makeText(
                            context,
                            "Welcome back, ${user.firstName}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        //save user email and UserType to shared preferences
                        val userType = user.userType.ifEmpty { "student" }
                        UniAdminPreferences.saveUserEmail(email)
                        UniAdminPreferences.saveUserType(userType)

                    } else {
                        Toast.makeText(context, "No user found", Toast.LENGTH_SHORT).show()
                        navController.navigate("moreDetails")
                    }
                }

                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { tokenTask ->
                    if (!tokenTask.isSuccessful) {
                        Log.w("FCM", "Fetching FCM registration token failed", tokenTask.exception)
                        return@OnCompleteListener

                    }
                    val token = tokenTask.result
                    generateFCMID { id ->
                        val fcmToken = Fcm(id = id, token = token, userId = email)
                        writeFcmToken(token = fcmToken)
                    }
                })
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    } else {
        onComplete(false)
        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun RegistrationFailed(onDismissRequest: (Boolean) -> Unit, context: Context){
    ErrorDialog(
        title = "Registration Failed",
        message = "Check your email and password and try again",
        onDismissRequest = onDismissRequest,
        context = context
    )
}

@Composable
fun AuthFailed(onDismissRequest: (Boolean) -> Unit, context: Context) {
    ErrorDialog(
        title = "Login Failed🥲",
        message = "Check your email and password and try again",
        onDismissRequest = onDismissRequest,
        context = context
    )
}


@Composable
fun ErrorDialog(title: String, message: String, onDismissRequest: (Boolean) -> Unit, context: Context) {
    AlertDialog(
        containerColor = CC.primary(),
        onDismissRequest = {onDismissRequest(false)},
        title = { Text(text = title, style = CC.titleTextStyle(context)) },
        text = {
            Text(
                text = message,
                style = CC.descriptionTextStyle(context)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onDismissRequest(false) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = CC.primary(),
                    containerColor = CC.secondary()
                )

            ) {
                Text(text = "OK", style = CC.descriptionTextStyle(context))
            }
        }
    )
}
