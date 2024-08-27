package com.mike.uniadmin.backEnd.attendance

import android.util.Log
import com.google.firebase.database.*
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttendanceRepository(private val attendanceDao: AttendanceDao) {
    private val courseCode = UniAdminPreferences.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("Attendance")

    // Function to get attendance for a specific student and course
    fun getAttendanceForStudent(
        studentId: String,
        courseId: String,
        onResult: (List<AttendanceEntity>) -> Unit
    ) {
        uniConnectScope.launch {
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
                            Log.d("AttendanceRepository", "Attendance fetched from Firebase: $it")
                        }
                    }

                    if (attendanceList.isNotEmpty()) {
                        uniConnectScope.launch {
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

    fun signAttendance(attendance: AttendanceEntity, success: (Boolean) -> Unit) {
        uniConnectScope.launch {
            // Insert into Room database
            attendanceDao.insertAttendance(attendance)
            // Update Firebase under the specific course and student
            database.child(attendance.moduleId).child(attendance.studentId)
                .child(attendance.id).setValue(attendance)
                .addOnSuccessListener {
                    success(true)
                }
                .addOnFailureListener {
                    success(false)
                }

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
                    uniConnectScope.launch {
                        attendanceDao.insertAllAttendance(attendanceList)
                    }
                }
            }
        })
    }
}
