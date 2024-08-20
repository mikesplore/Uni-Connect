package com.mike.uniadmin.backEnd.coursecontent.coursedetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mike.uniadmin.backEnd.courses.CourseEntity
import kotlinx.coroutines.launch

class CourseDetailViewModel(private val repository: CourseDetailRepository) : ViewModel() {

    private val _details = MutableLiveData<CourseDetail?>()
    val details: MutableLiveData<CourseDetail?> = _details

    private val _courseDetails = MutableLiveData<CourseEntity?>()
    val courseDetails: MutableLiveData<CourseEntity?> = _courseDetails

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading

    // Fetch details for a specific course
    fun getCourseDetails(courseID: String) {
        _isLoading.value = true
        repository.getCourseDetails(courseID) { details ->
            _details.value = details
            _isLoading.value = false
        }
    }

    // Save a new detail
    fun saveCourseDetail(courseID: String, detail: CourseDetail, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.writeCourseDetail(courseID, detail) { success ->
                if (success) {
                    onResult(true)
                    getCourseDetails(courseID) // Refresh the detail list after saving
                } else {
                    onResult(false)
                    // Handle save failure if needed
                }
            }
        }
    }

    fun getCourseDetailsByCourseID(courseID: String, onResult: (Boolean) -> Unit){
        viewModelScope.launch {
            repository.getCourseDetailsByCourseID(courseID) { details ->
                if (details != null) {
                    _courseDetails.value = details
                    onResult(true)
                } else {
                    onResult(false)
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
