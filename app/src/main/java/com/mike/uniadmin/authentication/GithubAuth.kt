package com.mike.uniadmin.authentication

import android.app.Activity
import android.util.Log
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import com.mike.uniadmin.R
import com.mike.uniadmin.ui.theme.CommonComponents as CC

@Composable
fun GitAuth(
    firebaseAuth: FirebaseAuth,
    onSignInSuccess: () -> Unit,
    onSignInFailure: (String) -> Unit,
) {
    val activity = LocalContext.current as Activity
    val provider = OAuthProvider.newBuilder("github.com")
    var isLoading by remember { mutableStateOf(false) } // State to track loading
    var success by remember { mutableStateOf(false) }

    Box(modifier = Modifier
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
                    Log.e("GithubAuth", "Sign-in failed", it)
                }
        }
        .border(
            width = 1.dp, color = CC.textColor(), shape = RoundedCornerShape(10.dp)
        )
        .background(CC.secondary(), shape = RoundedCornerShape(10.dp))
        .height(60.dp)
        .width(130.dp),
        contentAlignment = Alignment.Center) {
        if (isLoading) {
            // Show CircularProgressIndicator when loading
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp), color = CC.primary(), trackColor = CC.textColor()

            )
        } else if (success) {
            //show a check to indicate successful authentication
            Icon(
                Icons.Default.Check, "Success", tint = CC.textColor()
            )
        } else {
            // Show GitHub image when not loading
            Image(
                painter = painterResource(R.drawable.github),
                contentDescription = "GitHub",
                colorFilter = ColorFilter.tint(CC.textColor()),
                modifier = Modifier.size(50.dp)
            )
        }
    }

}
