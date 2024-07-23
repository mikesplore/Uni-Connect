package com.mike.uniadmin.dataModel.announcements

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AnnouncementViewModel(private val repository: AnnouncementRepository) : ViewModel() {
    private val _announcements = MutableLiveData<List<Announcement>>()
    val announcements: LiveData<List<Announcement>> = _announcements

    init {
        fetchAnnouncements()
    }

    private fun fetchAnnouncements() {
        repository.fetchAnnouncements { announcements ->
            _announcements.value = announcements
        }
    }

    fun saveAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.saveAnnouncement(announcement) { success ->
                if (success) {
                    fetchAnnouncements() // Refresh the announcement list after saving
                } else {
                    // Handle save failure if needed
                }
            }
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