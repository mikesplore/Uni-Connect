package com.mike.uniadmin.backEnd.courses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {
    private val _courses = MutableLiveData<List<CourseEntity>>()
    private val _fetchedCourse = MutableLiveData<CourseEntity?>()
    val courses: LiveData<List<CourseEntity>> = _courses
    val fetchedCourse: LiveData<CourseEntity?> = _fetchedCourse

    private val _attendanceStates = MutableLiveData<Map<String, AttendanceState>>()
    val attendanceStates: LiveData<Map<String, AttendanceState>> = _attendanceStates

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading


    init {
        fetchCourses()
        fetchAttendanceStates()
    }

    fun fetchAttendanceStates() {
        repository.fetchAttendanceStates { fetchedStates ->
            val statesMap = fetchedStates.associateBy { it.courseID }
            _attendanceStates.value = statesMap

        }
    }


    fun fetchCourses() {
        _isLoading.value = true // Set loading to true before fetching
        repository.fetchCourses { courses ->
            _courses.value = courses
            _isLoading.value = false // Set loading to false after fetching
        }
    }

    fun getCourseDetailsByCourseID(courseCode: String) {
        repository.getCourseDetailsByCourseID(courseCode) { course ->
            _fetchedCourse.value = course
        }
    }

    fun saveCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.saveCourse(course) { success ->
                if (success) {
                    fetchCourses() // Refresh the course list after saving
                    getCourseDetailsByCourseID(course.courseCode)
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }

    fun saveAttendanceState(attendanceState: AttendanceState) {
        viewModelScope.launch {
            repository.saveAttendanceState(attendanceState) { success ->
                if (success) {
                    fetchAttendanceStates() // Refresh the course list after saving
                } else {
                    // Handle save failure if needed
                }
            }
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