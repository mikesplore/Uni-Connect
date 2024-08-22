package com.mike.uniadmin.settings

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.MainActivity
import com.mike.uniadmin.backEnd.users.UserPreferencesEntity
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.helperFunctions.MyDatabase.generateSharedPreferencesID
import com.mike.uniadmin.helperFunctions.MyDatabase.updatePassword
import com.mike.uniadmin.ui.theme.CommonComponents

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
    BoxWithConstraints {
        val rowWidth = maxWidth
        val iconSize = rowWidth * 0.10f

        Row(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .background(CommonComponents.secondary(), CircleShape)
                    .size(iconSize),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    tint = CommonComponents.extraColor2(),
                )
            }
            Text(
                "Biometrics (${if (isBiometricsEnabled) "Enabled" else "Disabled"})",
                style = CommonComponents.descriptionTextStyle(context),
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
                },
                checked = isBiometricsEnabled,
                colors = switchColors(),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}


@Composable
fun PasswordUpdateSection(context: Context) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var loading by remember { mutableStateOf(false) }
    var signInMethod by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxWidth(0.8f)) {
        Text("Change your Password", style = CommonComponents.titleTextStyle(context), fontSize = 18.sp)
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
                style = CommonComponents.descriptionTextStyle(context),
                color = CommonComponents.textColor().copy(0.5f),
                textAlign = TextAlign.Center
            )

        }
    } else {
        Column(
            modifier = Modifier
                .border(
                    1.dp, CommonComponents.secondary(), RoundedCornerShape(10.dp)
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
                    containerColor = CommonComponents.tertiary(), contentColor = Color.White
                ), shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = CommonComponents.primary(),
                            trackColor = CommonComponents.tertiary(),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Change Password", style = CommonComponents.descriptionTextStyle(context))
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
        label = { Text(label, style = CommonComponents.descriptionTextStyle(context)) },
        enabled = isEditing,
        textStyle = CommonComponents.descriptionTextStyle(context),
        colors = TextFieldDefaults.colors(
            focusedTextColor = CommonComponents.textColor(),
            disabledContainerColor = CommonComponents.secondary(),
            focusedContainerColor = CommonComponents.primary(),
            unfocusedContainerColor = CommonComponents.primary(),
            focusedIndicatorColor = CommonComponents.secondary(),
            unfocusedIndicatorColor = CommonComponents.tertiary(),
            cursorColor = CommonComponents.textColor()
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp)
    )
}