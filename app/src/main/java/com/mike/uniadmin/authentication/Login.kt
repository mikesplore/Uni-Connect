package com.mike.uniadmin.authentication

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mike.uniadmin.chat.getCurrentDate
import com.mike.uniadmin.chat.getCurrentTimeInAmPm
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.notifications.NotificationEntity
import com.mike.uniadmin.dataModel.notifications.NotificationViewModel
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserRepository
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.Details
import com.mike.uniadmin.model.Fcm
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.generateFCMID
import com.mike.uniadmin.model.MyDatabase.generateIndexNumber
import com.mike.uniadmin.model.MyDatabase.writeFcmToken
import com.mike.uniadmin.ui.theme.GlobalColors
import com.mike.uniadmin.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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
    val firebaseAuth = FirebaseAuth.getInstance()
    var visible by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }
    val user = firebaseAuth.currentUser

    val notificationAdmin = context.applicationContext as UniAdmin
    val notificationRepository = remember { notificationAdmin.notificationRepository }
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.NotificationViewModelFactory(notificationRepository)
    )

    val userAdmin = context.applicationContext as? UniAdmin
    val userRepository = remember { userAdmin?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("UserRepository is null")
        )
    )

    val brush = Brush.verticalGradient(
        colors = listOf(
            CC.primary(), CC.secondary()
        )
    )

    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        auth.currentUser?.email?.let { userViewModel.findUserByEmail(it){} }
        visible = true
    }

//     val lifecycleObserver = object : DefaultLifecycleObserver {
//        override fun onStart(owner: LifecycleOwner) {
//            currentUser?.id?.let { writeUserOnlineStatus(it) }
//        }
//
//        override fun onStop(owner: LifecycleOwner) {
//            currentUser?.id?.let { writeUserOfflineStatus(it) }
//        }
//
//        override fun onDestroy(owner: LifecycleOwner) {
//            currentUser?.id?.let { writeUserOfflineStatus(it) }
//        }
//    }

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
                .padding(it)
                .background(brush)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSigningUp) "Sign Up" else "Sign In",
                style = CC.titleTextStyle(context).copy(fontSize = 40.sp)
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
                    style = CC.descriptionTextStyle(context)
                )

                Row(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GoogleAuth(firebaseAuth = firebaseAuth, onSignInSuccess = {
                        handleAuthSuccess(navController)
                    }, onSignInFailure = {
                        Toast.makeText(context, "Sign-in failed: $it", Toast.LENGTH_SHORT).show()
                        isGoogleLoading = false
                    })

                    GitAuth(firebaseAuth = firebaseAuth, onSignInSuccess = {
                        handleAuthSuccess(navController)
                    }, onSignInFailure = {
                        Toast.makeText(context, "Sign-in failed: $it", Toast.LENGTH_SHORT).show()
                        isGithubLoading = false
                    })
                }

                Text(
                    text = "Or", style = CC.descriptionTextStyle(context), color = CC.textColor()
                )

                Text(
                    if (isSigningUp) "Sign up with your email and password" else "Sign in with your email and password",
                    style = CC.descriptionTextStyle(context)
                )

                AnimatedContent(targetState = isSigningUp, label = "") { targetState ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (targetState) {
                            Spacer(modifier = Modifier.height(20.dp))
                            CC.SingleLinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = "First Name",
                                singleLine = true,
                                context = context
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            CC.SingleLinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = "Last Name",
                                singleLine = true,
                                context = context
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        CC.SingleLinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            singleLine = true,
                            context = context
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        CC.PasswordTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            context = context
                        )
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
                            context,
                            firebaseAuth,
                            email,
                            password,
                            navController,
                            userViewModel
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
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(visible = !isSigningUp) {
                Text(text = "Forgot Password? Reset",
                    fontSize = 16.sp,
                    color = CC.textColor(),
                    modifier = Modifier.clickable { navController.navigate("passwordreset") })
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.clickable { isSigningUp = !isSigningUp },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSigningUp) "Already have an account? " else "Don't have an account?",
                    style = CC.descriptionTextStyle(context),
                    fontWeight = FontWeight.Bold,
                    color = CC.textColor(),
                    modifier = Modifier.padding(5.dp)
                )
                Text(
                    text = if (isSigningUp) "Sign In" else "Sign Up",
                    style = CC.descriptionTextStyle(context).copy(fontWeight = FontWeight.Bold),
                    color = CC.extraColor2()
                )
            }
        }
    }
}


fun handleAuthSuccess(navController: NavController) {
    MyDatabase.database.child("Users").orderByChild("email").equalTo(FirebaseAuth.getInstance().currentUser?.email)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userSnapshot = snapshot.children.firstOrNull()
                val user = userSnapshot?.getValue(UserEntity::class.java)
                if (user != null) {
                    // User details found, navigate to HomeScreen
                    navController.navigate("homeScreen")
                } else {
                    // No user details found, navigate to MoreDetails
                    navController.navigate("moreDetails")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error, you might want to log it or show an error message
                println("Error fetching user: ${error.message}")
                // For now, let's navigate to MoreDetails on error as well
                navController.navigate("moreDetails")
            }
        })
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
                            gender = ""
                        )
                        userViewModel.writeUser(newUser) {
                            Toast.makeText(context, "Details saved!", Toast.LENGTH_SHORT).show()
                        }
                        MyDatabase.generateNotificationID { id ->
                            notificationViewModel.writeNotification(
                                notificationEntity = NotificationEntity(
                                    name = firstName,
                                    userId = userID ,
                                    id = id,
                                    title = "$firstName $lastName has Joined Uni Admin!",
                                    description = "Start a conversation by sending  a 👋",
                                    date = getCurrentDate(),
                                    time = getCurrentTimeInAmPm()
                                )
                            )
                            notificationViewModel.fetchNotifications()
                        }
                    }
                    onComplete(true)

                } else {
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }
    } else {
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
    onComplete: () -> Unit
) {
    if (email.isNotEmpty() && password.isNotEmpty()) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Details.email.value = email
                    firebaseAuth.currentUser?.email?.let { current ->
                        userViewModel.findUserByEmail(current) { user ->
                            if (user != null) {
                                navController.navigate("homescreen")
                                Toast.makeText(
                                    context,
                                    "Welcome back, ${user.firstName}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(context, "No user found", Toast.LENGTH_SHORT).show()
                                navController.navigate("moredetails")
                            }
                        }
                    }

                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                            return@OnCompleteListener
                        }
                        val token = task.result
                        generateFCMID { id ->
                            val fcmToken = Fcm(id = id, token = token)
                            writeFcmToken(token = fcmToken)
                        }
                    })
                    onComplete()
                } else {
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    onComplete()
                }
            }
    } else {
        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
    }
}

@Preview
@Composable
fun SignInScreenPreview() {
    LoginScreen(rememberNavController(), LocalContext.current)
}
