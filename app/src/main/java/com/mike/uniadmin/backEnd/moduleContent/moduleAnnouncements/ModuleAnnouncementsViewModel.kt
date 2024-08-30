package com.mike.uniadmin.backEnd.moduleContent.moduleAnnouncements


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import com.mike.uniadmin.backEnd.userchat.UserChatsWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ModuleAnnouncementViewModel(private val repository: ModuleAnnouncementRepository) : ViewModel() {

    private val _announcements = MutableLiveData<List<ModuleAnnouncementsWithAuthor>>()
    val announcements: LiveData<List<ModuleAnnouncementsWithAuthor>> = _announcements

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading

    private var moduleAnnouncementsLiveData: LiveData<List<ModuleAnnouncementsWithAuthor>>? = null

    private val moduleAnnouncementsObserver = Observer<List<ModuleAnnouncementsWithAuthor>> { moduleAnnouncements ->
        _announcements.value = moduleAnnouncements
    }

    fun getModuleAnnouncements(moduleID: String) {
        uniConnectScope.launch(Dispatchers.Main) {
            moduleAnnouncementsLiveData = withContext(Dispatchers.IO) {
                repository.getModuleAnnouncements(moduleID)
            }
            moduleAnnouncementsLiveData?.observeForever(moduleAnnouncementsObserver)
        }
    }

    override fun onCleared() {
        super.onCleared()
        moduleAnnouncementsLiveData?.removeObserver(moduleAnnouncementsObserver)
        moduleAnnouncementsLiveData = null
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