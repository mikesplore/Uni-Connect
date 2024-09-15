package com.mike.uniadmin.model.announcements

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AnnouncementViewModel(private val repository: AnnouncementRepository) : ViewModel() {
    private val _announcements = MutableLiveData<List<AnnouncementsWithAuthor>>()
    val announcements: LiveData<List<AnnouncementsWithAuthor>> = _announcements

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading


    init {
        fetchAnnouncements()
        startAnnouncementsListener()

    }

    fun startAnnouncementsListener() {
        repository.startAnnouncementsListener()
    }

    fun fetchAnnouncements() {
        repository.fetchAnnouncements { announcements ->
            _announcements.postValue(announcements)
        }
    }

    fun saveAnnouncement(announcement: AnnouncementEntity, onComplete: (Boolean) -> Unit) {
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

    fun deleteAnnouncement(announcementId: String, onComplete: (Boolean) -> Unit){
            repository.deleteAnnouncement(announcementId, onSuccess = {
                onComplete(true)
                fetchAnnouncements() // Refresh the announcement list after deleting
            }, onFailure = { exception ->
                Log.e("AnnouncementViewModel", "Error deleting announcement", exception)
                onComplete(false)
                // Handle delete failure if needed
            })

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