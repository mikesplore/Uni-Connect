package com.mike.uniadmin.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.dataModel.users.UserStateEntity
import java.util.Calendar
import java.util.Locale

data class Update(
    val id: String = "", val version: String = "", val updateLink: String = ""
)


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

    fun generateProgramID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode("Program") { newCode ->
            val indexNumber = "PR$newCode$year"
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

    private fun generateAttendanceID(onIndexNumberGenerated: (String) -> Unit) {
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


    fun writeItem(courseId: String, section: Section, item: GridItem) {
        database.child("Course Resources").child(courseId).child(section.name).push().setValue(item)
            .addOnSuccessListener {
                // Data successfully written
            }.addOnFailureListener { exception ->
                Log.e("FirebaseError", "Error writing item: ${exception.message}")
                // Handle the write error
            }
    }

    fun readItems(courseId: String, section: Section, onItemsRead: (List<GridItem>) -> Unit) {
        database.child("Course Resources").child(courseId).child(section.name)
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

    fun deleteItem(courseId: String, section: Section, item: GridItem) {
        val itemsRef = database.child("Course Resources").child(courseId).child(section.name)
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
}

val database = FirebaseDatabase.getInstance().reference

fun moveNodesToNewParent() {
    val sourceRef = database.child("Programs") // Reference to the "Programs" node
    val destinationRef = sourceRef.child("PR12024") // Reference to the "PR12024" node inside "Programs"

    sourceRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.hasChildren()) { // Check if "Programs" has any children
                val existingData = snapshot.value as Map<*, *>?

                existingData?.let { data ->
                    destinationRef.setValue(data)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Remove the original data from "Programs" (excluding "PR12024")
                                for (child in snapshot.children) {
                                    if (child.key != "PR12024") {
                                        child.ref.removeValue()
                                    }
                                }
                            } else {
                                println("Failed to move data: ${task.exception?.message}")
                            }
                        }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error reading data: ${error.message}")
        }
    })
}



