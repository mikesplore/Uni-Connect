package com.mike.uniadmin.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mike.uniadmin.backEnd.announcements.AnnouncementEntity
import com.mike.uniadmin.backEnd.announcements.AnnouncementsDao
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncement
import com.mike.uniadmin.backEnd.coursecontent.courseannouncements.CourseAnnouncementDao
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignment
import com.mike.uniadmin.backEnd.coursecontent.courseassignments.CourseAssignmentDao
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetail
import com.mike.uniadmin.backEnd.coursecontent.coursedetails.CourseDetailDao
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetable
import com.mike.uniadmin.backEnd.coursecontent.coursetimetable.CourseTimetableDao
import com.mike.uniadmin.backEnd.courses.AttendanceState
import com.mike.uniadmin.backEnd.courses.AttendanceStateDao
import com.mike.uniadmin.backEnd.courses.CourseDao
import com.mike.uniadmin.backEnd.courses.CourseEntity
import com.mike.uniadmin.backEnd.groupchat.ChatDao
import com.mike.uniadmin.backEnd.groupchat.ChatEntity
import com.mike.uniadmin.backEnd.groupchat.GroupDao
import com.mike.uniadmin.backEnd.groupchat.GroupEntity
import com.mike.uniadmin.backEnd.notifications.NotificationDao
import com.mike.uniadmin.backEnd.notifications.NotificationEntity
import com.mike.uniadmin.backEnd.programs.ProgramDao
import com.mike.uniadmin.backEnd.programs.ProgramEntity
import com.mike.uniadmin.backEnd.programs.ProgramState
import com.mike.uniadmin.backEnd.programs.ProgramStateDao
import com.mike.uniadmin.backEnd.userchat.MessageDao
import com.mike.uniadmin.backEnd.userchat.MessageEntity
import com.mike.uniadmin.backEnd.users.AccountDeletionDao
import com.mike.uniadmin.backEnd.users.AccountDeletionEntity
import com.mike.uniadmin.backEnd.users.DatabaseDao
import com.mike.uniadmin.backEnd.users.SignedInUser
import com.mike.uniadmin.backEnd.users.UserDao
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.backEnd.users.UserPreferencesDao
import com.mike.uniadmin.backEnd.users.UserPreferencesEntity
import com.mike.uniadmin.backEnd.users.UserStateDao
import com.mike.uniadmin.backEnd.users.UserStateEntity

@Database(
    entities = [
        ChatEntity::class,
        GroupEntity::class,
        MessageEntity::class,
        UserEntity::class,
        AccountDeletionEntity::class,
        UserPreferencesEntity::class,
        UserStateEntity::class,
        AnnouncementEntity::class,
        NotificationEntity::class,
        CourseEntity::class,
        CourseAnnouncement::class,
        CourseAssignment::class,
        CourseDetail::class,
        CourseTimetable::class,
        AttendanceState::class,
        SignedInUser::class,
        ProgramEntity::class,
        ProgramState::class,
               ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun groupDao(): GroupDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
    abstract fun accountDeletionDao(): AccountDeletionDao
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun userStateDao(): UserStateDao
    abstract fun announcementsDao(): AnnouncementsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun courseDao(): CourseDao
    abstract fun courseAnnouncementDao(): CourseAnnouncementDao
    abstract fun courseAssignmentDao(): CourseAssignmentDao
    abstract fun courseDetailsDao(): CourseDetailDao
    abstract fun courseTimetableDao(): CourseTimetableDao
    abstract fun attendanceStateDao(): AttendanceStateDao
    abstract fun databaseDao(): DatabaseDao
    abstract fun programDao(): ProgramDao
    abstract fun programStateDao(): ProgramStateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "Uni_Admin"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


