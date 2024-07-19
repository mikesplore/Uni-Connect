package com.mike.uniadmin

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerDefaults
import com.google.accompanist.pager.PagerState
import com.google.firebase.auth.FirebaseAuth
import com.mike.uniadmin.announcements.AnnouncementsScreen
import com.mike.uniadmin.model.MyDatabase
import com.mike.uniadmin.model.Screen
import com.mike.uniadmin.model.User
import com.mike.uniadmin.ui.theme.GlobalColors
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.mike.uniadmin.CommonComponents as CC


@OptIn(ExperimentalPagerApi::class, ExperimentalSnapperApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    context: Context,
    pagerState: PagerState,
    activity: MainActivity,
    screens: List<Screen>,
    coroutineScope: CoroutineScope
    ){
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var signedInUser by remember { mutableStateOf(User()) }
    var profileImageUrl by remember { mutableStateOf("") }

    if (user?.photoUrl.toString().isNotEmpty()){
        profileImageUrl = user?.photoUrl.toString()
    }

    LaunchedEffect(Unit) {
        GlobalColors.loadColorScheme(context)
    }
    LaunchedEffect(user?.email!!) {
        MyDatabase.fetchUserDataByEmail(user.email!!) { fetchedUser ->
            if (fetchedUser != null) {
                signedInUser = fetchedUser
            }
        }

    }
    //main content starts here
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .height(85.dp)
                    .background(Color.Transparent),
                containerColor = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CC.primary().copy()),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    screens.forEachIndexed { index, screen ->
                        val isSelected = pagerState.currentPage == index

                        val iconColor by animateColorAsState(
                            targetValue = if (isSelected) CC.textColor() else CC.textColor().copy(0.7f),
                            label = "",
                            animationSpec = tween(500)
                        )

                        // Use NavigationBarItem
                        NavigationBarItem(
                            selected = isSelected,
                            label = {
                                AnimatedVisibility(
                                    visible = isSelected,
                                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                                        animationSpec = tween(500)
                                    ) { initialState -> initialState },
                                    exit = fadeOut(animationSpec = tween(500)) + slideOutVertically(
                                        animationSpec = tween(500)
                                    ) { initialState -> initialState }
                                ) {
                                    Text(
                                        text = screen.name,
                                        style = CC.descriptionTextStyle(context).copy(fontSize = 13.sp),
                                        color = CC.textColor()
                                    )
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = CC.extraColor2(),
                                unselectedIconColor = CC.textColor(),
                                selectedIconColor = CC.textColor()
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.name,
                                        tint = iconColor,
                                        modifier = Modifier.size(25.dp)
                                    )

                                }
                            }
                        )
                    }
                }
            }
        },
        containerColor = CC.primary()
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            count = screens.size,
            modifier = Modifier.padding(innerPadding),
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
        ) { page ->
            when (screens[page]) {
                Screen.Home -> Dashboard(navController, context)
                Screen.Assignments -> AssignmentScreen( navController,context)
                Screen.Announcements -> AnnouncementsScreen(navController, context)
                Screen.Timetable -> TimetableScreen(navController, context)
                Screen.Attendance -> ManageAttendanceScreen(navController, context)

            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarContent(profileImageUrl: String, signedInUser: User, context: Context){
    TopAppBar(
        title = {
            Row(modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .background(CC.secondary(), CircleShape)
                    .clip(CircleShape)
                    .size(50.dp),
                    contentAlignment = Alignment.Center,){
                    if (profileImageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = profileImageUrl,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }else{
                        Image(
                            painter = painterResource(R.drawable.student),""
                        )
                    }

                }
                Column(modifier = Modifier
                    .padding(start = 10.dp, end = 20.dp)
                    .weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(CC.getGreetingMessage(), style = CC.descriptionTextStyle(context).copy(color = CC.textColor().copy(alpha = 0.5f)))
                    Text(signedInUser.firstName, style = CC.titleTextStyle(context).copy(fontWeight = FontWeight.ExtraBold))

                }
            }

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CC.primary(),
        ),
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "",
                    tint = CC.textColor()
                )
            }
        }
    )
}