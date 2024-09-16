package com.mike.uniadmin.model.localDatabase

import android.app.Application
import com.mike.uniadmin.model.announcements.AnnouncementRepository
import com.mike.uniadmin.model.attendance.AttendanceRepository
import com.mike.uniadmin.model.moduleContent.moduleAnnouncements.ModuleAnnouncementRepository
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignmentRepository
import com.mike.uniadmin.model.moduleContent.moduleDetails.ModuleDetailRepository
import com.mike.uniadmin.model.moduleContent.moduleTimetable.ModuleTimetableRepository
import com.mike.uniadmin.model.modules.ModuleRepository
import com.mike.uniadmin.model.groupchat.GroupChatRepository
import com.mike.uniadmin.model.notifications.NotificationRepository
import com.mike.uniadmin.model.courses.CourseRepository
import com.mike.uniadmin.model.userchat.UserChatRepository
import com.mike.uniadmin.model.users.UserRepository


class UniConnect : Application() {
    val database by lazy { UniConnectDatabase.getDatabase(this) }
    val groupChatRepository by lazy { GroupChatRepository(database.groupChatDao(), database.groupDao()) }
    val userChatRepository by lazy { UserChatRepository(database.userChatDao()) }
    val userRepository by lazy { UserRepository(database.userDao(), database.userStateDao(), database.accountDeletionDao(), database.databaseDao()) }
    val announcementRepository by lazy { AnnouncementRepository(database.announcementsDao()) }
    val notificationRepository by lazy { NotificationRepository(database.notificationDao()) }
    val moduleRepository by lazy { ModuleRepository(database.moduleDao(), database.attendanceStateDao()) }
    val moduleAnnouncementRepository by lazy { ModuleAnnouncementRepository(database.moduleAnnouncementDao()) }
    val moduleAssignmentRepository by lazy { ModuleAssignmentRepository(database.moduleAssignmentDao()) }
    val moduleDetailRepository by lazy { ModuleDetailRepository(database.moduleDetailsDao()) }
    val moduleTimetableRepository by lazy { ModuleTimetableRepository(database.moduleTimetableDao()) }
    val courseRepository by lazy { CourseRepository(database.courseDao(), database.academicYearDao()) }
    val attendanceRepository by lazy { AttendanceRepository(database.attendanceDao()) }


}