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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.auth.FirebaseAuth
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
import com.mike.uniadmin.dataModel.courses.CourseRepository
import com.mike.uniadmin.dataModel.courses.CourseViewModel
import com.mike.uniadmin.dataModel.courses.CourseViewModelFactory
import com.mike.uniadmin.dataModel.groupchat.ChatViewModel
import com.mike.uniadmin.dataModel.groupchat.UniAdmin
import com.mike.uniadmin.dataModel.users.User
import com.mike.uniadmin.dataModel.users.UserRepository
import com.mike.uniadmin.dataModel.users.UserViewModel
import com.mike.uniadmin.dataModel.users.UserViewModelFactory
import com.mike.uniadmin.model.Screen
import com.mike.uniadmin.settings.Settings
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
    val userRepository = remember { UserRepository() }
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))
    val groups by chatViewModel.groups.observeAsState(emptyList())
    val users by userViewModel.users.observeAsState(emptyList())
    val user by userViewModel.user.observeAsState(initial = null)
    var showAddGroup by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    var signedInUser by remember { mutableStateOf(User()) }

    val screens = listOf(
        Screen.Home, Screen.Announcements, Screen.Assignments, Screen.Timetable, Screen.Attendance
    )
    val uniChatScreens = listOf(
        UniScreen.Chats, UniScreen.Groups, UniScreen.Status
    )
    NavHost(navController = navController, startDestination = "splashscreen"){

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

        composable("moredetails"){
            MoreDetails(context, navController)

        }

//        composable("announcements"){
//            AnnouncementsScreen(navController = navController, context)
//        }

//        composable("timetable"){
//            TimetableScreen(navController = navController, context)
//        }
//        composable("assignments"){
//            AssignmentScreen(navController = navController, context)
//        }
//        composable("attendance"){
//            ManageAttendanceScreen(navController = navController, context)
//        }
        composable("passwordreset"){
            PasswordReset(navController = navController, context)
        }

        composable("appearance"){
            Appearance(navController = navController, context)
        }

        composable("homescreen"){
            HomeScreen(navController,context,pagerState,mainActivity,screens,coroutineScope)
        }
        composable("unichat"){
            UniChat(navController,context,pagerState, mainActivity,uniChatScreens, coroutineScope  )
        }

//        composable("course/{courseCode}",
//            arguments = listOf(navArgument("courseCode") { type = NavType.StringType })
//        ) { backStackEntry ->
//            CourseScreen(
//                backStackEntry.arguments?.getString("courseCode") ?: "",
//                context
//            )
//        }
//
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