package com.mike.uniadmin.backEnd.localDatabase

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface DatabaseDao {
    @Query("DELETE FROM announcements")
    suspend fun deleteAnnouncements()

    @Query("DELETE FROM notifications")
    suspend fun deleteNotifications()

    @Query("DELETE FROM moduleAnnouncements")
    suspend fun deleteModuleAnnouncements()

    @Query("DELETE FROM moduleAssignments")
    suspend fun deleteModuleAssignments()

    @Query("DELETE FROM moduleDetails")
    suspend fun deleteModuleDetails()

    @Query("DELETE FROM moduleTimetable")
    suspend fun deleteModuleTimetables()

    @Query("DELETE FROM attendanceStates")
    suspend fun deleteAttendanceStates()

    @Query("DELETE FROM modules")
    suspend fun deleteModules()

    @Query("DELETE FROM groupChats")
    suspend fun deleteChats()

    @Query("DELETE FROM groups")
    suspend fun deleteGroups()

    @Query("DELETE FROM userChats")
    suspend fun deleteMessages()

    @Query("DELETE FROM admins")
    suspend fun deleteUsers()

    @Query("DELETE FROM accountDeletion")
    suspend fun deleteAccountDeletions()

    @Query("DELETE FROM userPreferences")
    suspend fun deleteUserPreferences()

    @Query("DELETE FROM userState")
    suspend fun deleteUserStates()

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("DELETE FROM attendanceStates")
    suspend fun deleteAllAttendanceStates()

    @Query("DELETE FROM announcements")
    suspend fun deleteAllAnnouncements()

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
        deleteAllAnnouncements()
        deleteAllAttendanceStates()
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
}