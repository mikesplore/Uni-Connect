package com.mike.uniadmin.backEnd.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class AttendanceViewModel (private val attendanceRepository: AttendanceRepository): ViewModel() {
    private val _attendance = MutableLiveData<List<AttendanceEntity>>()
    val attendance: LiveData<List<AttendanceEntity>> get() = _attendance

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    init {
        syncAttendanceUpdates()
    }

    fun getAttendanceForStudent(studentId: String, courseId: String, callback: (List<AttendanceEntity>) -> Unit) {
        attendanceRepository.getAttendanceForStudent(studentId, courseId) { attendanceList ->
            callback(attendanceList)
            _attendance.postValue(attendanceList)
        }
    }

    fun signAttendance(attendance: AttendanceEntity, success: (Boolean) -> Unit) {
        attendanceRepository.signAttendance(attendance){ onSuccess ->
            success(onSuccess)
        }
    }

    private fun syncAttendanceUpdates() {
        attendanceRepository.syncAttendanceUpdates()

    }

    class AttendanceViewModelFactory(private val repository: AttendanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
                return AttendanceViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for Chats")
        }
    }

}