package com.mike.uniadmin.authentication

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@Composable
fun PasswordReset(navController: NavController, context: Context) {
    var email by remember { mutableStateOf("") }
    val auth: FirebaseAuth = Firebase.auth
    var loading by remember { mutableStateOf(false) }
    val brush = Brush.verticalGradient(
        colors = listOf(
            CC.primary(), CC.secondary()
        )
    )

    Column(
        modifier = Modifier.background(brush).imePadding().padding(16.dp).fillMaxSize(),
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
                "Password Reset", style = CC.titleTextStyle(context).copy(fontSize = 30.sp, fontWeight = FontWeight.Bold)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CC.SingleLinedTextField(
                value = email, onValueChange = { eM ->
                    email = eM
                }, label = "Email", singleLine = true, context = context
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
                                loading = false
                                Toast.makeText(
                                    context,
                                    "Password reset email sent to $email",
                                    Toast.LENGTH_SHORT
                                ).show()
                                email = ""
                                navController.navigate("login")
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
                    .height(50.dp)
                    .width(200.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = CC.secondary(), trackColor = CC.textColor()
                    )
                } else {
                    Text("Send Reset Email", style = CC.descriptionTextStyle(context))
                }
            }
        }
    }
}


@Preview
@Composable
fun MyPrev() {
    PasswordReset(rememberNavController(), LocalContext.current)
}