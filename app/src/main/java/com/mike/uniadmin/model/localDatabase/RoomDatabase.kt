package com.mike.uniadmin.model.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mike.uniadmin.model.announcements.AnnouncementEntity
import com.mike.uniadmin.model.announcements.AnnouncementsDao
import com.mike.uniadmin.model.attendance.AttendanceDao
import com.mike.uniadmin.model.attendance.AttendanceEntity
import com.mike.uniadmin.model.moduleContent.moduleAnnouncements.ModuleAnnouncement
import com.mike.uniadmin.model.moduleContent.moduleAnnouncements.ModuleAnnouncementDao
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignment
import com.mike.uniadmin.model.moduleContent.moduleAssignments.ModuleAssignmentDao
import com.mike.uniadmin.model.moduleContent.moduleDetails.ModuleDetail
import com.mike.uniadmin.model.moduleContent.moduleDetails.ModuleDetailDao
import com.mike.uniadmin.model.moduleContent.moduleTimetable.ModuleTimetable
import com.mike.uniadmin.model.moduleContent.moduleTimetable.ModuleTimetableDao
import com.mike.uniadmin.model.modules.AttendanceState
import com.mike.uniadmin.model.modules.AttendanceStateDao
import com.mike.uniadmin.model.modules.ModuleDao
import com.mike.uniadmin.model.modules.ModuleEntity
import com.mike.uniadmin.model.groupchat.GroupChatDao
import com.mike.uniadmin.model.groupchat.GroupChatEntity
import com.mike.uniadmin.model.groupchat.GroupDao
import com.mike.uniadmin.model.groupchat.GroupEntity
import com.mike.uniadmin.model.notifications.NotificationDao
import com.mike.uniadmin.model.notifications.NotificationEntity
import com.mike.uniadmin.model.courses.CourseDao
import com.mike.uniadmin.model.courses.CourseEntity
import com.mike.uniadmin.model.courses.CourseState
import com.mike.uniadmin.model.courses.CourseStateDao
import com.mike.uniadmin.model.userchat.UserChatDAO
import com.mike.uniadmin.model.userchat.UserChatEntity
import com.mike.uniadmin.model.users.AccountDeletionDao
import com.mike.uniadmin.model.users.AccountDeletionEntity
import com.mike.uniadmin.model.users.UserDao
import com.mike.uniadmin.model.users.UserEntity
import com.mike.uniadmin.model.users.UserPreferencesEntity
import com.mike.uniadmin.model.users.UserStateDao
import com.mike.uniadmin.model.users.UserStateEntity

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
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class UniConnectDatabase : RoomDatabase() {
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
        private var INSTANCE: UniConnectDatabase? = null

        fun getDatabase(context: Context): UniConnectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UniConnectDatabase::class.java,
                    "UniConnect"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Admins RENAME TO users")
            }
        }
    }
}

//alter courses table to add department