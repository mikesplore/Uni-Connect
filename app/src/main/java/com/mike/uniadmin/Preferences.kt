package com.mike.uniadmin

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UniConnectPreferences {
    // Keys for SharedPreferences
    private const val PREF_KEY_USER_EMAIL = "user_email_key"
    private const val PREF_KEY_USER_TYPE = "user_type_key"
    private const val PREF_KEY_USER_ID = "user_id_key"
    private const val PREF_KEY_DARK_MODE = "dark_mode_key"
    private const val PREF_KEY_NOTIFICATIONS_ENABLED = "notifications_enabled_key"
    private const val PREF_KEY_BIOMETRIC_ENABLED = "biometric_enabled_key"
    private const val PREF_KEY_MODULE_NAME = "module_name_key"
    private const val PREF_KEY_MODULE_ID = "module_id_key"
    private const val FONT_STYLE_KEY = "font_style_key"


    private lateinit var preferences: SharedPreferences


    // MutableStates to hold preference values
    val userEmail: MutableState<String> = mutableStateOf("")
    val userType: MutableState<String> = mutableStateOf("")
    val userID: MutableState<String> = mutableStateOf("")
    val moduleName: MutableState<String> = mutableStateOf("")
    val moduleID: MutableState<String> = mutableStateOf("")
    val fontStyle: MutableState<String> = mutableStateOf("System")
    val darkMode: MutableState<Boolean> = mutableStateOf(false)
    val notificationsEnabled: MutableState<Boolean> = mutableStateOf(false)
    val biometricEnabled: MutableState<Boolean> = mutableStateOf(false)

    // Initialize preferences and load stored values
    fun initialize(context: Context) {
        preferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        // Load values from SharedPreferences
        userEmail.value = preferences.getString(PREF_KEY_USER_EMAIL, "") ?: ""
        userType.value = preferences.getString(PREF_KEY_USER_TYPE, "") ?: ""
        userID.value = preferences.getString(PREF_KEY_USER_ID, "") ?: ""
        moduleName.value = preferences.getString(PREF_KEY_MODULE_NAME, "") ?: ""
        moduleID.value = preferences.getString(PREF_KEY_MODULE_ID, "") ?: ""
        fontStyle.value = preferences.getString(FONT_STYLE_KEY, "System") ?: "System"
        darkMode.value = preferences.getBoolean(PREF_KEY_DARK_MODE, true)

        notificationsEnabled.value = preferences.getBoolean(PREF_KEY_NOTIFICATIONS_ENABLED, false)
        biometricEnabled.value = preferences.getBoolean(PREF_KEY_BIOMETRIC_ENABLED, false)
    }

    // Save user ID
    fun saveUserID(newUserID: String) {
        userID.value = newUserID
        preferences.edit().putString(PREF_KEY_USER_ID, newUserID).apply()
        Log.d("UniConnectPreferences", "User ID saved: $newUserID")
    }

    // Save font style
    fun saveFontStyle(newFontStyle: String) {
        fontStyle.value = newFontStyle
        preferences.edit().putString(FONT_STYLE_KEY, newFontStyle).apply()
        Log.d("UniConnectPreferences", "Font style saved: $newFontStyle")
    }

    // Save module name
    fun saveModuleName(newModuleName: String) {
        moduleName.value = newModuleName
        preferences.edit().putString(PREF_KEY_MODULE_NAME, newModuleName).apply()
        Log.d("UniConnectPreferences", "Module name saved: $newModuleName")
    }

    // Save module
    fun saveModuleID(newModuleID: String) {
        moduleID.value = newModuleID
        preferences.edit().putString(PREF_KEY_MODULE_ID, newModuleID).apply()
        Log.d("UniConnectPreferences", "Module ID saved: $newModuleID")
    }

    // Save user email
    fun saveUserEmail(newEmail: String) {
        userEmail.value = newEmail
        preferences.edit().putString(PREF_KEY_USER_EMAIL, newEmail).apply()
        Log.d("UniConnectPreferences", "User email saved: $newEmail")
    }

    // Save user type
    fun saveUserType(newType: String) {
        userType.value = newType
        preferences.edit().putString(PREF_KEY_USER_TYPE, newType).apply()
        Log.d("UniConnectPreferences", "User type saved: $newType")
    }

    // Save dark mode preference
    fun saveDarkModePreference(isDarkMode: Boolean) {
        darkMode.value = isDarkMode
        preferences.edit().putBoolean(PREF_KEY_DARK_MODE, isDarkMode).apply()
        Log.d("UniConnectPreferences", "Dark mode saved: $isDarkMode")
    }

    //save notifications preference
    fun saveNotificationsPreference(isEnabled: Boolean) {
        notificationsEnabled.value = isEnabled
        preferences.edit().putBoolean(PREF_KEY_NOTIFICATIONS_ENABLED, isEnabled).apply()
        Log.d("UniConnectPreferences", "Notifications enabled saved: $isEnabled")
    }

    //save biometric preference
    fun saveBiometricPreference(isEnabled: Boolean) {
        biometricEnabled.value = isEnabled
        preferences.edit().putBoolean(PREF_KEY_BIOMETRIC_ENABLED, isEnabled).apply()
        Log.d("UniConnectPreferences", "Biometric enabled saved: $isEnabled")
    }

    fun clearAllData() {

        userEmail.value = ""
        userType.value = ""
        userID.value = ""
        moduleName.value = ""
        moduleID.value = ""
        fontStyle.value = "System"
        darkMode.value = true // Or set to your default value
        notificationsEnabled.value = false // Or set to your default value
        biometricEnabled.value = false // Or set to your default value

        preferences.edit().clear().apply()
        Log.d("UniConnectPreferences", "All preferences cleared")
    }
}


object CourseManager {
    private const val PREFS_NAME = "course_prefs"
    private const val KEY_COURSE_CODE = "course_code"
    private const val KEY_COURSE_NAME = "course_name"
    private const val KEY_ACADEMIC_YEAR = "academic_year"
    private const val KEY_SEMESTER = "semester"

    private lateinit var sharedPreferences: SharedPreferences

    private val _courseCode = MutableStateFlow("")
    private val _courseName = MutableStateFlow("")
    private val _academicYear = MutableStateFlow("")
    private val _semester = MutableStateFlow("")

    val courseCode: StateFlow<String> = _courseCode.asStateFlow()
    val courseName: StateFlow<String> = _courseName.asStateFlow()
    val academicYear: StateFlow<String> = _academicYear.asStateFlow()
    val semester: StateFlow<String> = _semester.asStateFlow()

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _courseCode.value = sharedPreferences.getString(KEY_COURSE_CODE, "") ?: ""
        _courseName.value = sharedPreferences.getString(KEY_COURSE_NAME, "") ?: ""
        _academicYear.value = sharedPreferences.getString(KEY_ACADEMIC_YEAR, "") ?: ""
        _semester.value = sharedPreferences.getString(KEY_SEMESTER, "") ?: ""
    }

    fun updateCourseCode(newCode: String) {
        _courseCode.value = newCode
        sharedPreferences.edit().putString(KEY_COURSE_CODE, newCode).apply()
    }

    fun updateCourseName(newName: String) {
        _courseName.value = newName
        sharedPreferences.edit().putString(KEY_COURSE_NAME, newName).apply()
    }

    fun updateAcademicYear(newYear: String) {
        _academicYear.value = newYear
        sharedPreferences.edit().putString(KEY_ACADEMIC_YEAR, newYear).apply()
    }

    fun updateSemester(newSemester: String) {
        _semester.value = newSemester
        sharedPreferences.edit().putString(KEY_SEMESTER, newSemester).apply()
    }

    //function for clearing the data
    fun clearCourseManagerData() {
        _courseCode.value = ""
        _courseName.value = ""
        _academicYear.value = ""
        _semester.value = ""
        sharedPreferences.edit().clear().apply()
        Log.d("UniConnectPreferences", "All preferences in CourseManager cleared")

    }


}