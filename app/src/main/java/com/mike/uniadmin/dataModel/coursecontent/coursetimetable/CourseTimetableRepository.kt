package com.mike.uniadmin.dataModel.coursecontent.coursetimetable

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.MyDatabase

class CourseTimetableRepository {
        private val database = FirebaseDatabase.getInstance().reference

        //Course Content database
        fun writeCourseTimetable(courseID: String, courseTimetable: CourseTimetable, onResult: (Boolean) -> Unit) {
            val courseTimetableRef = MyDatabase.database.child("CourseContent")
                .child(courseID)
                .child("courseTimetable")

            courseTimetableRef.child(courseTimetable.timetableID).setValue(courseTimetable)
                .addOnSuccessListener {
                    onResult(true) // Indicate success
                }
                .addOnFailureListener { exception ->
                    println("Error writing timetable: ${exception.message}")
                    onResult(false) // Indicate failure
                }
        }

        fun getCourseTimetables(courseID: String, onResult: (List<CourseTimetable>) -> Unit) {
            val courseTimetableRef = database.child("CourseContent")
                .child(courseID)
                .child("courseTimetable")

            courseTimetableRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val timetables = mutableListOf<CourseTimetable>()
                    for (childSnapshot in snapshot.children) {
                        val timetable = childSnapshot.getValue(CourseTimetable::class.java)
                        timetable?.let { timetables.add(it) }
                    }
                    onResult(timetables)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the read error (e.g., log the error)
                    println("Error reading timetables: ${error.message}")
                }
            })
        }

    }