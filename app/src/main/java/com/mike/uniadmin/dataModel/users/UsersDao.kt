package com.mike.uniadmin.dataModel.users

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mike.uniadmin.dataModel.groupchat.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(users: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>) // New method to insert multiple users

    @Query("DELETE FROM users WHERE id = :userID")
    suspend fun deleteUser(userID: String)

    @Query("SELECT * FROM users WHERE email = :userEmail")
    suspend fun getUserByEmail(userEmail: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userID")
    suspend fun getUserByID(userID: String): UserEntity?

    @Query("SELECT * FROM SignedInUser")
    suspend fun getSignedInUser(): SignedInUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignedInUser(signedInUser: SignedInUser)

    @Query("DELETE FROM signedinuser")
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