package com.mike.uniadmin.dataModel.users

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "SignedInUser")
data class SignedInUser(
   @PrimaryKey val id: String,
    val email: String? = null
){
    constructor(): this(
        "",
        null
    )
}


@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val profileImageLink: String? = null
)
{
    constructor() : this(
        "",
        null,
        null,
        null,
        null,
        ""
    )
}

@Entity(tableName = "accountDeletion")
data class AccountDeletionEntity(
    @PrimaryKey val id: String,
    val admissionNumber: String? = null,
    val email: String? = null,
    val date: String? = null,
    val status: String? = null
)
@Entity(tableName = "userPreferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: String,
    val studentID: String? = null,
    val profileImageLink: String? = null,
    val biometrics: String? = null,
    val darkMode: String? = null,
    val notifications: String? = null

){
    constructor() : this(
        "",
        null,
        null,
        null,
        null,
        null,

    )
}


@Entity(tableName = "userState")
data class UserStateEntity(
    @PrimaryKey val id: String,
    val userID: String? = null,
    val online: String? = null,
    val lastTime: String? = null,
){
    constructor() : this(
        "",
        null,
        null,
        null,

    )
}