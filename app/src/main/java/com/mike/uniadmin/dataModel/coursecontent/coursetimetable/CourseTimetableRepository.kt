package com.mike.uniadmin.dataModel.coursecontent.coursetimetable


import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.MyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class CourseTimetableRepository(private val courseTimetableDao: CourseTimetableDao) {
    private val database = FirebaseDatabase.getInstance().reference.child("CourseContent")

    //Course Content database
    fun writeCourseTimetable(
        courseID: String,
        courseTimetable: CourseTimetable,
        onResult: (Boolean) -> Unit
    ) {
        com.mike.uniadmin.dataModel.coursecontent.coursedetails.viewModelScope.launch {
            courseTimetableDao.insertCourseTimetable(courseTimetable)
            val courseTimetableRef = MyDatabase.database.child(courseID)
                .child("courseTimetables")

            courseTimetableRef.child(courseTimetable.timetableID)
                .setValue(courseTimetable).addOnSuccessListener {
                    onResult(true) // Indicate success
                }.addOnFailureListener { exception ->
                    println("Error writing timetable: ${exception.message}")
                    onResult(false) // Indicate failure
                }
        }
    }


    fun getCourseTimetables(courseID: String, onResult: (List<CourseTimetable>) -> Unit) {
        com.mike.uniadmin.dataModel.coursecontent.coursedetails.viewModelScope.launch {
            val cachedData = courseTimetableDao.getCourseTimetables(courseID)
            if (cachedData.isNotEmpty()) {
                onResult(cachedData)
            } else {
                val courseTimetableRef =
                    database.child(courseID).child("courseTimetables")

                courseTimetableRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val timetables = mutableListOf<CourseTimetable>()
                        for (childSnapshot in snapshot.children) {
                            val timetable =
                                childSnapshot.getValue(CourseTimetable::class.java)
                            timetable?.let { timetables.add(it) }
                        }
                        onResult(timetables)
                    }

                    override fun onCancelled(error: DatabaseError) {

                        println("Error reading timetables: ${error.message}")
                    }
                })
            }
        }
    }

}