package com.mike.uniadmin.dataModel.programs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProgramViewModel(private val repository: ProgramRepository) : ViewModel() {
    private val _programs = MutableLiveData<List<ProgramEntity>>()
    private val _fetchedProgram = MutableLiveData<ProgramEntity?>()
    val programs: LiveData<List<ProgramEntity>> = _programs
    val fetchedProgram: LiveData<ProgramEntity?> = _fetchedProgram

    private val _programStates = MutableLiveData<Map<String, ProgramState>>()
    val programStates: LiveData<Map<String, ProgramState>> = _programStates

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _programCode = MutableLiveData<String>()
    val programCode: LiveData<String> = _programCode


    init {
        fetchPrograms()
        fetchProgramStates()
    }
    
    fun getProgramCode(onResult: (String) -> Unit) {
        repository.getProgramCode(onResult)
    }
    
    fun insertProgramCode(program: Program) {
        repository.insertProgramCode(program)
    }

    private fun fetchProgramStates() {
        repository.fetchProgramStates { fetchedStates ->
            val statesMap = fetchedStates.associateBy { it.programID }
            _programStates.value = statesMap

        }
    }


    fun fetchPrograms() {
        _isLoading.value = true // Set loading to true before fetching
        repository.fetchPrograms { programs ->
            _programs.value = programs
            _isLoading.value = false // Set loading to false after fetching
        }
    }

    fun getProgramDetailsByProgramID(programCode: String) {
        repository.getProgramDetailsByProgramID(programCode) { program ->
            _fetchedProgram.value = program
        }
    }

    fun saveProgram(program: ProgramEntity, onProgramSaved: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.saveProgram(program) { success ->
                onProgramSaved(success)
                if (success) {
                    fetchPrograms() // Refresh the program list after saving
                    getProgramDetailsByProgramID(program.programCode)
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }

    fun saveProgramState(programState: ProgramState) {
        viewModelScope.launch {
            repository.saveProgramState(programState) { success ->
                if (success) {
                    fetchProgramStates() // Refresh the program list after saving
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }
}

class ProgramViewModelFactory(private val repository: ProgramRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgramViewModel::class.java)) {
            return ProgramViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for the Program")
    }
}