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

