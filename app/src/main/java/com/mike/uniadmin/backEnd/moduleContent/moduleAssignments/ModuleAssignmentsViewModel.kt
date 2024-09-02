package com.mike.uniadmin.backEnd.moduleContent.moduleAssignments


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ModuleAssignmentViewModel(private val repository: ModuleAssignmentRepository) : ViewModel() {

    private val _assignments = MutableLiveData<List<ModuleAssignment>>()
    val assignments: LiveData<List<ModuleAssignment>> = _assignments

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading

    private var moduleAssignmentsLiveData: LiveData<List<ModuleAssignment>>? = null

    private val moduleAssignmentsObserver = Observer<List<ModuleAssignment>> { moduleAssignments ->
        _assignments.value = moduleAssignments
    }

    fun getModuleAssignments(moduleID: String) {
        uniConnectScope.launch(Dispatchers.Main) {
            moduleAssignmentsLiveData = withContext(Dispatchers.IO) {
                repository.getModuleAssignments(moduleID)
            }
            moduleAssignmentsLiveData?.observeForever(moduleAssignmentsObserver)
        }
    }

    override fun onCleared() {
        super.onCleared()
        moduleAssignmentsLiveData?.removeObserver(moduleAssignmentsObserver)
        moduleAssignmentsLiveData = null
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