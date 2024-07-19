package com.mike.uniadmin

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.mike.uniadmin.announcements.AnnouncementsScreen
import com.mike.uniadmin.authentication.LoginScreen
import com.mike.uniadmin.authentication.PasswordReset
import com.mike.uniadmin.chat.ChatScreen
import com.mike.uniadmin.chat.ParticipantsScreen
import com.mike.uniadmin.chat.UserChatScreen
import com.mike.uniadmin.model.Screen
import com.mike.uniadmin.settings.Settings
import com.mike.uniadmin.ui.theme.Appearance
import com.mike.uniadmin.youtubeVideos.Videos

@OptIn(ExperimentalPagerApi::class)
@Composable
fun NavigationGraph(context: Context, mainActivity: MainActivity){
    val navController = rememberNavController()
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val screens = listOf(
        Screen.Home, Screen.Announcements, Screen.Assignments, Screen.Timetable, Screen.Attendance
    )
    NavHost(navController = navController, startDestination = "videos"){
        composable("splashscreen"){
            SplashScreen(navController = navController, context)
        }
        composable("videos"){
            Videos(navController = navController, context)
        }
        composable("login"){
            LoginScreen(navController = navController, context)
        }
        composable("dashboard"){
            Dashboard(navController = navController, context)
        }
        composable("profile"){
            ProfileScreen(navController = navController, context)
        }
        composable("settings"){
            Settings(navController = navController, context, mainActivity)
        }
        composable("users"){
            ParticipantsScreen(navController = navController, context)
        }
        composable("chat/{userId}", enterTransition = {
            fadeIn(animationSpec = tween(1000)) + slideInVertically(animationSpec = tween(1000)) { initialState -> initialState }
        }, exitTransition = {
            fadeOut(animationSpec = tween(1000)) + slideOutVertically(animationSpec = tween(1000)) { finalState -> finalState }
        }, arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            UserChatScreen(
                navController,
                LocalContext.current,
                backStackEntry.arguments?.getString("userId") ?: ""
            )
        }
        composable("announcements"){
            AnnouncementsScreen(navController = navController, context)
        }
        composable("timetable"){
            TimetableScreen(navController = navController, context)
        }
        composable("assignments"){
            AssignmentScreen(navController = navController, context)
        }
        composable("attendance"){
            ManageAttendanceScreen(navController = navController, context)
        }
        composable("passwordreset"){
            PasswordReset(navController = navController, context)
        }
        composable("courses"){
            ManageCoursesScreen(navController = navController, context)
        }
        composable("appearance"){
            Appearance(navController = navController, context)
        }
        composable("discussion"){
            ChatScreen(navController = navController, context)
        }
        composable("homescreen"){
            HomeScreen(navController,context,pagerState,mainActivity,screens,coroutineScope)
        }

        composable("course/{courseCode}",
            arguments = listOf(navArgument("courseCode") { type = NavType.StringType })
        ) { backStackEntry ->
            CourseScreen(
                backStackEntry.arguments?.getString("courseCode") ?: "",
                context
            )
        }

        composable("courseContent/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            CourseContent(
                navController,
                context,
                backStackEntry.arguments?.getString("courseId") ?: ""
            )
        }
    }
}