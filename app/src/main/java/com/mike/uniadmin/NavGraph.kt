package com.mike.uniadmin

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mike.uniadmin.announcements.AnnouncementsScreen
import com.mike.uniadmin.assignments.AssignmentScreen
import com.mike.uniadmin.authentication.LoginScreen
import com.mike.uniadmin.authentication.MoreDetails
import com.mike.uniadmin.authentication.PasswordReset
import com.mike.uniadmin.uniChat.groupChat.DiscussionScreen
import com.mike.uniadmin.uniChat.mainChatScreen.UniChat
import com.mike.uniadmin.uniChat.userChat.UserChatScreen
import com.mike.uniadmin.moduleContent.ModuleContent
import com.mike.uniadmin.moduleResources.ModuleResources
import com.mike.uniadmin.dashboard.Dashboard
import com.mike.uniadmin.homeScreen.HomeScreen
import com.mike.uniadmin.notification.PhoneNotifications
import com.mike.uniadmin.courses.CourseScreen
import com.mike.uniadmin.profile.ProfileScreen
import com.mike.uniadmin.settings.Settings
import com.mike.uniadmin.ui.theme.Appearance


@Composable
fun NavigationGraph(context: Context, mainActivity: MainActivity) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "homeScreen") {

        composable("splashScreen") {
            SplashScreen(navController = navController, context)
        }

        composable("login", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            LoginScreen(navController = navController, context)
        }

        composable("assignments", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            AssignmentScreen(context)
        }

        composable("dashboard", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            Dashboard(navController = navController, context)
        }

        composable("profile", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            ProfileScreen(navController = navController, context)
        }

        composable("settings", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            Settings(navController = navController, context, mainActivity)
        }

        composable("uniChat", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            UniChat(navController = navController, context)
        }

        composable("chat/{userId}", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }, arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            UserChatScreen(
                navController, context, backStackEntry.arguments?.getString("userId") ?: ""
            )
        }

        composable("GroupChat/{groupId}", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }, arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            DiscussionScreen(
                navController, context, backStackEntry.arguments?.getString("groupId") ?: ""
            )
        }

        composable("moreDetails", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            MoreDetails(context, navController)
        }

        composable("announcements", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            AnnouncementsScreen(context)
        }

        composable("notifications", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            PhoneNotifications(navController, context)
        }

        composable("passwordReset", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            PasswordReset(navController = navController, context)
        }

        composable("appearance", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            Appearance(navController = navController)
        }

        composable("homeScreen", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }) {
            HomeScreen(navController = navController, context, mainActivity)
        }

        composable("courses") {
            CourseScreen(context, navController)
        }

        composable("moduleResource/{moduleCode}", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }, arguments = listOf(navArgument("moduleCode") { type = NavType.StringType })
        ) { backStackEntry ->
            ModuleResources(
                backStackEntry.arguments?.getString("moduleCode") ?: "", context
            )
        }

        composable("moduleContent/{moduleId}", exitTransition = {
            fadeOut(animationSpec = tween(300))
        }, arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
        ) { backStackEntry ->
            ModuleContent(
                context, backStackEntry.arguments?.getString("moduleId") ?: ""
            )
        }
    }
}
