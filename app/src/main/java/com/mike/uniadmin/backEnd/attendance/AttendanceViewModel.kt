package com.mike.uniadmin.backEnd.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AttendanceViewModel (private val attendanceRepository: AttendanceRepository){
    private val _attendance = MutableLiveData<List<AttendanceEntity>>()
    val attendance: LiveData<List<AttendanceEntity>> get() = _attendance

    fun getAttendanceForStudent(studentId: String, courseId: String) {
        attendanceRepository.getAttendanceForStudent(studentId, courseId) { attendanceList ->
            _attendance.value = attendanceList
        }
    }

    fun insertAttendance(attendance: AttendanceEntity) {
        attendanceRepository.insertAttendance(attendance)
    }



}