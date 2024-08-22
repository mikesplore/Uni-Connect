package com.mike.uniadmin.backEnd.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface UserDao {
    @Query("SELECT * FROM admins")
    suspend fun getUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(users: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM admins WHERE id = :userID")
    suspend fun deleteUser(userID: String)

    @Query("SELECT * FROM admins WHERE email = :userEmail")
    suspend fun getUserByEmail(userEmail: String): UserEntity?

    @Query("SELECT * FROM admins WHERE id = :userID")
    suspend fun getUserByID(userID: String): UserEntity?

    @Query("SELECT * FROM SignedInUser")
    suspend fun getSignedInUser(): SignedInUser

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignedInUser(signedInUser: SignedInUser)

    @Query("DELETE FROM SignedInUser")
    suspend fun deleteSignedInUser()

    @Query("SELECT id FROM admins")
    suspend fun getAllUserIds(): List<String>

}


@Dao
interface UserStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserState(userState: UserStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStates(userStates: List<UserStateEntity>) // New method to insert multiple user states

    @Query("SELECT * FROM userState WHERE :userID")
    suspend fun getUserState(userID: String): UserStateEntity?

    @Query("SELECT * FROM userState")
    suspend fun getAllUserStates(): List<UserStateEntity>
}


@Dao
interface AccountDeletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountDeletion(accountDeletion: AccountDeletionEntity)

    @Query("SELECT * FROM accountDeletion WHERE id = :userID")
    suspend fun getAccountDeletion(userID: String): AccountDeletionEntity?

}


@Dao
interface UserPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(userPreferences: UserPreferencesEntity)

    @Query("SELECT * FROM userPreferences WHERE id = :userID")
    suspend fun getUserPreferences(userID: String): UserPreferencesEntity?

}

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

    @Query("DELETE FROM SignedInUser")
    suspend fun deleteSignedInUser()

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Transaction
    suspend fun deleteAllTables() {
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
        deleteSignedInUser()
    }
}