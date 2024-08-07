package com.mike.uniadmin

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
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

        composable("login", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }

        ) {
            LoginScreen(navController = navController, context)
        }

        composable("assignments", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            AssignmentScreen(context)
        }

        composable("timetable", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            TimetableScreen(context)
        }

        composable("dashboard", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            Dashboard(navController = navController, context)
        }

        composable("profile", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            ProfileScreen(navController = navController, context)
        }

        composable("settings", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            Settings(navController = navController, context, mainActivity)
        }

        composable("uniChat", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            UniChat(navController = navController, context)
        }

        composable("chat/{userId}", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }, arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            UserChatScreen(
                navController, context, backStackEntry.arguments?.getString("userId") ?: ""
            )
        }

        composable("GroupChat/{groupId}", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }, arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            DiscussionScreen(
                navController, context, backStackEntry.arguments?.getString("groupId") ?: ""
            )
        }

        composable("groups", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            UniGroups(context, navController)
        }

        composable("moreDetails", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            MoreDetails(context, navController)
        }

        composable("announcements", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            AnnouncementsScreen(context)
        }

        composable("manageUsers", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            ManageUsers(navController, context)
        }

        composable("notifications", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            PhoneNotifications(navController, context)
        }

        composable("passwordReset", enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500))
        }) {
            PasswordReset(navController = navController, context)
        }

        composable("appearance", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            Appearance(navController = navController, context)
        }

        composable("homeScreen", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }) {
            HomeScreen(navController, context, pagerState, mainActivity, screens, coroutineScope)
        }

        composable("courseResource/{courseCode}", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }, arguments = listOf(navArgument("courseCode") { type = NavType.StringType })
        ) { backStackEntry ->
            CourseResources(
                backStackEntry.arguments?.getString("courseCode") ?: "", context
            )
        }

        composable("courseContent/{courseId}", enterTransition = {
            fadeIn(animationSpec = tween(500))
        }, exitTransition = {
            fadeOut(animationSpec = tween(500))
        }, arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            CourseContent(
                context, backStackEntry.arguments?.getString("courseId") ?: ""
            )
        }
    }
}
