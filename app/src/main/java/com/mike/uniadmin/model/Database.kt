package com.mike.uniadmin.model

import android.content.Context
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
    val id: String = "",
    val version: String = "",
    val updateLink: String = ""
)


object MyDatabase {
    val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var calendar: Calendar = Calendar.getInstance()
    private var year = calendar.get(Calendar.YEAR)


    fun generateIndexNumber(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "CP$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateGroupId(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "GD$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }


    fun generateChatID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "CH$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }


    fun generateFCMID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "FC$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateAccountDeletionID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "AD$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateSharedPreferencesID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "SP$newCode$year"
            onIndexNumberGenerated(indexNumber) // Pass the generated index number to the callback
        }
    }

    fun generateFeedbackID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "FB$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    private fun generateAttendanceID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "AT$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateAnnouncementID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "AN$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateTimetableID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "TT$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateNotificationID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "NT$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }


    fun generateAssignmentID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "AS$newCode$year"
            onIndexNumberGenerated(indexNumber)
        }
    }

    fun generateScreenTimeID(onLastDateGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val dateCode = "ST$newCode$year"
            onLastDateGenerated(dateCode) // Pass the generated index number to the callback
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
            // Handle potential errors during data retrieval
            onResult(null)
        }
    }


    private fun updateAndGetCode(onCodeUpdated: (Int) -> Unit) {
        val database = FirebaseDatabase.getInstance().getReference("Code")

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
            }
            .addOnFailureListener { exception ->
                // Handle the write error
            }
    }

    fun readItems(courseId: String, section: Section, onItemsRead: (List<GridItem>) -> Unit) {
        database.child("Course Resources").child(courseId).child(section.name).addListenerForSingleValueEvent(object : ValueEventListener {
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
                        child.ref.removeValue()
                            .addOnSuccessListener {
                                // Item successfully deleted
                            }
                            .addOnFailureListener { exception ->
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



    fun saveScreenTime(
        screenTime: ScreenTime,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        val screenTimeRef = database.child("ScreenTime").child(screenTime.id)
        screenTimeRef.setValue(screenTime).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    fun writeScren(courseScreen: Screens, onSuccess: () -> Unit) {
        database.child("Screens").child(courseScreen.screenId).setValue(courseScreen)
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener {}
    }

    fun getScreenTime(screenID: String, onScreenTimeFetched: (ScreenTime?) -> Unit) {
        val screenTimeRef = database.child("ScreenTime").child(screenID)
        screenTimeRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val retrievedScreenTime = snapshot.getValue(ScreenTime::class.java)
                onScreenTimeFetched(retrievedScreenTime)
            } else {
                onScreenTimeFetched(null)
            }
        }.addOnFailureListener {
            onScreenTimeFetched(null)
        }
    }

    fun getAllScreenTimes(onScreenTimesFetched: (List<ScreenTime>) -> Unit) {
        val screenTimeRef = database.child("ScreenTime")
        screenTimeRef.get().addOnSuccessListener { snapshot ->
            val screenTimes = mutableListOf<ScreenTime>()
            for (childSnapshot in snapshot.children) {
                val screenTime = childSnapshot.getValue(ScreenTime::class.java)
                screenTime?.let { screenTimes.add(it) }
            }
            onScreenTimesFetched(screenTimes)
        }.addOnFailureListener {
            onScreenTimesFetched(emptyList()) // Return an empty list on failure
        }
    }

    fun getScreenDetails(screenID: String, onScreenDetailsFetched: (Screens?) -> Unit) {
        val screenDetailsRef = database.child("Screens").child(screenID)
        screenDetailsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val screenDetails = snapshot.getValue(Screens::class.java)
                onScreenDetailsFetched(screenDetails)
            } else {
                onScreenDetailsFetched(null)
            }
        }.addOnFailureListener {
            onScreenDetailsFetched(null)
        }
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
        feedbackRef.setValue(feedback)
            .addOnSuccessListener {
                onSuccess() // Callback on successful write
            }
            .addOnFailureListener { exception ->
                onFailure(exception) // Callback on failure with exception
            }
    }

    //send the token to the database

    fun writeFcmToken(token: Fcm) {
        database.child("FCM").child(token.id).setValue(token)
    }



    fun loadAttendanceRecords(onAttendanceRecordsLoaded: (List<AttendanceRecord>?) -> Unit) {
        database.child("attendanceRecords").get().addOnSuccessListener { snapshot ->
            val attendanceRecords = snapshot.children.mapNotNull { it.getValue(AttendanceRecord::class.java) }
            onAttendanceRecordsLoaded(attendanceRecords)
        }.addOnFailureListener {
            onAttendanceRecordsLoaded(null)
        }
    }


    fun saveAttendanceRecords(records: List<AttendanceRecord>, onComplete: (Boolean) -> Unit) {
        val batch = database.child("attendanceRecords")
        records.map { record ->
            val key = batch.push().key ?: ""
            batch.child(key).setValue(record)
        }
    }

    fun ExitScreen(context: Context, screenID: String, timeSpent: Long){

        
        // Fetch the screen details
        getScreenDetails(screenID) { screenDetails ->
            if (screenDetails != null) {
                writeScren(courseScreen = screenDetails) {}
                // Fetch existing screen time
                getScreenTime(screenID) { existingScreenTime ->
                    val totalScreenTime = if (existingScreenTime != null) {
                        Log.d("Screen Time", "Retrieved Screen time: $existingScreenTime")
                        existingScreenTime.time + timeSpent
                    } else {
                        timeSpent
                    }

                    // Create a new ScreenTime object
                    val screenTime = ScreenTime(
                        id = screenID,
                        screenName = screenDetails.screenName,
                        time = totalScreenTime
                    )

                    // Save the updated screen time
                    saveScreenTime(screenTime = screenTime, onSuccess = {
                        Log.d("Screen Time", "Saved $totalScreenTime to the database")
                    }, onFailure = {
                        Log.d("Screen Time", "Failed to save $totalScreenTime to the database")
                    })
                }

            } else {
                Log.d("Screen Time", "Screen details not found for ID: $screenID")
            }
        }
    }


    fun writeUserActivity(userState: UserStateEntity, onSuccess: (Boolean) -> Unit) {
        userState.userID?.let {
            database.child("Users Online Status").child(it).setValue(userState)
                .addOnSuccessListener {
                    onSuccess(true)
                }.addOnFailureListener {
                    onSuccess(false)
                }
        }
    }

}


