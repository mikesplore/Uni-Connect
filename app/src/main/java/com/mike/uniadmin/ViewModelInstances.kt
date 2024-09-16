package com.mike.uniadmin

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mike.uniadmin.model.announcements.AnnouncementViewModel
import com.mike.uniadmin.model.announcements.AnnouncementViewModelFactory
import com.mike.uniadmin.model.attendance.AttendanceViewModel
import com.mike.uniadmin.model.moduleContent.moduleAnnouncements.ModuleAnnouncementViewModel
import com.mike.uniadmin.model.moduleContent.moduleAnnouncements.ModuleAnnouncementViewModelFactory
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignmentViewModel
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignmentViewModelFactory
import com.mike.uniadmin.model.moduleContent.moduleDetails.ModuleDetailViewModel
import com.mike.uniadmin.model.moduleContent.moduleDetails.ModuleDetailViewModelFactory
import com.mike.uniadmin.model.moduleContent.moduleTimetable.ModuleTimetableViewModel
import com.mike.uniadmin.model.moduleContent.moduleTimetable.ModuleTimetableViewModelFactory
import com.mike.uniadmin.model.modules.ModuleViewModel
import com.mike.uniadmin.model.modules.ModuleViewModelFactory
import com.mike.uniadmin.model.groupchat.GroupChatViewModel
import com.mike.uniadmin.model.notifications.NotificationViewModel
import com.mike.uniadmin.model.courses.CourseViewModel
import com.mike.uniadmin.model.courses.CourseViewModelFactory
import com.mike.uniadmin.model.userchat.UserChatViewModel
import com.mike.uniadmin.model.users.UserViewModel
import com.mike.uniadmin.model.users.UserViewModelFactory
import com.mike.uniadmin.model.localDatabase.UniConnect
import com.mike.uniadmin.model.attendance.AttendanceViewModel.AttendanceViewModelFactory

//messageViewModel
@Composable
fun getUserChatViewModel(context: Context): UserChatViewModel {
    val application = context.applicationContext as UniConnect
    val userChatRepository = application.userChatRepository
    return viewModel(factory = UserChatViewModel.UserChatViewModelFactory(userChatRepository))
}

//chatViewModel
@Composable
fun getGroupChatViewModel(context: Context): GroupChatViewModel {
    val application = context.applicationContext as UniConnect
    val chatRepository = application.groupChatRepository
    return viewModel(factory = GroupChatViewModel.GroupChatViewModelFactory(chatRepository))
}

//userViewModel
@Composable
fun getUserViewModel(context: Context): UserViewModel {
    val application = context.applicationContext as UniConnect
    val userRepository = application.userRepository
    return viewModel(factory = UserViewModelFactory(userRepository))
}


//ModuleViewModel
@Composable
fun getModuleViewModel(context: Context): ModuleViewModel {
    val moduleResource = context.applicationContext as UniConnect
    val moduleRepository = remember { moduleResource.moduleRepository }
    return viewModel(factory = ModuleViewModelFactory(moduleRepository))
}

//ModuleAnnouncementViewModel
@Composable
fun getModuleAnnouncementViewModel(context: Context): ModuleAnnouncementViewModel {
    val moduleResource = context.applicationContext as UniConnect
    val moduleAnnouncementRepository = remember { moduleResource.moduleAnnouncementRepository }
    return viewModel(factory = ModuleAnnouncementViewModelFactory(moduleAnnouncementRepository))
}

//ModuleAssignmentViewModel
@Composable
fun getModuleAssignmentViewModel(context: Context): ModuleAssignmentViewModel {
    val moduleResource = context.applicationContext as UniConnect
    val moduleAssignmentRepository = remember { moduleResource.moduleAssignmentRepository }
    return viewModel(factory = ModuleAssignmentViewModelFactory(moduleAssignmentRepository))
}

//ModuleDetailViewModel
@Composable
fun getModuleDetailViewModel(context: Context): ModuleDetailViewModel {
    val moduleResource = context.applicationContext as UniConnect
    val moduleDetailRepository = remember { moduleResource.moduleDetailRepository }
    return viewModel(factory = ModuleDetailViewModelFactory(moduleDetailRepository))
}

//ModuleTimetableViewModel
@Composable
fun getModuleTimetableViewModel(context: Context): ModuleTimetableViewModel {
    val moduleResource = context.applicationContext as UniConnect
    val moduleTimetableRepository = remember { moduleResource.moduleTimetableRepository }
    return viewModel(factory = ModuleTimetableViewModelFactory(moduleTimetableRepository))
}

//AnnouncementViewModel
@Composable
fun getAnnouncementViewModel(context: Context): AnnouncementViewModel {
    val announcementResource = context.applicationContext as UniConnect
    val announcementRepository = remember { announcementResource.announcementRepository }
    return viewModel(factory = AnnouncementViewModelFactory(announcementRepository))
}

//Notification ViewModel
@Composable
fun getNotificationViewModel(context: Context): NotificationViewModel {
    val notificationResource = context.applicationContext as UniConnect
    val notificationRepository = remember { notificationResource.notificationRepository }
    return viewModel(factory = NotificationViewModel.NotificationViewModelFactory(notificationRepository))
}

//CourseViewModel
@Composable
fun getCourseViewModel(context: Context): CourseViewModel {
    val courseResource = context.applicationContext as UniConnect
    val courseRepository = remember { courseResource.courseRepository }
    return viewModel(factory = CourseViewModelFactory(courseRepository))
}

//AttendanceViewModel
@Composable
fun getAttendanceViewModel(context: Context): AttendanceViewModel {
    val attendanceResource = context.applicationContext as UniConnect
    val attendanceRepository = remember { attendanceResource.attendanceRepository }
    return viewModel(factory = AttendanceViewModelFactory(attendanceRepository))
}

