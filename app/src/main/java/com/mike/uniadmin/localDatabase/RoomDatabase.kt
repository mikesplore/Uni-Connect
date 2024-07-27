package com.mike.uniadmin.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mike.uniadmin.dataModel.announcements.AnnouncementEntity
import com.mike.uniadmin.dataModel.announcements.AnnouncementsDao
import com.mike.uniadmin.dataModel.coursecontent.courseannouncements.CourseAnnouncement
import com.mike.uniadmin.dataModel.coursecontent.courseannouncements.CourseAnnouncementDao
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignment
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignmentDao
import com.mike.uniadmin.dataModel.coursecontent.coursedetails.CourseDetail
import com.mike.uniadmin.dataModel.coursecontent.coursedetails.CourseDetailDao
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetable
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetableDao
import com.mike.uniadmin.dataModel.courses.AttendanceState
import com.mike.uniadmin.dataModel.courses.AttendanceStateDao
import com.mike.uniadmin.dataModel.courses.CourseDao
import com.mike.uniadmin.dataModel.courses.CourseEntity
import com.mike.uniadmin.dataModel.groupchat.ChatDao
import com.mike.uniadmin.dataModel.groupchat.ChatEntity
import com.mike.uniadmin.dataModel.groupchat.Converters
import com.mike.uniadmin.dataModel.groupchat.GroupDao
import com.mike.uniadmin.dataModel.groupchat.GroupEntity
import com.mike.uniadmin.dataModel.notifications.NotificationDao
import com.mike.uniadmin.dataModel.notifications.NotificationEntity
import com.mike.uniadmin.dataModel.userchat.MessageDao
import com.mike.uniadmin.dataModel.userchat.MessageEntity
import com.mike.uniadmin.dataModel.users.AccountDeletionDao
import com.mike.uniadmin.dataModel.users.AccountDeletionEntity
import com.mike.uniadmin.dataModel.users.UserDao
import com.mike.uniadmin.dataModel.users.UserEntity
import com.mike.uniadmin.dataModel.users.UserPreferencesDao
import com.mike.uniadmin.dataModel.users.UserPreferencesEntity
import com.mike.uniadmin.dataModel.users.UserStateDao
import com.mike.uniadmin.dataModel.users.UserStateEntity

@Database(
    entities =
    [
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
        AttendanceState::class
    ],
    version = 2, exportSchema = false)
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
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "UniAdminDatabase"
                ).addMigrations(MIGRATION_1_2) // Add this line
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}



val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE groups ADD COLUMN new_column TEXT")
    }
}