package com.mike.uniadmin.backEnd.modules

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ModuleViewModel(private val repository: ModuleRepository) : ViewModel() {
    private val _modules = MutableLiveData<List<ModuleEntity>>()
    private val _fetchedModule = MutableLiveData<ModuleEntity?>()
    val modules: LiveData<List<ModuleEntity>> = _modules
    val fetchedModule: LiveData<ModuleEntity?> = _fetchedModule

    private val _attendanceStates = MutableLiveData<Map<String, AttendanceState>>()
    val attendanceStates: LiveData<Map<String, AttendanceState>> = _attendanceStates

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading


    init {
        fetchModules()
        fetchAttendanceStates()
    }

    fun fetchAttendanceStates() {
        repository.fetchAttendanceStates { fetchedStates ->
            val statesMap = fetchedStates.associateBy { it.moduleID }
            _attendanceStates.value = statesMap

        }
    }


    fun fetchModules() {
        _isLoading.value = true // Set loading to true before fetching
        repository.fetchModules { modules ->
            _modules.value = modules
            _isLoading.value = false // Set loading to false after fetching
        }
    }

    fun getModuleDetailsByModuleID(moduleCode: String) {
        repository.getModuleDetailsByModuleID(moduleCode) { module ->
            _fetchedModule.value = module
        }
    }

    fun saveModule(module: ModuleEntity) {
        viewModelScope.launch {
            repository.saveModule(module) { success ->
                if (success) {
                    fetchModules() // Refresh the module list after saving
                    getModuleDetailsByModuleID(module.moduleCode)
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }

    fun saveAttendanceState(attendanceState: AttendanceState) {
        viewModelScope.launch {
            repository.saveAttendanceState(attendanceState) { success ->
                if (success) {
                    fetchAttendanceStates() // Refresh the module list after saving
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }
}

class ModuleViewModelFactory(private val repository: ModuleRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ModuleViewModel::class.java)) {
            return ModuleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for the Module")
    }
}