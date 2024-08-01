package com.mike.uniadmin.authentication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun GoogleAuth(
    firebaseAuth: FirebaseAuth,
    onSignInSuccess: () -> Unit,
    onSignInFailure: (String) -> Unit,
) {
    val activity = LocalContext.current as Activity
    val provider = OAuthProvider.newBuilder("google.com")
    var isLoading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    provider.scopes = listOf(
        "profile",    // Required for display name and profile picture
        "email",      // Required for email address
        "openid"      // Required for OpenID Connect authentication
    )

    Box(
        modifier = Modifier
            .clickable {
                isLoading = true
                firebaseAuth
                    .startActivityForSignInWithProvider(activity, provider.build())
                    .addOnSuccessListener {
                        success = true
                        isLoading = false // Stop loading on success
                        onSignInSuccess()
                    }
                    .addOnFailureListener {
                        isLoading = false // Stop loading on failure
                        onSignInFailure(it.message ?: "Unknown error")
                        Log.e("GoogleAuth", "Sign-in failed", it)
                    }
            }
            .border(
                width = 1.dp,
                color = CC.textColor(),
                shape = RoundedCornerShape(10.dp)
            )
            .background(CC.secondary(), shape = RoundedCornerShape(10.dp))
            .height(60.dp)
            .width(130.dp),
        contentAlignment = Alignment.Center

    ) {
        // Use AnimatedVisibility for smooth transitions
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                color = CC.primary(),
                trackColor = CC.textColor()
            )
        }

        AnimatedVisibility(
            visible = success && !isLoading, // Show check only if successful and not loading
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Icon(
                Icons.Default.Check,
                "Success",
                tint = CC.textColor(),
                modifier = Modifier.size(40.dp) // Adjust size as needed
            )
        }

        AnimatedVisibility(
            visible = !isLoading && !success, // Show image only if not loading and not successful
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Image(
                painter = painterResource(com.google.android.gms.base.R.drawable.googleg_standard_color_18),
                contentDescription = "Google",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

