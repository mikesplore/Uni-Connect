package com.mike.uniadmin

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mike.uniadmin.backEnd.announcements.AnnouncementViewModel
import com.mike.uniadmin.backEnd.announcements.AnnouncementViewModelFactory
import com.mike.uniadmin.backEnd.attendance.AttendanceViewModel
import com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements.ModuleAnnouncementViewModel
import com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements.ModuleAnnouncementViewModelFactory
import com.mike.uniadmin.backEnd.moduleContent.moduleAssignments.ModuleAssignmentViewModel
import com.mike.uniadmin.backEnd.moduleContent.moduleAssignments.ModuleAssignmentViewModelFactory
import com.mike.uniadmin.backEnd.moduleContent.moduleDetails.ModuleDetailViewModel
import com.mike.uniadmin.backEnd.moduleContent.moduleDetails.ModuleDetailViewModelFactory
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetableViewModel
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetableViewModelFactory
import com.mike.uniadmin.backEnd.modules.ModuleViewModel
import com.mike.uniadmin.backEnd.modules.ModuleViewModelFactory
import com.mike.uniadmin.backEnd.groupchat.GroupChatViewModel
import com.mike.uniadmin.backEnd.notifications.NotificationViewModel
import com.mike.uniadmin.backEnd.courses.CourseViewModel
import com.mike.uniadmin.backEnd.courses.CourseViewModelFactory
import com.mike.uniadmin.backEnd.userchat.UserChatViewModel
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.localDatabase.UniAdmin
import com.mike.uniadmin.backEnd.attendance.AttendanceViewModel.AttendanceViewModelFactory

//messageViewModel
@Composable
fun getUserChatViewModel(context: Context): UserChatViewModel {
    val application = context.applicationContext as UniAdmin
    val userChatRepository = application.userChatRepository
    return viewModel(factory = UserChatViewModel.UserChatViewModelFactory(userChatRepository))
}

//chatViewModel
@Composable
fun getGroupChatViewModel(context: Context): GroupChatViewModel {
    val application = context.applicationContext as UniAdmin
    val chatRepository = application.groupChatRepository
    return viewModel(factory = GroupChatViewModel.GroupChatViewModelFactory(chatRepository))
}

//userViewModel
@Composable
fun getUserViewModel(context: Context): UserViewModel {
    val application = context.applicationContext as UniAdmin
    val userRepository = application.userRepository
    return viewModel(factory = UserViewModelFactory(userRepository))
}


//ModuleViewModel
@Composable
fun getModuleViewModel(context: Context): ModuleViewModel {
    val moduleResource = context.applicationContext as UniAdmin
    val moduleRepository = remember { moduleResource.moduleRepository }
    return viewModel(factory = ModuleViewModelFactory(moduleRepository))
}

//ModuleAnnouncementViewModel
@Composable
fun getModuleAnnouncementViewModel(context: Context): ModuleAnnouncementViewModel {
    val moduleResource = context.applicationContext as UniAdmin
    val moduleAnnouncementRepository = remember { moduleResource.moduleAnnouncementRepository }
    return viewModel(factory = ModuleAnnouncementViewModelFactory(moduleAnnouncementRepository))
}

//ModuleAssignmentViewModel
@Composable
fun getModuleAssignmentViewModel(context: Context): ModuleAssignmentViewModel {
    val moduleResource = context.applicationContext as UniAdmin
    val moduleAssignmentRepository = remember { moduleResource.moduleAssignmentRepository }
    return viewModel(factory = ModuleAssignmentViewModelFactory(moduleAssignmentRepository))
}

//ModuleDetailViewModel
@Composable
fun getModuleDetailViewModel(context: Context): ModuleDetailViewModel {
    val moduleResource = context.applicationContext as UniAdmin
    val moduleDetailRepository = remember { moduleResource.moduleDetailRepository }
    return viewModel(factory = ModuleDetailViewModelFactory(moduleDetailRepository))
}

//ModuleTimetableViewModel
@Composable
fun getModuleTimetableViewModel(context: Context): ModuleTimetableViewModel {
    val moduleResource = context.applicationContext as UniAdmin
    val moduleTimetableRepository = remember { moduleResource.moduleTimetableRepository }
    return viewModel(factory = ModuleTimetableViewModelFactory(moduleTimetableRepository))
}

//AnnouncementViewModel
@Composable
fun getAnnouncementViewModel(context: Context): AnnouncementViewModel {
    val announcementResource = context.applicationContext as UniAdmin
    val announcementRepository = remember { announcementResource.announcementRepository }
    return viewModel(factory = AnnouncementViewModelFactory(announcementRepository))
}

//Notification ViewModel
@Composable
fun getNotificationViewModel(context: Context): NotificationViewModel {
    val notificationResource = context.applicationContext as UniAdmin
    val notificationRepository = remember { notificationResource.notificationRepository }
    return viewModel(factory = NotificationViewModel.NotificationViewModelFactory(notificationRepository))
}

//CourseViewModel
@Composable
fun getCourseViewModel(context: Context): CourseViewModel {
    val courseResource = context.applicationContext as UniAdmin
    val courseRepository = remember { courseResource.courseRepository }
    return viewModel(factory = CourseViewModelFactory(courseRepository))
}

//AttendanceViewModel
@Composable
fun getAttendanceViewModel(context: Context): AttendanceViewModel {
    val attendanceResource = context.applicationContext as UniAdmin
    val attendanceRepository = remember { attendanceResource.attendanceRepository }
    return viewModel(factory = AttendanceViewModelFactory(attendanceRepository))
}

