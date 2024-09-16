package com.mike.uniadmin.authentication

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun PasswordReset(navController: NavController, context: Context) {
    var email by remember { mutableStateOf("") }
    val auth: FirebaseAuth = Firebase.auth
    var loading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }

    val brush = Brush.verticalGradient(
        colors = listOf(
            CC.primary(), CC.secondary()
        )
    )

    Column(
        modifier = Modifier
            .background(brush)
            .imePadding()
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth(0.9f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Password Reset",
                style = CC.titleTextStyle()
                    .copy(fontSize = 30.sp, fontWeight = FontWeight.Bold)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(success) {
                SendPasswordResetSuccess(context, email) {
                    if (it) {
                        success = false
                        navController.navigate("login")
                    }
                }
            }
            CC.CustomTextField(
                value = email, onValueChange = { eM ->
                    email = eM
                }, label = "Email", singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    loading = true
                    if (email.isEmpty()) {
                        Toast.makeText(
                            context, "Please enter your email", Toast.LENGTH_SHORT
                        ).show()
                        loading = false
                    } else {
                        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                success = true
                                loading = false
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to send password reset email.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                loading = false
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CC.extraColor2(), contentColor = CC.tertiary()
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(40.dp)
                    .width(200.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = CC.secondary(), trackColor = CC.textColor(),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Send Reset Email", style = CC.descriptionTextStyle())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPasswordResetSuccess(context: Context, email: String, onDismiss: (Boolean) -> Unit = {}) {
    BasicAlertDialog(onDismissRequest = {},
        content = {
            BoxWithConstraints {
                val maxWidth = maxWidth
                Column(
                    modifier = Modifier
                        .background(CC.secondary(), RoundedCornerShape(16.dp))
                        .height(maxWidth * 0.5f)
                        .width(maxWidth),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        "Password Reset", style = CC.titleTextStyle().copy(
                            fontSize = 20.sp, fontWeight = FontWeight.Bold
                        )
                    )

                    val emailText = AnnotatedString.Builder().apply {
                        append("Password reset email will be sent to ")
                        pushStyle(emailTextStyle().toSpanStyle()) // Apply email style
                        append(email)
                        pop() // Remove email style
                        append(" if it exists in our database")
                    }.toAnnotatedString()

                    Text(
                        text = emailText,
                        modifier = Modifier.padding(6.dp),
                        style = CC.descriptionTextStyle().copy(textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            openGmailApp(context)
                            onDismiss(true)

                        },
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CC.extraColor1(), contentColor = CC.tertiary()
                        )
                    ) {
                        Text("Open Gmail App", style = CC.descriptionTextStyle())
                    }

                }

            }
        })
}

// In your CC class
@Composable
fun emailTextStyle(): TextStyle = CC.descriptionTextStyle().copy(
    textDecoration = TextDecoration.Underline,
    color = CC.tertiary(),
    fontStyle = FontStyle.Italic
)



fun openGmailApp(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setClassName(
        "com.google.android.gm",
        "com.google.android.gm.ConversationListActivityGmail"
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Add this flag
    context.startActivity(intent)
}