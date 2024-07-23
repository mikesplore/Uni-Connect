package com.mike.uniadmin.dataModel.courses

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {
    private val _courses = MutableLiveData<List<Course>>()
    private val _fetchedCourse = MutableLiveData<Course?>()
    val courses: LiveData<List<Course>> = _courses
    val fetchedCourse: LiveData<Course?> = _fetchedCourse

    init {
        fetchCourses()
    }

    private fun fetchCourses() {
        repository.fetchCourses { courses ->
            _courses.value = courses
        }
    }

    fun getCourseDetailsByCourseID(courseCode: String) {
        repository.getCourseDetailsByCourseID(courseCode) { course ->
            _fetchedCourse.value = course
        }
    }

    fun saveCourse(course: Course) {
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