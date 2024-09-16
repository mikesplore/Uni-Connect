package com.mike.uniadmin.model.courses

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CourseViewModel(
    private val repository: CourseRepository
) : ViewModel() {

    // LiveData to observe the list of courses
    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses

    // LiveData to observe the list of academic years
    private val _academicYears = MutableLiveData<List<AcademicYear>>()
    val academicYears: LiveData<List<AcademicYear>> = _academicYears

    // LiveData to observe the user's enrolled course
    private val _userEnrolledCourse = MutableLiveData<CourseWithEnrollment?>()
    val userEnrolledCourse: LiveData<CourseWithEnrollment?> = _userEnrolledCourse

    // LiveData for enrollments
    private val _enrollments = MutableLiveData<List<Enrollment>>()
    val enrollments: LiveData<List<Enrollment>> = _enrollments

    // Load all courses
    fun loadCourses() {
        viewModelScope.launch {
            val courseList = repository.getAllCourses()
            _courses.postValue(courseList)
            Log.d("CourseViewModel", "Loaded courses: $courseList")
        }
    }

    // Get user's enrolled course
    fun getUserEnrolledCourse(userId: String) {
        viewModelScope.launch {
            val enrolledCourse = repository.getUserEnrolledCourse(userId)
            _userEnrolledCourse.postValue(enrolledCourse)
        }
    }

    //get All Academic Years
    fun getAllAcademicYears() {
        viewModelScope.launch {
            val academicYears = repository.getAllAcademicYears()
            _academicYears.postValue(academicYears)
        }
    }

    // Enroll a user into a course
    fun enrollUser(enrollment: Enrollment) {
        viewModelScope.launch {
            repository.enrollUser(enrollment)
            getUserEnrolledCourse(enrollment.userId) // Refresh enrollment data after enrolling
        }
    }

    // Restore enrollments from Firebase into Room
    fun restoreEnrollmentsFromFirebase() {
        viewModelScope.launch {
            repository.restoreEnrollmentsFromFirebase()
            loadEnrollments() // Reload enrollments after restoration
        }
    }

    // Load all enrollments
    private fun loadEnrollments() {
        viewModelScope.launch {
            val enrollmentList = repository.getAllEnrollments()
            _enrollments.postValue(enrollmentList)
        }
    }

    //Add Academic Year
    fun addAcademicYear(academicYear: AcademicYear) {
        viewModelScope.launch {
            repository.addAcademicYear(academicYear)
        }
    }
}


class CourseViewModelFactory(private val repository: CourseRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseViewModel::class.java)) {
            return CourseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for the Course")
    }
}