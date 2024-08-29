package com.mike.uniadmin

import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mike.uniadmin.authentication.LoginScreen
import com.mike.uniadmin.authentication.MoreDetails
import com.mike.uniadmin.authentication.PasswordReset
import com.mike.uniadmin.courses.CourseScreen
import com.mike.uniadmin.homeScreen.HomeScreen
import com.mike.uniadmin.moduleContent.ModuleContent
import com.mike.uniadmin.moduleResources.DownloadedResources
import com.mike.uniadmin.moduleResources.ModuleResources
import com.mike.uniadmin.notification.PhoneNotifications
import com.mike.uniadmin.profile.ProfileScreen
import com.mike.uniadmin.settings.Settings
import com.mike.uniadmin.ui.theme.Appearance
import com.mike.uniadmin.uniChat.groupChat.DiscussionScreen
import com.mike.uniadmin.uniChat.mainChatScreen.UniChat
import com.mike.uniadmin.uniChat.userChat.UserChatScreen

@Composable
fun NavigationGraph(context: Context, mainActivity: MainActivity) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "uniChat") {

        composable("splashScreen") {
            SplashScreen(navController = navController, context)
        }

        composable(
            "downloads",
            enterTransition = {
                fadeIn(animationSpec = tween(200))
            }
        ) {
            DownloadedResources(context, navController)
        }

        composable("login",
            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            }
        ) {
            LoginScreen(navController = navController, context)
        }

        composable(
            "profile",
            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }
        ) {
            ProfileScreen(navController = navController, context)
        }

        composable("settings",
            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }
        ) {
            Settings(navController = navController, context, mainActivity)
        }

        composable("uniChat",

            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }) {
            UniChat(navController = navController, context)
        }

        composable("chat/{userId}",
            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }, arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            UserChatScreen(
                navController, context, backStackEntry.arguments?.getString("userId") ?: ""
            )
        }

        composable("GroupChat/{groupId}",
            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }, arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            DiscussionScreen(
                navController, context, backStackEntry.arguments?.getString("groupId") ?: ""
            )
        }

        composable("moreDetails",
            exitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(200))
            }) {
            MoreDetails(context, navController)
        }


        composable("notifications",

            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }
        ) {
            PhoneNotifications(navController, context)
        }

        composable("passwordReset",

            exitTransition = {
                slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            PasswordReset(navController = navController, context)
        }

        composable("appearance",

            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }) {
            Appearance(navController = navController)
        }

        composable("homeScreen",
            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }
        ) {
            HomeScreen(navController = navController, context, mainActivity)
        }

        composable("courses",

            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }) {
            CourseScreen(context, navController)
        }

        composable("moduleResource/{moduleCode}",

            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }, arguments = listOf(navArgument("moduleCode") { type = NavType.StringType })
        ) { backStackEntry ->
            ModuleResources(
                backStackEntry.arguments?.getString("moduleCode") ?: "", context, navController
            )
        }

        composable("moduleContent/{moduleId}",

            enterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(600))
            }, arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
        ) { backStackEntry ->
            ModuleContent(
                context, backStackEntry.arguments?.getString("moduleId") ?: ""
            )
        }
    }
}
