package com.mike.uniadmin.dataModel.users

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "SignedInUser")
data class SignedInUser(
   @PrimaryKey val id: String,
    val email: String = ""
){
    constructor(): this("", "")
}


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageLink: String = ""
){
    constructor(): this("", "", "", "", "", "")
}


@Entity(tableName = "accountDeletion")
data class AccountDeletionEntity(
    @PrimaryKey val id: String,
    val admissionNumber: String ="",
    val email: String = "",
    val date: String = "",
    val status: String = ""
){
    constructor(): this("", "", "", "", "")
}


@Entity(tableName = "userPreferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: String,
    val studentID: String = "",
    val profileImageLink: String = "",
    val biometrics: String = "",
    val darkMode: String = "",
    val notifications: String = ""

){
    constructor(): this("", "", "", "", "", "")
}


@Entity(tableName = "userState")
data class UserStateEntity(
    @PrimaryKey val id: String,
    val userID: String = "",
    val online: String = "",
    val lastTime: String = "",
){
    constructor(): this("", "", "", "")

}