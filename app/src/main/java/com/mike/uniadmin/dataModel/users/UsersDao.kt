package com.mike.uniadmin.dataModel.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(users: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("DELETE FROM users WHERE id = :userID")
    suspend fun deleteUser(userID: String)

    @Query("SELECT * FROM users WHERE email = :userEmail")
    suspend fun getUserByEmail(userEmail: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userID")
    suspend fun getUserByID(userID: String): UserEntity?

    @Query("SELECT * FROM SignedInUser")
    suspend fun getSignedInUser(): SignedInUser

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignedInUser(signedInUser: SignedInUser)

    @Query("DELETE FROM SignedInUser")
    suspend fun deleteSignedInUser()
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
interface AccountDeletionDao{
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountDeletion(accountDeletion: AccountDeletionEntity)

    @Query ("SELECT * FROM accountDeletion WHERE id = :userID")
    suspend fun getAccountDeletion(userID: String): AccountDeletionEntity?

}



@Dao
interface UserPreferencesDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(userPreferences: UserPreferencesEntity)

    @Query ("SELECT * FROM userPreferences WHERE id = :userID")
    suspend fun getUserPreferences(userID: String): UserPreferencesEntity?

}

@Dao
interface DatabaseDao {
    @Query("DELETE FROM announcements")
    suspend fun deleteAnnouncements()

    @Query("DELETE FROM notifications")
    suspend fun deleteNotifications()

    @Query("DELETE FROM courseAnnouncements")
    suspend fun deleteCourseAnnouncements()

    @Query("DELETE FROM courseAssignments")
    suspend fun deleteCourseAssignments()

    @Query("DELETE FROM courseDetails")
    suspend fun deleteCourseDetails()

    @Query("DELETE FROM courseTimetable")
    suspend fun deleteCourseTimetables()

    @Query("DELETE FROM attendanceStates")
    suspend fun deleteAttendanceStates()

    @Query("DELETE FROM courses")
    suspend fun deleteCourses()

    @Query("DELETE FROM chats")
    suspend fun deleteChats()

    @Query("DELETE FROM groups")
    suspend fun deleteGroups()

    @Query("DELETE FROM messages")
    suspend fun deleteMessages()

    @Query("DELETE FROM users")
    suspend fun deleteUsers()

    @Query("DELETE FROM accountDeletion")
    suspend fun deleteAccountDeletions()

    @Query("DELETE FROM userPreferences")
    suspend fun deleteUserPreferences()

    @Query("DELETE FROM userState")
    suspend fun deleteUserStates()

    @Query("DELETE FROM SignedInUser")
    suspend fun deleteSignedInUser()

    @Transaction
    suspend fun deleteAllTables() {
        deleteAnnouncements()
        deleteNotifications()
        deleteCourseAnnouncements()
        deleteCourseAssignments()
        deleteCourseDetails()
        deleteCourseTimetables()
        deleteAttendanceStates()
        deleteCourses()
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