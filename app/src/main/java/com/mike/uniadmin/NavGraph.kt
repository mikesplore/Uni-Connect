package com.mike.uniadmin

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.mike.uniadmin.announcements.AnnouncementsScreen
import com.mike.uniadmin.assignments.AssignmentScreen
import com.mike.uniadmin.attendance.ManageAttendanceScreen
import com.mike.uniadmin.authentication.LoginScreen
import com.mike.uniadmin.authentication.MoreDetails
import com.mike.uniadmin.authentication.PasswordReset
import com.mike.uniadmin.chat.AddGroupSection
import com.mike.uniadmin.chat.DiscussionScreen
import com.mike.uniadmin.chat.ParticipantsScreen
import com.mike.uniadmin.chat.UniChat
import com.mike.uniadmin.chat.UniGroups
import com.mike.uniadmin.chat.UniScreen
import com.mike.uniadmin.chat.UserChatScreen
import com.mike.uniadmin.courseContent.CourseContent
import com.mike.uniadmin.courseResources.CourseResources
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.ManageUsers
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.home.Dashboard
import com.mike.uniadmin.home.HomeScreen
import com.mike.uniadmin.model.Screen
import com.mike.uniadmin.notification.PhoneNotifications
import com.mike.uniadmin.settings.Settings
import com.mike.uniadmin.timetable.TimetableScreen
import com.mike.uniadmin.ui.theme.Appearance

@OptIn(ExperimentalPagerApi::class)
@Composable
fun NavigationGraph(context: Context,  mainActivity: MainActivity){
    val navController = rememberNavController()
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    val application = context.applicationContext as UniAdmin
    val chatRepository = remember { application.chatRepository }
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.ChatViewModelFactory(chatRepository)
    )
    val userAdmin = context.applicationContext as? UniAdmin
    val userRepository = remember { userAdmin?.userRepository }
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            userRepository ?: throw IllegalStateException("UserRepository is null")
        )
    )

    val users by userViewModel.users.observeAsState(emptyList())
    val signedInUser by remember { mutableStateOf(UserEntity())}
    val screens = listOf(
        Screen.Home, Screen.Announcements, Screen.Assignments, Screen.Timetable, Screen.Attendance
    )
    val uniChatScreens = listOf(
        UniScreen.Chats, UniScreen.Groups, UniScreen.Status
    )
    NavHost(navController = navController, startDestination = "homescreen"){

        composable("splashscreen"){
            SplashScreen(navController = navController, context)
        }
//        composable("videos"){
//            Videos(navController = navController, context)
//        }
        composable("login"){
            LoginScreen(navController = navController, context)
        }
        composable("addgroup"){
            AddGroupSection(signedInUser,context,chatViewModel,users)

        }

        composable("assignments"){
            AssignmentScreen(context)
        }

        composable("timetable"){
            TimetableScreen(context)
        }

        composable("manageattendance"){
            ManageAttendanceScreen(context)
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
                context,
                backStackEntry.arguments?.getString("userId") ?: ""
            )
        }
        composable("GroupChat/{groupId}", enterTransition = {
            fadeIn(animationSpec = tween(1000)) + slideInVertically(animationSpec = tween(1000)) { initialState -> initialState }
        }, exitTransition = {
            fadeOut(animationSpec = tween(1000)) + slideOutVertically(animationSpec = tween(1000)) { finalState -> finalState }
        }, arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            DiscussionScreen(
                navController,
                context,
                backStackEntry.arguments?.getString("groupId") ?: ""
            )
        }

        composable("groups"){
            UniGroups(context, navController)
        }

        composable("moreDetails"){
            MoreDetails(context, navController)

        }

        composable("announcements"){
            AnnouncementsScreen(context)
        }

        composable("manageusers"){
            ManageUsers(navController, context)
        }

        composable("notifications"){
            PhoneNotifications(navController, context)
        }

//        composable("statistics"){
//            Statistics(navController, context)
//        }


//        composable("assignments"){
//            AssignmentScreen(navController = navController, context)
//        }

        composable("passwordreset",
            enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500))
        }) {
            PasswordReset(navController = navController, context)
        }

        composable("appearance"){
            Appearance(navController = navController, context)
        }

        composable("homescreen"){
            HomeScreen(navController,context,pagerState,mainActivity,screens,coroutineScope)
        }
        composable("unichat"){
            UniChat(navController,context,pagerState, uniChatScreens, coroutineScope  )
        }

        composable("courseResource/{courseCode}",
            arguments = listOf(navArgument("courseCode") { type = NavType.StringType })
        ) { backStackEntry ->
            CourseResources(
                backStackEntry.arguments?.getString("courseCode") ?: "",
                context
            )
        }

        composable("courseContent/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            CourseContent(
                context,
                backStackEntry.arguments?.getString("courseId") ?: ""
            )
        }
    }
}