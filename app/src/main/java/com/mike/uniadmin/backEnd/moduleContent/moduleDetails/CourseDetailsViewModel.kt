package com.mike.uniadmin.backEnd.moduleContent.moduleDetails

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mike.uniadmin.backEnd.modules.ModuleEntity
import kotlinx.coroutines.launch

class ModuleDetailViewModel(private val repository: ModuleDetailRepository) : ViewModel() {

    private val _details = MutableLiveData<ModuleDetail?>()
    val details: MutableLiveData<ModuleDetail?> = _details

    private val _moduleDetails = MutableLiveData<ModuleEntity?>()
    val moduleDetails: MutableLiveData<ModuleEntity?> = _moduleDetails

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading

    // Fetch details for a specific module
    fun getModuleDetails(moduleID: String) {
        _isLoading.value = true
        repository.getModuleDetails(moduleID) { details ->
            _details.value = details
            _isLoading.value = false
        }
    }

    // Save a new detail
    fun saveModuleDetail(moduleID: String, detail: ModuleDetail, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.writeModuleDetail(moduleID, detail) { success ->
                if (success) {
                    onResult(true)
                    getModuleDetails(moduleID) // Refresh the detail list after saving
                } else {
                    onResult(false)
                    // Handle save failure if needed
                }
            }
        }
    }

    fun getModuleDetailsByModuleID(moduleID: String, onResult: (Boolean) -> Unit){
        viewModelScope.launch {
            repository.getModuleDetailsByModuleID(moduleID) { details ->
                if (details != null) {
                    _moduleDetails.value = details
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
        }

    }
}

class ModuleDetailViewModelFactory(private val repository: ModuleDetailRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModuleDetailViewModel::class.java)) {
            return ModuleDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for ModuleDetail")
    }
}
