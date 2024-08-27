package com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class ModuleAnnouncementViewModel(private val repository: ModuleAnnouncementRepository) : ViewModel() {

    private val _announcements = MutableLiveData<List<ModuleAnnouncement>>()
    val announcements: LiveData<List<ModuleAnnouncement>> = _announcements

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading

    // Fetch announcements for a specific module
    fun getModuleAnnouncements(moduleID: String) {
        _isLoading.postValue(true) // Set loading to true before fetching
        repository.getModuleAnnouncements(moduleID) { announcements ->
            _announcements.postValue(announcements)
            _isLoading.postValue(false) // Set loading to false after fetching
        }
    }

    // Save a new announcement
    fun saveModuleAnnouncement(moduleID: String, announcement: ModuleAnnouncement) {
        viewModelScope.launch {
            repository.writeModuleAnnouncement(moduleID, announcement) { success ->
                if (success) {
                    getModuleAnnouncements(moduleID) // Refresh the announcement list after saving
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }
}

class ModuleAnnouncementViewModelFactory(private val repository: ModuleAnnouncementRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModuleAnnouncementViewModel::class.java)) {
            return ModuleAnnouncementViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for ModuleAnnouncement")
    }
}