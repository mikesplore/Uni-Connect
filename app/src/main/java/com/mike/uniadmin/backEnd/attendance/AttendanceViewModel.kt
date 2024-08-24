package com.mike.uniadmin.backEnd.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class AttendanceViewModel (private val attendanceRepository: AttendanceRepository): ViewModel() {
    private val _attendance = MutableLiveData<List<AttendanceEntity>>()
    val attendance: LiveData<List<AttendanceEntity>> get() = _attendance

    init {
        syncAttendanceUpdates()
    }

    fun getAttendanceForStudent(studentId: String, courseId: String) {
        attendanceRepository.getAttendanceForStudent(studentId, courseId) { attendanceList ->
            _attendance.value = attendanceList
        }
    }

    fun signAttendance(attendance: AttendanceEntity) {
        attendanceRepository.insertAttendance(attendance)
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