package com.mike.uniadmin.helperFunctions

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

object Global {
    val showAlert: MutableState<Boolean> = mutableStateOf(false)
}

object Details {
    var email: MutableState<String> = mutableStateOf("")
    var firstName: MutableState<String> = mutableStateOf("🔃")
    var lastName: MutableState<String> = mutableStateOf("")

}

