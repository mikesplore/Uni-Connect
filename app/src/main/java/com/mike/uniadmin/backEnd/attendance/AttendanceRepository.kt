package com.mike.uniadmin.backEnd.attendance

import com.google.firebase.database.*
import com.mike.uniadmin.UniAdminPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttendanceRepository(private val attendanceDao: AttendanceDao) {
    private val courseCode = UniAdminPreferences.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("Attendance")
    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    // Function to get attendance for a specific student and course
    fun getAttendanceForStudent(
        studentId: String,
        courseId: String,
        onResult: (List<AttendanceEntity>) -> Unit
    ) {
        viewModelScope.launch {
            val localAttendance = attendanceDao.getAttendanceForStudent(studentId, courseId)
            if (localAttendance.isNotEmpty()) {
                onResult(localAttendance)
            }

            // Fetch data from Firebase
            database.child(courseId).child(studentId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val attendanceList = mutableListOf<AttendanceEntity>()
                    for (attendanceSnapshot in snapshot.children) {
                        val attendance = attendanceSnapshot.getValue(AttendanceEntity::class.java)
                        attendance?.let {
                            attendanceList.add(it)
                        }
                    }

                    if (attendanceList.isNotEmpty()) {
                        viewModelScope.launch {
                            attendanceDao.insertAllAttendance(attendanceList)
                        }
                        onResult(attendanceList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
        }
    }

    // Function to insert attendance into both local and Firebase database
    fun insertAttendance(attendance: AttendanceEntity) {
        viewModelScope.launch {
            attendanceDao.insertAttendance(attendance)
            // Update Firebase under the specific course and student
            database.child(attendance.courseId).child(attendance.studentId)
                .push().setValue(attendance)
        }
    }

    // Function to continuously listen for attendance updates from Firebase
    fun syncAttendanceUpdates() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                processAttendanceUpdate(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                processAttendanceUpdate(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Handle deletion of attendance records
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}

            private fun processAttendanceUpdate(snapshot: DataSnapshot) {
                val attendanceList = mutableListOf<AttendanceEntity>()
                for (studentSnapshot in snapshot.children) {
                    for (attendanceSnapshot in studentSnapshot.children) {
                        val attendance = attendanceSnapshot.getValue(AttendanceEntity::class.java)
                        attendance?.let {
                            attendanceList.add(it)
                        }
                    }
                }

                if (attendanceList.isNotEmpty()) {
                    viewModelScope.launch {
                        attendanceDao.insertAllAttendance(attendanceList)
                    }
                }
            }
        })
    }
}
