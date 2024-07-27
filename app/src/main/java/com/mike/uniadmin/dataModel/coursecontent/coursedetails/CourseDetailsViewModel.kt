package com.mike.uniadmin.dataModel.coursecontent.coursedetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CourseDetailViewModel(private val repository: CourseDetailRepository) : ViewModel() {

    private val _details = MutableLiveData<CourseDetail?>()
    val details: MutableLiveData<CourseDetail?> = _details

    // Fetch details for a specific course
    fun getCourseDetails(courseID: String) {
        repository.getCourseDetails(courseID) { details ->
            _details.value = details
        }
    }

    // Save a new detail
    fun saveCourseDetail(courseID: String, detail: CourseDetail) {
        viewModelScope.launch {
            repository.writeCourseDetail(courseID, detail) { success ->
                if (success) {
                    getCourseDetails(courseID) // Refresh the detail list after saving
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }
}

class CourseDetailViewModelFactory(private val repository: CourseDetailRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseDetailViewModel::class.java)) {
            return CourseDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for CourseDetail")
    }
}
