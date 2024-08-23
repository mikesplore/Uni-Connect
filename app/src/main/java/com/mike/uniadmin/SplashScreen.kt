package com.mike.uniadmin

import android.content.Context
import android.util.Log
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mike.uniadmin.authentication.UserType
import kotlinx.coroutines.delay
import com.mike.uniadmin.ui.theme.CommonComponents as CC


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashScreen(navController: NavController, context: Context) {
    var startAnimation by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 800, easing = { OvershootInterpolator(2f).getInterpolation(it) }),
        label = ""
    )

    val userType = UniAdminPreferences.userType.value
    val email = UniAdminPreferences.userEmail.value
    val courseCode = UniAdminPreferences.courseCode.value

    val destination = when {
        userType.isEmpty() -> null // Show bottom sheet if userType is not selected
        email.isEmpty() -> "login" // User type selected but email is empty, go to login
        courseCode.isEmpty() -> "courses" // Email is set but courseCode is empty, go to courses
        else -> "homeScreen" // All data is present, go to homeScreen
    }

    LaunchedEffect(userType) {
        if (userType.isEmpty()) {
            delay(1000) // Delay to complete the splash animation first
            showBottomSheet = true // Show the bottom sheet if userType is empty
        } else if (destination != null) {
            navController.navigate(destination) {
                popUpTo("splashScreen") { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    if (email.isNotEmpty()) {
        Log.d("Found User", "User found in the database $email")
    } else {
        Log.e("Found User", "No User found in the database")
    }

    if (showBottomSheet) {
        val maxHeight = LocalContext.current.resources.displayMetrics.heightPixels.dp / 2
        ModalBottomSheet(
            onDismissRequest = { /* Prevent dismissing until a type is selected */ },
            containerColor = CC.primary(),
            modifier = Modifier.heightIn(max = maxHeight * 0.4f) // Adjusted height
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                UserType(context, navController)
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CC.primary(), // Top color
                        CC.secondary()  // Bottom color
                    )
                )
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.logo), // Replace with your logo
                contentDescription = "App Logo",
                tint = CC.extraColor2(),
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Uni Admin", style = CC.titleTextStyle(context)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your Ultimate Educational Companion",
                style = CC.descriptionTextStyle(context)
            )
            Spacer(modifier = Modifier.height(24.dp)) // Added more space for better visual balance
        }

        CircularProgressIndicator(
            color = CC.textColor(),
            trackColor = CC.primary(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp) // More padding for better positioning
        )
    }
}

@Preview
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = rememberNavController(), context = LocalContext.current)
}
