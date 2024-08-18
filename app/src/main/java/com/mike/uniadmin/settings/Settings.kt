package com.mike.uniadmin.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ModeNight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.DeviceTheme
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.R
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.notifications.NotificationEntity
import com.mike.uniadmin.dataModel.notifications.NotificationViewModel
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserPreferencesEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.Feedback
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.MyDatabase.generateSharedPreferencesID
import com.mike.uniadmin.model.MyDatabase.updatePassword
import com.mike.uniadmin.ui.theme.FontPreferences
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavController, context: Context, mainActivity: MainActivity) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val fontPrefs = remember { FontPreferences(context) }
    var savedFont by remember { mutableStateOf("system") }

    val userAdmin = context.applicationContext as UniAdmin
    val userRepository = remember { userAdmin.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository
        )
    )

    val notificationRepository = remember { userAdmin.notificationRepository }
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModel.NotificationViewModelFactory(
            notificationRepository
        )
    )

    val currentUser by userViewModel.user.observeAsState()


    LaunchedEffect(savedFont) {
        savedFont = fontPrefs.getSelectedFont().toString()
        userViewModel.findUserByEmail(user?.email!!) {}

    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(onClick = { navController.navigate("homeScreen") }) {
                    Icon(
                        Icons.Default.ArrowBackIosNew, "Back", tint = CC.textColor()
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = CC.primary()
            )
            )
        }, containerColor = CC.primary()
    ) {

        Column(
            modifier = Modifier
                .background(CC.primary())
                .verticalScroll(rememberScrollState())
                .padding(it)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Settings",
                    style = CC.titleTextStyle(context),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text("Account", style = CC.titleTextStyle(context))
            }
            Spacer(modifier = Modifier.height(20.dp))
            //Profile Section
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(80.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .border(
                                1.dp, CC.textColor(), CircleShape
                            )
                            .clip(CircleShape)
                            .background(CC.secondary(), CircleShape)
                            .size(70.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentUser?.profileImageLink?.isNotEmpty() == true) {
                            AsyncImage(model = currentUser?.profileImageLink,
                                contentDescription = "Profile Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                onError = { R.drawable.notification },
                                onLoading = { R.drawable.logo }

                            )
                        } else {
                            Text(
                                "${currentUser?.firstName?.get(0)}${currentUser?.lastName?.get(0)}",
                                style = CC.titleTextStyle(context)
                                    .copy(fontWeight = FontWeight.Bold, fontSize = 40.sp),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxHeight(0.9f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            currentUser?.firstName + " " + currentUser?.lastName,
                            style = CC.descriptionTextStyle(context),
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            "Personal Info",
                            style = CC.descriptionTextStyle(context),
                            color = CC.textColor().copy(0.8f)
                        )
                    }
                }
                MyIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowForwardIos, navController, "profile"
                )

            }
            Spacer(modifier = Modifier.height(40.dp))
            Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                Text("System", style = CC.titleTextStyle(context))
            }
            Spacer(modifier = Modifier.height(20.dp))
            DarkMode(context)
            Spacer(modifier = Modifier.height(20.dp))
            Notifications(context, userViewModel)
            Spacer(modifier = Modifier.height(40.dp))
            Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                Text("Security", style = CC.titleTextStyle(context))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Biometrics(context, mainActivity, userViewModel)
            Spacer(modifier = Modifier.height(20.dp))
            PasswordUpdateSection(context, notificationViewModel)
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Font Style", style = CC.titleTextStyle(context))

                Text(
                    savedFont,
                    style = CC.descriptionTextStyle(context)
                        .copy(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                )
                IconButton(
                    onClick = { navController.navigate("appearance") },
                    modifier = Modifier.background(
                        CC.secondary(), RoundedCornerShape(10.dp)
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Font Style",
                        tint = CC.textColor()
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                Text("We care about your feedback", style = CC.titleTextStyle(context))
            }
            currentUser?.let { it1 -> RatingAndFeedbackScreen(it1, context) }
            Spacer(modifier = Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(0.9f)) {
                Text("About", style = CC.titleTextStyle(context))
            }
            MyAbout(context)
        }
    }
}


@Composable
fun MyIconButton(icon: ImageVector, navController: NavController, route: String) {
    Box(modifier = Modifier
        .background(CC.secondary(), RoundedCornerShape(10.dp))
        .clickable { navController.navigate(route) }
        .size(50.dp)
        .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = CC.textColor())
    }

}

@Composable
fun DarkMode(context: Context) {
    val icon = if (DeviceTheme.darkMode.value) Icons.Filled.ModeNight else Icons.Filled.WbSunny
    val iconDescription =
        if (DeviceTheme.darkMode.value) "Switch to Dark Mode" else "Switch to Light Mode"

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .background(CC.secondary(), CircleShape)
                .size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, contentDescription = iconDescription, tint = CC.extraColor2()
            )
        }
        Text("Dark Mode", style = CC.descriptionTextStyle(context), fontSize = 20.sp)
        Switch(
            onCheckedChange = {
                DeviceTheme.darkMode.value = it
                DeviceTheme.saveDarkModePreference(it)

            }, checked = DeviceTheme.darkMode.value, colors = SwitchDefaults.colors(
                checkedThumbColor = CC.extraColor1(),
                uncheckedThumbColor = CC.extraColor2(),
                checkedTrackColor = CC.extraColor2(),
                uncheckedTrackColor = CC.extraColor1(),
                checkedIconColor = CC.textColor(),
                uncheckedIconColor = CC.textColor()
            )
        )
    }
}

@Composable
fun Notifications(context: Context, viewModel: UserViewModel) {
    var isNotificationEnabled by remember { mutableStateOf(false) }
    val icon =
        if (isNotificationEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff
    val iconDescription =
        if (isNotificationEnabled) "Enable Notifications" else "Disable Notifications"
    val currentUser by viewModel.user.observeAsState()

    LaunchedEffect(Unit) {
        currentUser?.id?.let { userId -> // Use safe call and let
            viewModel.fetchPreferences(userId, onPreferencesFetched = { userPreferences ->
                isNotificationEnabled = userPreferences?.notifications == "enabled"
            })
        }
    }



    fun updatePreferences(isEnabled: Boolean) {
        if (currentUser != null) { // Check if currentUser is not null
            generateSharedPreferencesID { id ->
                val myPreferences = UserPreferencesEntity(
                    studentID = currentUser!!.id, // Now safe to access currentUser.id
                    id = id, notifications = if (isEnabled) "enabled" else "disabled"
                )
                viewModel.writePreferences(myPreferences) {
                    Log.d("Preferences", "Preferences successfully updated: $myPreferences")
                }
            }
        } else {
            // Handle the case where currentUser is null (e.g., show an error message)
            Log.e("Preferences", "Cannot update preferences: currentUser is null")
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .background(CC.secondary(), CircleShape)
                .size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, contentDescription = iconDescription, tint = CC.extraColor2()
            )
        }
        Text("Notifications", style = CC.descriptionTextStyle(context), fontSize = 20.sp)
        Switch(
            onCheckedChange = { notifications ->
                if (!notifications) {
                    (context as MainActivity).requestNotificationPermission()

                }
                isNotificationEnabled = notifications
                updatePreferences(notifications)
            }, checked = isNotificationEnabled, colors = SwitchDefaults.colors(
                checkedThumbColor = CC.extraColor1(),
                uncheckedThumbColor = CC.extraColor2(),
                checkedTrackColor = CC.extraColor2(),
                uncheckedTrackColor = CC.extraColor1(),
                checkedIconColor = CC.textColor(),
                uncheckedIconColor = CC.textColor()
            )
        )
    }
}


@Composable
fun Biometrics(context: Context, mainActivity: MainActivity, viewModel: UserViewModel) {
    var isBiometricsEnabled by remember { mutableStateOf(false) }
    val icon = if (isBiometricsEnabled) Icons.Filled.Security else Icons.Filled.Security
    val iconDescription = if (isBiometricsEnabled) "Biometrics enabled" else "Biometrics disabled"
    val promptManager = mainActivity.promptManager
    val currentUser by viewModel.user.observeAsState()

    LaunchedEffect(Unit) {
        currentUser?.id?.let { userId -> // Use safe call and let
            viewModel.fetchPreferences(userId, onPreferencesFetched = { userPreferences ->
                isBiometricsEnabled = userPreferences?.biometrics == "enabled"
            })
        }
    }

    fun updatePreferences(isEnabled: Boolean) {
        if (currentUser != null) { // Check if currentUser is not null
            generateSharedPreferencesID { id ->
                val myPreferences = UserPreferencesEntity(
                    studentID = currentUser!!.id, // Now safe to access currentUser.id
                    id = id, notifications = if (isEnabled) "enabled" else "disabled"
                )
                viewModel.writePreferences(myPreferences) {
                    Log.d("Preferences", "Preferences successfully updated: $myPreferences")
                }
            }
        } else {
            // Handle the case where currentUser is null (e.g., show an error message)
            Log.e("Preferences", "Cannot update preferences: currentUser is null")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .background(CC.secondary(), CircleShape)
                .size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = CC.extraColor2(),
            )
        }
        Text(
            "Biometrics (${if (isBiometricsEnabled) "Enabled" else "Disabled"})",
            style = CC.descriptionTextStyle(context),
            fontSize = 20.sp
        )
        Switch(
            onCheckedChange = { isChecked ->
                if (isChecked) {
                    promptManager.showBiometricPrompt(
                        title = "Authenticate", description = "Please authenticate to continue"
                    ) { success ->
                        if (success) {
                            isBiometricsEnabled = true
                            updatePreferences(true)
                        }
                    }
                } else {
                    isBiometricsEnabled = false
                    updatePreferences(false)
                }
            }, checked = isBiometricsEnabled, colors = SwitchDefaults.colors(
                checkedThumbColor = CC.extraColor1(),
                uncheckedThumbColor = CC.extraColor2(),
                checkedTrackColor = CC.extraColor2(),
                uncheckedTrackColor = CC.extraColor1(),
                checkedIconColor = CC.textColor(),
                uncheckedIconColor = CC.textColor()
            )
        )
    }
}


@Composable
fun PasswordUpdateSection(context: Context, notificationViewModel: NotificationViewModel) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var loading by remember { mutableStateOf(false) }
    var signInMethod by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxWidth(0.8f)) {
        Text("Change your Password", style = CC.titleTextStyle(context), fontSize = 18.sp)
    }
    Spacer(modifier = Modifier.height(10.dp))
    LaunchedEffect(key1 = Unit) {
        if (currentUser != null) {
            for (userInfo in currentUser.providerData) {
                when (userInfo.providerId) {
                    "password" -> {
                        // User signed in with email and password
                        signInMethod = "password"
                        Log.d("Auth", "User signed in with email/password")
                    }

                    "google.com" -> {
                        // User signed in with Google
                        signInMethod = "google.com"
                        Log.d("Auth", "User signed in with Google")
                    }

                    "github.com" -> {
                        // User signed in with GitHub
                        signInMethod = "github.com"
                        Log.d("Auth", "User signed in with GitHub")
                    }
                }
            }
        }
    }
    if (signInMethod != "password") {
        Row(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth(0.9f)
        ) {
            Text(
                "This section only applies to users who signed in using Email and Password",
                style = CC.descriptionTextStyle(context),
                color = CC.textColor().copy(0.5f),
                textAlign = TextAlign.Center
            )

        }
    } else {
        Column(
            modifier = Modifier
                .border(
                    1.dp, CC.secondary(), RoundedCornerShape(10.dp)
                )
                .fillMaxWidth(0.8f)
                .padding(16.dp)
        ) {
            PasswordTextField(
                label = "Current Password",
                value = currentPassword,
                isEditing = true,
                onValueChange = { currentPassword = it },
                context = context
            )
            PasswordTextField(
                label = "New Password",
                value = newPassword,
                isEditing = true,
                onValueChange = { newPassword = it },
                context = context
            )
            PasswordTextField(
                label = "Confirm Password",
                value = confirmPassword,
                isEditing = true,
                onValueChange = { confirmPassword = it },
                context = context
            )

            Button(
                onClick = {
                    loading = true
                    if (newPassword == confirmPassword && newPassword.isNotEmpty() && currentPassword.isNotEmpty()) {
                        currentUser?.let { user ->
                            val credential =
                                EmailAuthProvider.getCredential(user.email!!, currentPassword)
                            user.reauthenticate(credential).addOnCompleteListener { reAuthTask ->
                                if (reAuthTask.isSuccessful) {
                                    updatePassword(newPassword, onSuccess = {
                                        // Handle success (e.g., show a success message)
                                        loading = false
                                        MyDatabase.generateNotificationID { id ->
                                            notificationViewModel.writeNotification(
                                                notificationEntity = NotificationEntity(
                                                    id = id,
                                                    title = "Account Updated",
                                                    description = "You have successfully updated your password",
                                                    date = CC.getTimeStamp(),
                                                    time = CC.getTimeStamp(),
                                                    category = "Announcements",
                                                )
                                            )
                                        }
                                        Toast.makeText(
                                            context,
                                            "Password updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                    }, onFailure = { exception ->
                                        // Handle failure (e.g., show an error message)
                                        loading = false
                                        Toast.makeText(
                                            context,
                                            "Failed to Change password: ${exception.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    })
                                } else {
                                    // Handle authentication failure
                                    loading = false
                                    Toast.makeText(
                                        context,
                                        "Authentication failed: ${reAuthTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    } else {
                        // Handle password mismatch
                        loading = false
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = CC.tertiary(), contentColor = Color.White
                ), shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = CC.primary(),
                            trackColor = CC.tertiary(),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Change Password", style = CC.descriptionTextStyle(context))
                    }
                }

            }
        }
    }
}

@Composable
fun PasswordTextField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    context: Context
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = CC.descriptionTextStyle(context)) },
        enabled = isEditing,
        textStyle = CC.descriptionTextStyle(context),
        colors = TextFieldDefaults.colors(
            focusedTextColor = CC.textColor(),
            disabledContainerColor = CC.secondary(),
            focusedContainerColor = CC.primary(),
            unfocusedContainerColor = CC.primary(),
            focusedIndicatorColor = CC.secondary(),
            unfocusedIndicatorColor = CC.tertiary(),
            cursorColor = CC.textColor()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun MyAbout(context: Context) {
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Uni Admin", style = CC.descriptionTextStyle(context).copy(
                fontWeight = FontWeight.Bold, fontSize = 20.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Version $versionName", style = CC.descriptionTextStyle(context))
        Text("Developed by Mike", style = CC.descriptionTextStyle(context))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {

            //Phone Icon
            IconButton(onClick = {
                val intent = Intent(
                    Intent.ACTION_DIAL, Uri.parse("tel:+254799013845")
                )
                context.startActivity(intent)
            }, modifier = Modifier
                .background(CC.extraColor1(), CircleShape)
                .size(35.dp)) {
                Icon(Icons.Default.Call, "Call", tint = CC.textColor())
            }
            Spacer(modifier = Modifier.width(10.dp))

            // GitHub Icon with Link
            IconButton(
                onClick = { uriHandler.openUri("https://github.com/mikesplore") },
                modifier = Modifier
                    .background(CC.extraColor1(), CircleShape)
                    .size(35.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.github),
                    tint = CC.textColor(),
                    contentDescription = "GitHub Profile",
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))

            // Google Icon with Link
            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:") // Only email apps should handle this
                        putExtra(
                            Intent.EXTRA_EMAIL,
                            arrayOf("mikepremium8@gmail.com")
                        ) // Recipients
                        putExtra(Intent.EXTRA_SUBJECT, "Email Subject")
                        putExtra(Intent.EXTRA_TEXT, "Email body text")
                    }
                    ContextCompat.startActivity(context, intent, null) // Start the activity
                }, modifier = Modifier
                    .background(CC.extraColor1(), CircleShape)
                    .size(35.dp)
            ) {
                Icon(
                    painter = painterResource(com.google.android.gms.base.R.drawable.googleg_standard_color_18),
                    tint = CC.textColor(),
                    contentDescription = "Open Gmail",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("All rights reserved © 2024", style = CC.descriptionTextStyle(context))
    }
}

@Composable
fun StarRating(
    currentRating: Int, onRatingChanged: (Int) -> Unit, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val color = when {
                i <= currentRating -> when (i) {
                    in 1..2 -> Color.Red
                    3 -> CC.extraColor2()
                    else -> Color.Green
                }

                else -> CC.secondary()
            }
            val animatedScale by animateFloatAsState(
                targetValue = if (i <= currentRating) 1.2f else 1.0f,
                animationSpec = tween(durationMillis = 300),
                label = ""
            )
            Star(filled = i <= currentRating,
                color = color,
                scale = animatedScale,
                onClick = { onRatingChanged(i) })
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun Star(
    filled: Boolean, color: Color, scale: Float, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    val path = Path().apply {
        moveTo(50f, 0f)
        lineTo(61f, 35f)
        lineTo(98f, 35f)
        lineTo(68f, 57f)
        lineTo(79f, 91f)
        lineTo(50f, 70f)
        lineTo(21f, 91f)
        lineTo(32f, 57f)
        lineTo(2f, 35f)
        lineTo(39f, 35f)
        close()
    }

    Canvas(
        modifier = modifier
            .size((40 * scale).dp)
            .clickable(onClick = onClick)
    ) {
        drawPath(
            path = path,
            color = if (filled) color else com.mike.uniadmin.ui.theme.BrightBlue,
            style = if (filled) Stroke(width = 8f) else Stroke(
                width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun RatingAndFeedbackScreen(user: UserEntity, context: Context) {
    var currentRating by remember { mutableIntStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    var averageRatings by remember { mutableStateOf("") }
    var showFeedbackForm by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        MyDatabase.fetchAverageRating { averageRating ->
            averageRatings = averageRating
        }
    }

    Column(
        modifier = Modifier

            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (averageRatings.isEmpty()) "No ratings yet" else "Average Rating: $averageRatings",
            style = CC.descriptionTextStyle(context),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        StarRating(
            currentRating = currentRating,
            onRatingChanged = { rating ->
                currentRating = rating
                showFeedbackForm = true
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(visible = showFeedbackForm) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = {
                        Text(
                            "Enter your feedback (optional)",
                            style = CC.descriptionTextStyle(context)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    textStyle = CC.descriptionTextStyle(context),
                    maxLines = 5,
                    colors = CC.appTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        loading = true
                        MyDatabase.generateFeedbackID { feedbackId ->
                            val feedback = Feedback(
                                id = feedbackId,
                                rating = currentRating,
                                sender = user.firstName + " " + user.lastName,
                                message = feedbackText,
                                admissionNumber = user.id
                            )
                            MyDatabase.writeFeedback(feedback, onSuccess = {
                                loading = false
                                Toast.makeText(
                                    context, "Thanks for your feedback", Toast.LENGTH_SHORT
                                ).show()
                                feedbackText = ""
                                MyDatabase.fetchAverageRating { averageRating ->
                                    averageRatings = averageRating
                                }
                                showFeedbackForm = false
                            }, onFailure = {
                                loading = false
                                Toast.makeText(
                                    context,
                                    "Failed to send feedback: ${it?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CC.extraColor1(), contentColor = CC.secondary()
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                color = CC.primary(),
                                trackColor = CC.tertiary(),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Submit Feedback", style = CC.descriptionTextStyle(context))
                        }
                    }
                }
            }
        }
    }
}




