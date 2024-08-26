package com.mike.uniadmin

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

object UniAdminPreferences {
    // Keys for SharedPreferences
    private const val PREF_KEY_PROGRAM_CODE = "course_code_key"
    private const val PREF_KEY_USER_EMAIL = "user_email_key"
    private const val PREF_KEY_USER_TYPE = "user_id_key"
    private const val PREF_KEY_DARK_MODE = "dark_mode_key"
    private const val PREF_KEY_NOTIFICATIONS_ENABLED = "notifications_enabled_key"
    private const val PREF_KEY_BIOMETRIC_ENABLED = "biometric_enabled_key"
    private const val PREF_KEY_MODULE_NAME = "module_name_key"
    private const val PREF_KEY_MODULE_ID = "module_id_key"

    private lateinit var preferences: SharedPreferences

    // MutableStates to hold preference values
    val courseCode: MutableState<String> = mutableStateOf("")
    val userEmail: MutableState<String> = mutableStateOf("")
    val userType: MutableState<String> = mutableStateOf("")
    val moduleName: MutableState<String> = mutableStateOf("")
    val moduleID: MutableState<String> = mutableStateOf("")
    val darkMode: MutableState<Boolean> = mutableStateOf(false)
    val notificationsEnabled: MutableState<Boolean> = mutableStateOf(false)
    val biometricEnabled: MutableState<Boolean> = mutableStateOf(false)

    // Initialize preferences and load stored values
    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // Load values from SharedPreferences
        courseCode.value = preferences.getString(PREF_KEY_PROGRAM_CODE, "") ?: ""
        userEmail.value = preferences.getString(PREF_KEY_USER_EMAIL, "") ?: ""
        userType.value = preferences.getString(PREF_KEY_USER_TYPE, "") ?: ""
        moduleName.value = preferences.getString(PREF_KEY_MODULE_NAME, "") ?: ""
        moduleID.value = preferences.getString(PREF_KEY_MODULE_ID, "") ?: ""
        darkMode.value = preferences.getBoolean(PREF_KEY_DARK_MODE, true)
        notificationsEnabled.value = preferences.getBoolean(PREF_KEY_NOTIFICATIONS_ENABLED, true)
        biometricEnabled.value = preferences.getBoolean(PREF_KEY_BIOMETRIC_ENABLED, true)
    }

    // Save module name
    fun saveModuleName(newModuleName: String) {
        moduleName.value = newModuleName
        preferences.edit().putString(PREF_KEY_MODULE_NAME, newModuleName).apply()
        Log.d("UniAdminPreferences", "Module name saved: $newModuleName")
    }

    // Save module
    fun saveModuleID(newModuleID: String) {
        moduleID.value = newModuleID
        preferences.edit().putString(PREF_KEY_MODULE_ID, newModuleID).apply()
        Log.d("UniAdminPreferences", "Module ID saved: $newModuleID")
    }

    // Save course code
    fun saveCourseCode(newCourseCode: String) {
        courseCode.value = newCourseCode
        preferences.edit().putString(PREF_KEY_PROGRAM_CODE, newCourseCode).apply()
        Log.d("UniAdminPreferences", "Course code saved: $newCourseCode")
    }

    // Save user email
    fun saveUserEmail(newEmail: String) {
        userEmail.value = newEmail
        preferences.edit().putString(PREF_KEY_USER_EMAIL, newEmail).apply()
        Log.d("UniAdminPreferences", "User email saved: $newEmail")
    }

    // Save user type
    fun saveUserType(newType: String) {
        userType.value = newType
        preferences.edit().putString(PREF_KEY_USER_TYPE, newType).apply()
        Log.d("UniAdminPreferences", "User type saved: $newType")
    }

    // Save dark mode preference
    fun saveDarkModePreference(isDarkMode: Boolean) {
        darkMode.value = isDarkMode
        preferences.edit().putBoolean(PREF_KEY_DARK_MODE, isDarkMode).apply()
        Log.d("UniAdminPreferences", "Dark mode saved: $isDarkMode")
    }
    //save notifications preference
    fun saveNotificationsPreference(isEnabled: Boolean) {
        notificationsEnabled.value = isEnabled
        preferences.edit().putBoolean(PREF_KEY_NOTIFICATIONS_ENABLED, isEnabled).apply()
        Log.d("UniAdminPreferences", "Notifications enabled saved: $isEnabled")
    }
    //save biometric preference
    fun saveBiometricPreference(isEnabled: Boolean) {
        biometricEnabled.value = isEnabled
        preferences.edit().putBoolean(PREF_KEY_BIOMETRIC_ENABLED, isEnabled).apply()
        Log.d("UniAdminPreferences", "Biometric enabled saved: $isEnabled")
    }

    fun clearAllData() {
        courseCode.value = ""
        userEmail.value = ""
        userType.value = ""
        darkMode.value = true // Or set to your default value
        notificationsEnabled.value = false // Or set to your default value
        biometricEnabled.value = false // Or set to your default value

        preferences.edit().clear().apply()
        Log.d("UniAdminPreferences", "All preferences cleared")
    }
}
