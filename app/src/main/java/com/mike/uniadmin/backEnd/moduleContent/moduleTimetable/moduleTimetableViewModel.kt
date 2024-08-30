package com.mike.uniadmin.backEnd.moduleContent.moduleTimetable

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class ModuleTimetableViewModel(private val repository: ModuleTimetableRepository) : ViewModel() {

    private val _timetables = MutableLiveData<List<ModuleTimetable>>()
    val timetables: LiveData<List<ModuleTimetable>> = _timetables

    private val _moduleTimetables = MutableLiveData<List<ModuleTimetable>>()
    val moduleTimetables: LiveData<List<ModuleTimetable>> = _moduleTimetables

    private val _timetablesToday = MutableLiveData<ModuleTimetable?>()
    val timetablesToday: MutableLiveData<ModuleTimetable?> = _timetablesToday

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


    fun listenForFirebaseUpdates(moduleID: String) {
        repository.listenForFirebaseUpdates(moduleID)
    }

    // Fetch timetables for a specific module
    fun getModuleTimetables(moduleID: String) {
        repository.listenForFirebaseUpdates(moduleID)
        _isLoading.postValue(true)
        repository.getModuleTimetables(moduleID) { timetables ->
            _timetables.postValue(timetables) // Use postValue for background updates
            _isLoading.postValue(false)
        }
    }

    //Fetch timetables for all modules
    fun getAllModuleTimetables() {
        _isLoading.postValue(true)
        repository.getAllModuleTimetables { timetables ->
            _moduleTimetables.postValue(timetables) // Use postValue for background updates
            _isLoading.postValue(false)
        }
    }

    // Save a new timetable
    fun saveModuleTimetable(moduleID: String, timetable: ModuleTimetable, onCompletion: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.writeModuleTimetable(moduleID, timetable) { success ->
                if (success) {
                    onCompletion(true)
                    getModuleTimetables(moduleID) // Refresh the timetable list after saving
                } else {
                    onCompletion(false)
                    // Handle save failure if needed
                    Log.e("ModuleTimetableViewModel", "Failed to save timetable")
                }
            }
        }
    }

    fun findUpcomingClass() {
        _isLoading.postValue(true)
        repository.getUpcomingClass { timetable ->
            _timetablesToday.postValue(timetable)
            _isLoading.postValue(false)
        }
    }




}


class ModuleTimetableViewModelFactory(private val repository: ModuleTimetableRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModuleTimetableViewModel::class.java)) {
            return ModuleTimetableViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for ModuleTimetable")
    }
}