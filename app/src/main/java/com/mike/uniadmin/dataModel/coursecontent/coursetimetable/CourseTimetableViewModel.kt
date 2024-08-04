package com.mike.uniadmin.dataModel.coursecontent.coursetimetable

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class CourseTimetableViewModel(private val repository: CourseTimetableRepository) : ViewModel() {

    private val _timetables = MutableLiveData<List<CourseTimetable>>()
    val timetables: LiveData<List<CourseTimetable>> = _timetables

    private val _courseTimetables = MutableLiveData<List<CourseTimetable>>()
    val courseTimetables: LiveData<List<CourseTimetable>> = _courseTimetables

    private val _timetablesToday = MutableLiveData<List<CourseTimetable>?>()
    val timetablesToday: LiveData<List<CourseTimetable>?> = _timetablesToday

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Fetch timetables for a specific course
    fun getCourseTimetables(courseID: String) {
        _isLoading.value = true
        repository.getCourseTimetables(courseID) { timetables ->
            _timetables.postValue(timetables) // Use postValue for background updates
            _isLoading.postValue(false)
        }
    }

    //Fetch timetables for all courses
    fun getAllCourseTimetables() {
        _isLoading.value = true
        repository.getAllCourseTimetables { timetables ->
            _courseTimetables.postValue(timetables) // Use postValue for background updates
            _isLoading.postValue(false)
        }
    }

    // Save a new timetable
    fun saveCourseTimetable(courseID: String, timetable: CourseTimetable, onCompletion: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.writeCourseTimetable(courseID, timetable) { success ->
                if (success) {
                    onCompletion(true)
                    getCourseTimetables(courseID) // Refresh the timetable list after saving
                } else {
                    onCompletion(false)
                    // Handle save failure if needed
                    Log.e("CourseTimetableViewModel", "Failed to save timetable")
                }
            }
        }
    }

    fun getTimetableByDay(day: String) {
        _isLoading.value = true
        repository.getTimetableByDay(day) { timetables ->
            _timetablesToday.postValue(timetables) // Use postValue for background updates
            _isLoading.postValue(false)
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