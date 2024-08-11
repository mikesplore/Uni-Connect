package com.mike.uniadmin

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.mike.uniadmin.announcements.AnnouncementsScreen
import com.mike.uniadmin.assignments.AssignmentScreen
import com.mike.uniadmin.authentication.LoginScreen
import com.mike.uniadmin.authentication.MoreDetails
import com.mike.uniadmin.authentication.PasswordReset
import com.mike.uniadmin.chat.DiscussionScreen
import com.mike.uniadmin.chat.UniChat
import com.mike.uniadmin.chat.UniGroups
import com.mike.uniadmin.chat.UserChatScreen
import com.mike.uniadmin.courseContent.CourseContent
import com.mike.uniadmin.courseResources.CourseResources
import com.mike.uniadmin.dataModel.users.ManageUsers
import com.mike.uniadmin.home.Dashboard
import com.mike.uniadmin.home.HomeScreen
import com.mike.uniadmin.model.Screen
import com.mike.uniadmin.notification.PhoneNotifications
import com.mike.uniadmin.settings.Settings
import com.mike.uniadmin.timetable.TimetableScreen
import com.mike.uniadmin.ui.theme.Appearance

@OptIn(ExperimentalPagerApi::class)
@Composable
fun NavigationGraph(context: Context, mainActivity: MainActivity) {
    val navController = rememberNavController()
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    val screens = listOf(
        Screen.Home, Screen.Announcements, Screen.Assignments, Screen.Timetable, Screen.Attendance
    )

    NavHost(navController = navController, startDestination = "uniChat") {

        composable("splashScreen") {
            SplashScreen(navController = navController, context)
        }

        composable("login",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            LoginScreen(navController = navController, context)
        }

        composable("assignments",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            AssignmentScreen(context)
        }

        composable("timetable",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            TimetableScreen(context)
        }

        composable("dashboard",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            Dashboard(navController = navController, context)
        }

        composable("profile",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            ProfileScreen(navController = navController, context)
        }

        composable("settings",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            Settings(navController = navController, context, mainActivity)
        }

        composable("uniChat",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            UniChat(navController = navController, context)
        }

        composable("chat/{userId}",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }, arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            UserChatScreen(
                navController, context, backStackEntry.arguments?.getString("userId") ?: ""
            )
        }

        composable("GroupChat/{groupId}",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }, arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            DiscussionScreen(
                navController, context, backStackEntry.arguments?.getString("groupId") ?: ""
            )
        }

        composable("groups",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            UniGroups(context, navController)
        }

        composable("moreDetails",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            MoreDetails(context, navController)
        }

        composable("announcements",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            AnnouncementsScreen(context)
        }

        composable("manageUsers",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            ManageUsers(navController, context)
        }

        composable("notifications",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            PhoneNotifications(navController, context)
        }

        composable("passwordReset",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            PasswordReset(navController = navController, context)
        }

        composable("appearance",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            Appearance(navController = navController)
        }

        composable("homeScreen",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }) {
            HomeScreen(navController, context, pagerState, mainActivity, screens, coroutineScope)
        }

        composable("courseResource/{courseCode}",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }, arguments = listOf(navArgument("courseCode") { type = NavType.StringType })
        ) { backStackEntry ->
            CourseResources(
                backStackEntry.arguments?.getString("courseCode") ?: "", context
            )
        }

        composable("courseContent/{courseId}",  exitTransition = {
            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
        }, arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            CourseContent(
                context, backStackEntry.arguments?.getString("courseId") ?: ""
            )
        }
    }
}
