package com.mike.uniadmin.backEnd.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mike.uniadmin.backEnd.announcements.AnnouncementEntity
import com.mike.uniadmin.backEnd.announcements.AnnouncementsDao
import com.mike.uniadmin.backEnd.attendance.AttendanceDao
import com.mike.uniadmin.backEnd.attendance.AttendanceEntity
import com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements.ModuleAnnouncement
import com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements.ModuleAnnouncementDao
import com.mike.uniadmin.backEnd.moduleContent.moduleAssignments.ModuleAssignment
import com.mike.uniadmin.backEnd.moduleContent.moduleAssignments.ModuleAssignmentDao
import com.mike.uniadmin.backEnd.moduleContent.moduleDetails.ModuleDetail
import com.mike.uniadmin.backEnd.moduleContent.moduleDetails.ModuleDetailDao
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetable
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetableDao
import com.mike.uniadmin.backEnd.modules.AttendanceState
import com.mike.uniadmin.backEnd.modules.AttendanceStateDao
import com.mike.uniadmin.backEnd.modules.ModuleDao
import com.mike.uniadmin.backEnd.modules.ModuleEntity
import com.mike.uniadmin.backEnd.groupchat.GroupChatDao
import com.mike.uniadmin.backEnd.groupchat.GroupChatEntity
import com.mike.uniadmin.backEnd.groupchat.GroupDao
import com.mike.uniadmin.backEnd.groupchat.GroupEntity
import com.mike.uniadmin.backEnd.notifications.NotificationDao
import com.mike.uniadmin.backEnd.notifications.NotificationEntity
import com.mike.uniadmin.backEnd.courses.CourseDao
import com.mike.uniadmin.backEnd.courses.CourseEntity
import com.mike.uniadmin.backEnd.courses.CourseState
import com.mike.uniadmin.backEnd.courses.CourseStateDao
import com.mike.uniadmin.backEnd.userchat.UserChatDAO
import com.mike.uniadmin.backEnd.userchat.UserChatEntity
import com.mike.uniadmin.backEnd.users.AccountDeletionDao
import com.mike.uniadmin.backEnd.users.AccountDeletionEntity
import com.mike.uniadmin.backEnd.users.UserDao
import com.mike.uniadmin.backEnd.users.UserEntity
import com.mike.uniadmin.backEnd.users.UserPreferencesDao
import com.mike.uniadmin.backEnd.users.UserPreferencesEntity
import com.mike.uniadmin.backEnd.users.UserStateDao
import com.mike.uniadmin.backEnd.users.UserStateEntity

@Database(
    entities = [
        GroupChatEntity::class,
        GroupEntity::class,
        UserChatEntity::class,
        UserEntity::class,
        AccountDeletionEntity::class,
        UserPreferencesEntity::class,
        UserStateEntity::class,
        AnnouncementEntity::class,
        NotificationEntity::class,
        ModuleEntity::class,
        ModuleAnnouncement::class,
        ModuleAssignment::class,
        ModuleDetail::class,
        ModuleTimetable::class,
        AttendanceState::class,
        CourseEntity::class,
        CourseState::class,
        AttendanceEntity::class
               ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CampusConnectDatabase : RoomDatabase() {
    abstract fun groupChatDao(): GroupChatDao
    abstract fun groupDao(): GroupDao
    abstract fun userChatDao(): UserChatDAO
    abstract fun userDao(): UserDao
    abstract fun accountDeletionDao(): AccountDeletionDao
    abstract fun userStateDao(): UserStateDao
    abstract fun announcementsDao(): AnnouncementsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun moduleDao(): ModuleDao
    abstract fun moduleAnnouncementDao(): ModuleAnnouncementDao
    abstract fun moduleAssignmentDao(): ModuleAssignmentDao
    abstract fun moduleDetailsDao(): ModuleDetailDao
    abstract fun moduleTimetableDao(): ModuleTimetableDao
    abstract fun attendanceStateDao(): AttendanceStateDao
    abstract fun databaseDao(): DatabaseDao
    abstract fun courseDao(): CourseDao
    abstract fun courseStateDao(): CourseStateDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: CampusConnectDatabase? = null

        fun getDatabase(context: Context): CampusConnectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, CampusConnectDatabase::class.java, "CampusConnect"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

