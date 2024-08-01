package com.mike.uniadmin.dataModel.coursecontent.courseassignments

import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignment
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignmentRepository



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class CourseAssignmentViewModel(private val repository: CourseAssignmentRepository) : ViewModel() {

    private val _assignments = MutableLiveData<List<CourseAssignment>>()
    val assignments: LiveData<List<CourseAssignment>> = _assignments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Fetch assignments for a specific course
    fun getCourseAssignments(courseID: String) {
        _isLoading.value = true
        repository.getCourseAssignments(courseID) { assignments ->
            _assignments.value = assignments
            _isLoading.value = false
        }
    }

    // Save a new assignment
    fun saveCourseAssignment(courseID: String, assignment: CourseAssignment, onComplete:(Boolean) -> Unit) {
        viewModelScope.launch {
            repository.writeCourseAssignment(courseID, assignment) { success ->
                if (success) {
                    onComplete(true)
                    getCourseAssignments(courseID) // Refresh the assignment list after saving
                } else {
                    onComplete(false)
                    // Handle save failure if needed
                }
            }
        }
    }
}

class CourseAssignmentViewModelFactory(private val repository: CourseAssignmentRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseAssignmentViewModel::class.java)) {
            return CourseAssignmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for CourseAssignment")
    }
}