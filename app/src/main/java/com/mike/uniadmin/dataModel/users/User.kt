package com.mike.uniadmin.dataModel.users

data class User(
    var id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val gender: String = "",
    val profileImageLink: String = ""
)

data class AccountDeletion(
    val id: String = "",
    val admissionNumber: String = "",
    val email: String = ""
)

data class UserPreferences(
    val studentID: String = "",
    val id: String = "",
    val profileImageLink: String = "",
    val biometrics: String = "disabled",
    val darkMode: String = "disabled",
    val notifications: String = "disabled"

)

data class UserState(
    val userID: String = "",
    val id: String = "",
    val online: String = "",
    val lastTime: String = "",
)