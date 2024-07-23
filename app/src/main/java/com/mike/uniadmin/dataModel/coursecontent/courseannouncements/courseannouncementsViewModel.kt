package com.mike.uniadmin.dataModel.coursecontent.courseannouncements


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class CourseAnnouncementViewModel(private val repository: CourseAnnouncementRepository) : ViewModel() {

    private val _announcements = MutableLiveData<List<CourseAnnouncement>>()
    val announcements: LiveData<List<CourseAnnouncement>> = _announcements

    // Fetch announcements for a specific course
    fun getCourseAnnouncements(courseID: String) {
        repository.getCourseAnnouncements(courseID) { announcements ->
            _announcements.value = announcements
        }
    }

    // Save a new announcement
    fun saveCourseAnnouncement(courseID: String, announcement: CourseAnnouncement) {
        viewModelScope.launch {
            repository.writeCourseAnnouncement(courseID, announcement) { success ->
                if (success) {
                    getCourseAnnouncements(courseID) // Refresh the announcement list after saving
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }
}

class CourseAnnouncementViewModelFactory(private val repository: CourseAnnouncementRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseAnnouncementViewModel::class.java)) {
            return CourseAnnouncementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for CourseAnnouncement")
    }
}