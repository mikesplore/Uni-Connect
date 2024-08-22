package com.mike.uniadmin.backEnd.modules

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mike.uniadmin.backEnd.programs.CourseRepository
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {
    private val _courses = MutableLiveData<List<CourseEntity>?>()
    private val _fetchedCourse = MutableLiveData<CourseEntity?>()

    val courses: MutableLiveData<List<CourseEntity>?> = _courses
    val fetchedCourse: LiveData<CourseEntity?> = _fetchedCourse

    private val _courseStates = MutableLiveData<Map<String, CourseState>>()
    val courseStates: LiveData<Map<String, CourseState>> = _courseStates

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading
    



    init {
        fetchCourses()
        fetchCourseStates()
    }
    


    private fun fetchCourseStates() {
        repository.fetchCourseStates { fetchedStates ->
            val statesMap = fetchedStates.associateBy { it.courseID }
            _courseStates.value = statesMap

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

    fun saveCourse(course: CourseEntity, onCourseSaved: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.saveCourse(course) { success ->
                onCourseSaved(success)
                if (success) {
                    fetchCourses() // Refresh the course list after saving
                    getCourseDetailsByCourseID(course.courseCode)
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }

    fun saveCourseState(courseState: CourseState) {
        viewModelScope.launch {
            repository.saveCourseState(courseState) { success ->
                if (success) {
                    fetchCourseStates() // Refresh the course list after saving
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