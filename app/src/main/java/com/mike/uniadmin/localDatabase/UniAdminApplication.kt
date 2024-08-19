package com.mike.uniadmin.localDatabase

import android.app.Application
import com.mike.uniadmin.dataModel.announcements.AnnouncementRepository
import com.mike.uniadmin.dataModel.coursecontent.courseannouncements.CourseAnnouncementRepository
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignmentRepository
import com.mike.uniadmin.dataModel.coursecontent.coursedetails.CourseDetailRepository
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableRepository
import com.mike.uniadmin.dataModel.courses.CourseRepository
import com.mike.uniadmin.dataModel.groupchat.ChatRepository
import com.mike.uniadmin.dataModel.notifications.NotificationRepository
import com.mike.uniadmin.dataModel.programs.ProgramRepository
import com.mike.uniadmin.dataModel.userchat.MessageRepository
import com.mike.uniadmin.dataModel.users.UserRepository


class UniAdmin : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val chatRepository by lazy { ChatRepository(database.chatDao(), database.groupDao()) }
    val messageRepository by lazy { MessageRepository(database.messageDao(), database.userDao()) }
    val userRepository by lazy { UserRepository(database.userDao(), database.userStateDao(), database.accountDeletionDao(), database.userPreferencesDao(), database.databaseDao()) }
    val announcementRepository by lazy { AnnouncementRepository(database.announcementsDao()) }
    val notificationRepository by lazy { NotificationRepository(database.notificationDao()) }
    val courseRepository by lazy { CourseRepository(database.courseDao(), database.attendanceStateDao(), database.programDao()) }
    val courseAnnouncementRepository by lazy { CourseAnnouncementRepository(database.courseAnnouncementDao()) }
    val courseAssignmentRepository by lazy { CourseAssignmentRepository(database.courseAssignmentDao()) }
    val courseDetailRepository by lazy { CourseDetailRepository(database.courseDetailsDao()) }
    val courseTimetableRepository by lazy { CourseTimetableRepository(database.courseTimetableDao()) }
    val programRepository by lazy { ProgramRepository(database.programDao(), database.programStateDao()) }

}