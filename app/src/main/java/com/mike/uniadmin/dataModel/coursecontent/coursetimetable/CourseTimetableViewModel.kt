package com.mike.uniadmin.dataModel.coursecontent.coursetimetable


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class CourseTimetableViewModel(private val repository: CourseTimetableRepository) : ViewModel() {

    private val _timetables = MutableLiveData<List<CourseTimetable>>()
    val timetables: LiveData<List<CourseTimetable>> = _timetables

    // Fetch timetables for a specific course
    fun getCourseTimetables(courseID: String) {
        repository.getCourseTimetables(courseID) { timetables ->
            _timetables.value = timetables
        }
    }

    // Save a new timetable
    fun saveCourseTimetable(courseID: String, timetable: CourseTimetable) {
        viewModelScope.launch {
            repository.writeCourseTimetable(courseID, timetable) { success ->
                if (success) {
                    getCourseTimetables(courseID) // Refresh the timetable list after saving
                } else {
                    // Handle save failure if needed
                }
            }
        }
    }
}

class CourseTimetableViewModelFactory(private val repository: CourseTimetableRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseTimetableViewModel::class.java)) {
            return CourseTimetableViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for CourseTimetable")
    }
}