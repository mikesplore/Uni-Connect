package com.mike.uniadmin

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mike.uniadmin.backEnd.announcements.AnnouncementViewModel
import com.mike.uniadmin.backEnd.announcements.AnnouncementViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncementViewModel
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncementViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignmentViewModel
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignmentViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetailViewModel
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetailViewModelFactory
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetableViewModel
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetableViewModelFactory
import com.mike.uniadmin.backEnd.courses.CourseViewModel
import com.mike.uniadmin.backEnd.courses.CourseViewModelFactory
import com.mike.uniadmin.backEnd.groupchat.ChatViewModel
import com.mike.uniadmin.backEnd.notifications.NotificationViewModel
import com.mike.uniadmin.backEnd.programs.ProgramViewModel
import com.mike.uniadmin.backEnd.programs.ProgramViewModelFactory
import com.mike.uniadmin.backEnd.userchat.MessageViewModel
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.localDatabase.UniAdmin


//messageViewModel
@Composable
fun getMessageViewModel(context: Context): MessageViewModel {
    val application = context.applicationContext as UniAdmin
    val messageRepository = application.messageRepository
    return viewModel(factory = MessageViewModel.MessageViewModelFactory(messageRepository))
}

//chatViewModel
@Composable
fun getChatViewModel(context: Context): ChatViewModel {
    val application = context.applicationContext as UniAdmin
    val chatRepository = application.chatRepository
    return viewModel(factory = ChatViewModel.ChatViewModelFactory(chatRepository))
}

//userViewModel
@Composable
fun getUserViewModel(context: Context): UserViewModel {
    val application = context.applicationContext as UniAdmin
    val userRepository = application.userRepository
    return viewModel(factory = UserViewModelFactory(userRepository))
}


//CourseViewModel
@Composable
fun getCourseViewModel(context: Context): CourseViewModel {
    val courseResource = context.applicationContext as UniAdmin
    val courseRepository = remember { courseResource.courseRepository }
    return viewModel(factory = CourseViewModelFactory(courseRepository))
}

//CourseAnnouncementViewModel
@Composable
fun getCourseAnnouncementViewModel(context: Context): CourseAnnouncementViewModel {
    val courseResource = context.applicationContext as UniAdmin
    val courseAnnouncementRepository = remember { courseResource.courseAnnouncementRepository }
    return viewModel(factory = CourseAnnouncementViewModelFactory(courseAnnouncementRepository))
}

//CourseAssignmentViewModel
@Composable
fun getCourseAssignmentViewModel(context: Context): CourseAssignmentViewModel {
    val courseResource = context.applicationContext as UniAdmin
    val courseAssignmentRepository = remember { courseResource.courseAssignmentRepository }
    return viewModel(factory = CourseAssignmentViewModelFactory(courseAssignmentRepository))
}

//CourseDetailViewModel
@Composable
fun getCourseDetailViewModel(context: Context): CourseDetailViewModel {
    val courseResource = context.applicationContext as UniAdmin
    val courseDetailRepository = remember { courseResource.courseDetailRepository }
    return viewModel(factory = CourseDetailViewModelFactory(courseDetailRepository))
}

//CourseTimetableViewModel
@Composable
fun getCourseTimetableViewModel(context: Context): CourseTimetableViewModel {
    val courseResource = context.applicationContext as UniAdmin
    val courseTimetableRepository = remember { courseResource.courseTimetableRepository }
    return viewModel(factory = CourseTimetableViewModelFactory(courseTimetableRepository))
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
    val announcementResource = context.applicationContext as UniAdmin
    val announcementRepository = remember { announcementResource.announcementRepository }
    return viewModel(factory = AnnouncementViewModelFactory(announcementRepository))
}

//ProgramViewModel
@Composable
fun getProgramViewModel(context: Context): ProgramViewModel {
    val programResource = context.applicationContext as UniAdmin
    val programRepository = remember { programResource.programRepository }
    return viewModel(factory = ProgramViewModelFactory(programRepository))
}

