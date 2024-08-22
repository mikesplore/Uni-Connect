package com.mike.uniadmin.backEnd.moduleContent.moduleAssignments


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class ModuleAssignmentViewModel(private val repository: ModuleAssignmentRepository) : ViewModel() {

    private val _assignments = MutableLiveData<List<ModuleAssignment>>()
    val assignments: LiveData<List<ModuleAssignment>> = _assignments

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading

    // Fetch assignments for a specific module
    fun getModuleAssignments(moduleID: String) {
        _isLoading.value = true // Set loading to true before fetching
        repository.getModuleAssignments(moduleID) { assignments ->
            _assignments.value = assignments
            _isLoading.value = false // Set loading to false after fetching
        }
    }

    // Save a new assignment
    fun saveModuleAssignment(moduleID: String, assignment: ModuleAssignment, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.writeModuleAssignment(moduleID, assignment) { success ->
                if (success) {
                    onComplete(true)
                    getModuleAssignments(moduleID) // Refresh the assignment list after saving
                } else {
                    onComplete(false)
                    // Handle save failure if needed
                }
            }
        }
    }
}

class ModuleAssignmentViewModelFactory(private val repository: ModuleAssignmentRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModuleAssignmentViewModel::class.java)) {
            return ModuleAssignmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for ModuleAssignment")
    }
}