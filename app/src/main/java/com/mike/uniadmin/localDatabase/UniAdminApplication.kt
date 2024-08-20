package com.mike.uniadmin.localDatabase

import android.app.Application
import com.mike.uniadmin.backEnd.announcements.AnnouncementRepository
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncementRepository
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignmentRepository
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetailRepository
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetableRepository
import com.mike.uniadmin.backEnd.courses.CourseRepository
import com.mike.uniadmin.backEnd.groupchat.ChatRepository
import com.mike.uniadmin.backEnd.notifications.NotificationRepository
import com.mike.uniadmin.backEnd.programs.ProgramRepository
import com.mike.uniadmin.backEnd.userchat.MessageRepository
import com.mike.uniadmin.backEnd.users.UserRepository


class UniAdmin : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val chatRepository by lazy { ChatRepository(database.chatDao(), database.groupDao()) }
    val messageRepository by lazy { MessageRepository(database.messageDao()) }
    val userRepository by lazy { UserRepository(database.userDao(), database.userStateDao(), database.accountDeletionDao(), database.userPreferencesDao(), database.databaseDao()) }
    val announcementRepository by lazy { AnnouncementRepository(database.announcementsDao()) }
    val notificationRepository by lazy { NotificationRepository(database.notificationDao()) }
    val courseRepository by lazy { CourseRepository(database.courseDao(), database.attendanceStateDao()) }
    val courseAnnouncementRepository by lazy { CourseAnnouncementRepository(database.courseAnnouncementDao()) }
    val courseAssignmentRepository by lazy { CourseAssignmentRepository(database.courseAssignmentDao()) }
    val courseDetailRepository by lazy { CourseDetailRepository(database.courseDetailsDao()) }
    val courseTimetableRepository by lazy { CourseTimetableRepository(database.courseTimetableDao()) }
    val programRepository by lazy { ProgramRepository(database.programDao(), database.programStateDao()) }

}