package com.mike.uniadmin


import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.model.MyDatabase.fetchUserDataByEmail
import com.mike.uniadmin.model.User
import com.mike.uniadmin.ui.theme.GlobalColors
import java.time.LocalTime
import com.mike.uniadmin.CommonComponents as CC

@Composable
fun Dashboard(navController: NavController, context: Context) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var signedInUser by remember { mutableStateOf(User()) }
    var profileImageUrl by remember { mutableStateOf("") }


    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
        fetchUserDataByEmail(user?.email!!) { fetchedUser ->
            if (fetchedUser != null) {
                signedInUser = fetchedUser
                if (user.photoUrl.toString().isNotEmpty()) {
                    profileImageUrl = user.photoUrl.toString()
                }
            }
        }
    }

    fun getGreetingMessage(): String {
        val currentTime = LocalTime.now()
        return when (currentTime.hour) {
            in 5..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            in 18..21 -> "Good Evening"
            else -> "Good Night"
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBarContent(profileImageUrl, signedInUser, context)
        Column(modifier = Modifier.weight(1f)) { }
    }
}

@Composable
fun Background(context: Context) {
    val icons = listOf(
        Icons.Outlined.Home,
        Icons.AutoMirrored.Outlined.Assignment,
        Icons.Outlined.School,
        Icons.Outlined.AccountCircle,
        Icons.Outlined.BorderColor,
        Icons.Outlined.Book,
    )
    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)

    }
    // Calculate the number of repetitions needed to fill the screen
    val repetitions = 1000 // Adjust this value as needed
    val repeatedIcons = mutableListOf<ImageVector>()
    repeat(repetitions) {
        repeatedIcons.addAll(icons.shuffled())
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(10),
        modifier = Modifier
            .fillMaxSize()
            .background(CC.primary())
            .padding(10.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(repeatedIcons) { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CC.secondary().copy(0.5f),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}


@Preview
@Composable
fun DashboardPreview() {
    Dashboard(navController = rememberNavController(), LocalContext.current)
}
