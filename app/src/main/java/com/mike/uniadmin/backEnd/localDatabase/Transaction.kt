package com.mike.uniadmin.backEnd.localDatabase

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.mike.uniadmin.backEnd.announcements.AnnouncementEntity
import com.mike.uniadmin.backEnd.groupchat.GroupChatEntity
import com.mike.uniadmin.backEnd.groupchat.GroupEntity
import com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements.ModuleAnnouncement
import com.mike.uniadmin.backEnd.moduleContent.moduleAssignments.ModuleAssignment
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetable
import com.mike.uniadmin.backEnd.moduleContent.moduleTimetable.ModuleTimetableRepository
import com.mike.uniadmin.backEnd.modules.ModuleEntity
import com.mike.uniadmin.backEnd.notifications.NotificationEntity
import com.mike.uniadmin.backEnd.userchat.UserChatEntity
import com.mike.uniadmin.backEnd.users.UserEntity


@Dao
interface DatabaseDao {
    @Query("SELECT * FROM announcements")
    suspend fun getAnnouncements(): List<AnnouncementEntity>

    @Query("DELETE FROM announcements")
    suspend fun deleteAnnouncements()

    @Query("SELECT * FROM notifications")
    suspend fun getNotifications(): List<NotificationEntity>

    @Query("DELETE FROM notifications")
    suspend fun deleteNotifications()


    @Query("DELETE FROM moduleAnnouncements")
    suspend fun deleteModuleAnnouncements()

    @Query("SELECT * FROM moduleAssignments")
    suspend fun getModuleAssignments(): List<ModuleAssignment>

    @Query("DELETE FROM moduleAssignments")
    suspend fun deleteModuleAssignments()


    @Query("DELETE FROM moduleDetails")
    suspend fun deleteModuleDetails()

    @Query("SELECT * FROM moduleTimetable")
    suspend fun getModuleTimetables(): List<ModuleTimetable>

    @Query("DELETE FROM moduleTimetable")
    suspend fun deleteModuleTimetables()

    @Query("DELETE FROM attendanceStates")
    suspend fun deleteAttendanceStates()

    @Query("DELETE FROM modules")
    suspend fun deleteModules()

    @Query("SELECT * FROM groupChats")
    suspend fun getChats(): List<GroupChatEntity>

    @Query("DELETE FROM groupChats")
    suspend fun deleteChats()

    @Query("SELECT * FROM groups")
    suspend fun getGroups(): List<GroupEntity>

    @Query("DELETE FROM groups")
    suspend fun deleteGroups()

    @Query("SELECT * FROM userChats")
    suspend fun getMessages(): List<UserChatEntity>

    @Query("DELETE FROM userChats")
    suspend fun deleteMessages()

    @Query("SELECT * FROM admins")
    suspend fun getUsers(): List<UserEntity>

    @Query("DELETE FROM admins")
    suspend fun deleteUsers()

    @Query("DELETE FROM accountDeletion")
    suspend fun deleteAccountDeletions()

    @Query("DELETE FROM userPreferences")
    suspend fun deleteUserPreferences()

    @Query("DELETE FROM userState")
    suspend fun deleteUserStates()

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotifications(): List<NotificationEntity>

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("DELETE FROM moduleAnnouncements")
    suspend fun deleteAllModuleAnnouncements()

    @Query("DELETE FROM moduleAssignments")
    suspend fun deleteAllModuleAssignments()

    @Query("DELETE FROM moduleDetails")
    suspend fun deleteAllModuleDetails()

    @Query("DELETE FROM moduleTimetable")
    suspend fun deleteAllModuleTimetables()

    @Query("DELETE FROM modules")
    suspend fun deleteAllModules()

    @Query("SELECT * FROM modules")
    suspend fun getAllModules(): List<ModuleEntity>

    @Query("DELETE FROM groupChats")
    suspend fun deleteAllChats()

    @Query("DELETE FROM groups")
    suspend fun deleteAllGroups()

    @Query("DELETE FROM userChats")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM admins")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM accountDeletion")
    suspend fun deleteAllAccountDeletions()

    @Query("DELETE FROM userPreferences")
    suspend fun deleteAllUserPreferences()

    @Query("DELETE FROM userState")
    suspend fun deleteAllUserStates()

    @Query("DELETE FROM attendance")
    suspend fun deleteAllAttendance()


    @Transaction
    suspend fun deleteAllTables() {
        deleteAllAttendance()
        deleteAllUserStates()
        deleteAllUserPreferences()
        deleteAllAccountDeletions()
        deleteAllUsers()
        deleteAllMessages()
        deleteAllGroups()
        deleteAllChats()
        deleteAllModules()
        deleteAllModuleTimetables()
        deleteAllModuleDetails()
        deleteAllModuleAssignments()
        deleteAllModuleAnnouncements()
        deleteAllNotifications()
        deleteAnnouncements()
        deleteNotifications()
        deleteModuleAnnouncements()
        deleteModuleAssignments()
        deleteModuleDetails()
        deleteModuleTimetables()
        deleteAttendanceStates()
        deleteModules()
        deleteChats()
        deleteGroups()
        deleteMessages()
        deleteUsers()
        deleteAccountDeletions()
        deleteUserPreferences()
        deleteUserStates()
    }

    suspend fun loadCrucialData(){
        getUsers()
        getMessages()
        getChats()
        getGroups()
        getAllModules()
        getAllNotifications()
        getModuleTimetables()
        getNotifications()
        getAnnouncements()
        getModuleAssignments()
    }
}

