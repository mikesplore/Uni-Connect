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


    // Load all courses
    fun loadCourses() {
        viewModelScope.launch {
            val courseList = repository.getAllCourses()
            _courses.postValue(courseList)
            Log.d("CourseViewModel", "Loaded courses: $courseList")
        }
    }

    //get All Academic Years
    fun getAllAcademicYears() {
        viewModelScope.launch {
            val academicYears = repository.getAllAcademicYears()
            _academicYears.postValue(academicYears)
        }
    }

    //Add Academic Year
    fun addAcademicYear(academicYear: AcademicYear) {
        viewModelScope.launch {
            repository.addAcademicYear(academicYear)
        }
    }

    //Add Course
    fun addCourse(course: Course) {
        viewModelScope.launch {
            repository.insertCourse(course)
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