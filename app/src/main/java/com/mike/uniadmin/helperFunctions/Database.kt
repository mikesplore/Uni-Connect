package com.mike.uniadmin.helperFunctions

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.users.UserStateEntity
import java.util.Calendar
import java.util.Locale

data class Update(
    val id: String = "",
    val version: String = "",
    val updateLink: String = ""
)

private val courseCode = UniAdminPreferences.courseCode.value


object MyDatabase {
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var calendar: Calendar = Calendar.getInstance()
    private var year = calendar.get(Calendar.YEAR)

    fun generateIndexNumber(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("IndexNumber") { newCode ->
            val indexNumber = "CP$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateCourseID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Course") { newCode ->
            val indexNumber = "CR$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateModuleID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Modules") { newCode ->
            val indexNumber = "CS$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateGroupId(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Group") { newCode ->
            val indexNumber = "GD$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }


    fun generateChatID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Chat") { newCode ->
            val indexNumber = "CH$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }


    fun generateFCMID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("FCM") { newCode ->
            val indexNumber = "FC$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateAccountDeletionID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Account Deletion") { newCode ->
            val indexNumber = "AD$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateSharedPreferencesID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("SharedPreferences") { newCode ->
            val indexNumber = "SP$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateFeedbackID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Feedback") { newCode ->
            val indexNumber = "FB$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

     fun generateAttendanceID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Attendance") { newCode ->
            val indexNumber = "AT$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateAnnouncementID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Announcement") { newCode ->
            val indexNumber = "AN$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateTimetableID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Timetable") { newCode ->
            val indexNumber = "TT$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateNotificationID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Notification") { newCode ->
            val indexNumber = "NT$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }


    fun generateAssignmentID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Assignment") { newCode ->
            val indexNumber = "AS$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun getUpdate(onResult: (Update?) -> Unit) {
        val updatesRef = database.child("Updates")
        updatesRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val update = snapshot.getValue(Update::class.java)
                onResult(update)
            } else {
                onResult(null) // Handle the case where no update data exists
            }
        }.addOnFailureListener { exception ->

            Log.e("FirebaseError", "Error fetching update data: ${exception.message}")
            // Handle potential errors during data retrieval
            onResult(null)
        }
    }


    private fun updateAndGetCode(path: String, onCodeUpdated: (Int) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("Codes").child(path)

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val myCode = snapshot.getValue(MyCode::class.java)!!
                    myCode.code += 1
                    database.setValue(myCode).addOnSuccessListener {
                        onCodeUpdated(myCode.code) // Pass the incremented code to the callback
                    }
                } else {
                    val newCode = MyCode(code = 1)
                    database.setValue(newCode).addOnSuccessListener {
                        onCodeUpdated(newCode.code) // Pass the initial code to the callback
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error appropriately (e.g., log it or notify the user)
            }
        })
    }


    fun writeItem(moduleId: String, section: Section, item: GridItem) {
        database.child(courseCode).child("Module Resources").child(moduleId).child(section.name).push().setValue(item)
            .addOnSuccessListener {
                // Data successfully written
            }.addOnFailureListener { exception ->
                Log.e("FirebaseError", "Error writing item: ${exception.message}")
                // Handle the write error
            }
    }

    fun readItems(moduleId: String, section: Section, onItemsRead: (List<GridItem>) -> Unit) {
        database.child(courseCode).child("Module Resources").child(moduleId).child(section.name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val items = snapshot.children.mapNotNull { it.getValue(GridItem::class.java) }
                    onItemsRead(items)
                }

                override fun onCancelled(error: DatabaseError) {
                    onItemsRead(emptyList())
                }
            })
    }

    fun deleteItem(moduleId: String, section: Section, item: GridItem) {
        val itemsRef = database.child(courseCode).child("Module Resources").child(moduleId).child(section.name)
        itemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    if (child.getValue(GridItem::class.java) == item) {
                        child.ref.removeValue().addOnSuccessListener {
                            // Item successfully deleted
                        }.addOnFailureListener { exception ->
                            Log.e("FirebaseError", "Error deleting item: ${exception.message}")
                            // Handle the deletion error
                        }
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error
            }
        })
    }


    fun updatePassword(newPassword: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.updatePassword(newPassword)?.addOnSuccessListener { onSuccess() }
            ?.addOnFailureListener { exception -> onFailure(exception) }
    }

    fun fetchAverageRating(onAverageRatingFetched: (String) -> Unit) {
        val feedbackRef = database.child("Feedback")
        feedbackRef.get().addOnSuccessListener { snapshot ->
            var totalRating = 0.0
            var count = 0

            for (childSnapshot in snapshot.children) {
                val feedback = childSnapshot.getValue(Feedback::class.java)
                feedback?.rating?.let {
                    totalRating += it
                    count++
                }
            }

            val averageRating = if (count > 0) totalRating / count else 0.0
            val formattedAverage = String.format(Locale.US, "%.1f", averageRating)
            onAverageRatingFetched(formattedAverage)
        }.addOnFailureListener {
            onAverageRatingFetched(String.format(Locale.US, "%.1f", 0.0))
        }
    }


    fun writeFeedback(feedback: Feedback, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        val feedbackRef = database.child("Feedback").child(feedback.id)
        feedbackRef.setValue(feedback).addOnSuccessListener {
            onSuccess() // Callback on successful write
        }.addOnFailureListener { exception ->
            onFailure(exception) // Callback on failure with exception
        }
    }

    //send the token to the database

    fun writeFcmToken(token: Fcm) {
        database.child("FCM").child(token.id).setValue(token)
    }


    fun writeUserActivity(userState: UserStateEntity, onSuccess: (Boolean) -> Unit) {
        userState.userID.let {
            database.child("Users Online Status").child(it).setValue(userState)
                .addOnSuccessListener {
                    onSuccess(true)
                }.addOnFailureListener {
                    onSuccess(false)
                }
        }
    }

    private fun checkAttendanceRecord(
        studentID: String,
        courseCode: String,
        date: String,
        onResult: (Boolean) -> Unit
    ) {
        val key = "Attendances/$courseCode/$studentID"
        database.child(key).orderByChild("date").equalTo(date)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        onResult(true) // Attendance record found for today
                    } else {
                        onResult(false) // No attendance record found for today
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error checking attendance record: ${error.message}")
                    onResult(false) // Error occurred, consider it as no record found
                }
            })
    }

    fun fetchAttendances(
        studentID: String, courseCode: String, onAttendanceFetched: (List<Attendance>) -> Unit
    ) {
        val attendanceRef = database.child("Attendances/$courseCode/$studentID")
        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val attendances =
                    snapshot.children.mapNotNull { it.getValue(Attendance::class.java) }
                onAttendanceFetched(attendances)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error, maybe pass an empty list or an error state to the callback
                onAttendanceFetched(emptyList())
            }
        })
    }

    fun setUpdate(update: Update) {
        val updatesRef = database.child("Updates")
        updatesRef.setValue(update)
            .addOnSuccessListener {
                Log.d("FirebaseSuccess", "Update data written successfully")
                // Handle successful write operation
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseError", "Error writing update data: ${exception.message}")
                // Handle potential errors during data writing
            }
    }
}




