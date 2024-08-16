package com.mike.uniadmin.dataModel.groupchat
import androidx.room.Entity
import androidx.room.PrimaryKey
import android.app.Application
import com.mike.uniadmin.dataModel.announcements.AnnouncementRepository
import com.mike.uniadmin.dataModel.coursecontent.courseannouncements.CourseAnnouncementRepository
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignmentRepository
import com.mike.uniadmin.dataModel.coursecontent.coursedetails.CourseDetailRepository
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableRepository
import com.mike.uniadmin.dataModel.courses.CourseRepository
import com.mike.uniadmin.dataModel.notifications.NotificationRepository
import com.mike.uniadmin.dataModel.userchat.MessageRepository
import com.mike.uniadmin.dataModel.users.UserRepository
import com.mike.uniadmin.localDatabase.AppDatabase

fun generateConversationId(userId1: String, userId2: String): String {
    return if (userId1 < userId2) {
        "$userId1$userId2"
    } else {
        "$userId2$userId1"
    }
}


@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    var message: String = "",
    var senderName: String = "",
    var senderID: String = "",
    var time: String = "",
    var date: String = "",
    var profileImageLink: String = ""
){
    constructor(): this("", "", "", "", "", "", "")
}

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val admin: String = "",
    var name: String = "",
    var description: String = "",
    var groupImageLink: String = "",
    var members: List<String> = emptyList()
){
    constructor(): this("", "", "", "", "", emptyList())
}


class UniAdmin : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val chatRepository by lazy { ChatRepository(database.chatDao(), database.groupDao()) }
    val messageRepository by lazy { MessageRepository(database.messageDao(), database.userDao()) }
    val userRepository by lazy { UserRepository(database.userDao(), database.userStateDao(), database.accountDeletionDao(), database.userPreferencesDao(), database.databaseDao()) }
    val announcementRepository by lazy { AnnouncementRepository(database.announcementsDao()) }
    val notificationRepository by lazy { NotificationRepository(database.notificationDao()) }
    val courseRepository by lazy {CourseRepository(database.courseDao(), database.attendanceStateDao())}
    val courseAnnouncementRepository by lazy { CourseAnnouncementRepository(database.courseAnnouncementDao()) }
    val courseAssignmentRepository by lazy { CourseAssignmentRepository(database.courseAssignmentDao()) }
    val courseDetailRepository by lazy { CourseDetailRepository(database.courseDetailsDao()) }
    val courseTimetableRepository by lazy { CourseTimetableRepository(database.courseTimetableDao()) }
}
