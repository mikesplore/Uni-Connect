package com.mike.uniadmin.dataModel.announcements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AnnouncementViewModel(private val repository: AnnouncementRepository) : ViewModel() {
    private val _announcements = MutableLiveData<List<AnnouncementEntity>>()
    val announcements: LiveData<List<AnnouncementEntity>> = _announcements

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading


    init {
        fetchAnnouncements()
    }

    fun fetchAnnouncements() {
        _isLoading.value = true // Set loading to true before fetching
        repository.fetchAnnouncements { announcements ->
            _announcements.value = announcements
            _isLoading.value = false // Set loading to false after fetching
        }
    }

    fun saveAnnouncement(announcement: AnnouncementEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.saveAnnouncement(announcement) { success ->
                if (success) {
                    onComplete(true)
                    fetchAnnouncements() // Refresh the announcement list after saving
                } else {
                    onComplete(false)
                    // Handle save failure if needed
                }
            }
        }
    }

    fun deleteAnnouncement(announcementId: String, onComplete: (Boolean) -> Unit){
        viewModelScope.launch {
            repository.deleteAnnouncement(announcementId, onSuccess = {
                onComplete(true)
                fetchAnnouncements() // Refresh the announcement list after deleting
            }, onFailure = { exception ->
                onComplete(false)
                // Handle delete failure if needed
            })
        }
    }
}

class AnnouncementViewModelFactory(private val repository: AnnouncementRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnnouncementViewModel::class.java)) {
            return AnnouncementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for announcement")
    }
}