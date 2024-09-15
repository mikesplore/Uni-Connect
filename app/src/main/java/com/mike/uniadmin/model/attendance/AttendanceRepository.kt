package com.mike.uniadmin.model.attendance

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.CourseManager
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.model.announcements.uniConnectScope
import kotlinx.coroutines.launch

class AttendanceRepository(private val attendanceDao: AttendanceDao) {
    private val courseCode = CourseManager.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("Attendance")
    private val studentId = UniAdminPreferences.userID.value

    // Function to get attendance for a specific student and course
    fun getAttendanceForStudent(
        courseId: String,
        onResult: (List<AttendanceEntity>) -> Unit
    ) {
        uniConnectScope.launch {
            val localAttendance = attendanceDao.getAttendanceForStudent(studentId, courseId)
            if (localAttendance.isNotEmpty()) {
                onResult(localAttendance)
            }
        }

        // Fetch data from Firebase
        database.child(courseId).child(studentId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
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

    // Function to sign attendance
    fun signAttendance(attendance: AttendanceEntity, success: (Boolean) -> Unit) {
        uniConnectScope.launch {
            // Insert into Room database
            attendanceDao.insertAttendance(attendance)
        }
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

    // Function to continuously listen for attendance updates from Firebase
    fun syncAttendanceUpdates() {
        database.child(studentId).addChildEventListener(object : ChildEventListener {
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

    // Function to get all attendances for the current course
    fun getAllAttendances(onResult: (List<AttendanceEntity>) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allAttendanceList = mutableListOf<AttendanceEntity>()
                for (studentSnapshot in snapshot.children) {
                    for (attendanceSnapshot in studentSnapshot.children) {
                        val attendance = attendanceSnapshot.getValue(AttendanceEntity::class.java)
                        attendance?.let {
                            allAttendanceList.add(it)
                        }
                    }
                }

                if (allAttendanceList.isNotEmpty()) {
                    uniConnectScope.launch {
                        attendanceDao.insertAllAttendance(allAttendanceList)
                    }
                }
                onResult(allAttendanceList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors
                Log.e("AttendanceRepository", "Failed to fetch all attendances: ${error.message}")
                onResult(emptyList()) // Return empty list on error
            }
        })
    }
}
