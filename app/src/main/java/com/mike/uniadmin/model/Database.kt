package com.mike.uniadmin.model

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.dataModel.announcements.AnnouncementEntity
import com.mike.uniadmin.dataModel.coursecontent.courseannouncements.CourseAnnouncement
import com.mike.uniadmin.dataModel.coursecontent.courseassignments.CourseAssignment
import com.mike.uniadmin.dataModel.coursecontent.coursedetails.CourseDetails
import com.mike.uniadmin.dataModel.coursecontent.coursetimetable.CourseTimetable
import com.mike.uniadmin.dataModel.users.UserStateEntity
import com.mike.uniadmin.ui.theme.GlobalColors
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

    fun generateDayID(onIndexNumberGenerated: (String) -> Unit) {
        updateAndGetCode { newCode ->
            val indexNumber = "DY$newCode$year"
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



    fun getCourseDetailsByCourseID(courseCode: String, onResult: (Course?) -> Unit) {
        val courseDetailsRef = database.child("Courses").child(courseCode)

        courseDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courseInfo = snapshot.getValue(Course::class.java)
                onResult(courseInfo)
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching course details: ${error.message}")
                onResult(null) // Indicate failure by returning null
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


    fun writeTimetable(timetable: Timetable, onComplete: (Boolean) -> Unit) {
        database.child("Timetable").child(timetable.id).setValue(timetable)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun getTimetable(dayId: String, onAssignmentsFetched: (List<Timetable>?) -> Unit) {
        database.child("Timetable").orderByChild("dayId").equalTo(dayId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val timetable =
                        snapshot.children.mapNotNull { it.getValue(Timetable::class.java) }
                    onAssignmentsFetched(timetable)
                }

                override fun onCancelled(error: DatabaseError) {
                    onAssignmentsFetched(null)
                }
            })
    }

    fun getCurrentDayTimetable(dayName: String, onTimetableFetched: (List<Timetable>?) -> Unit) {
        // Step 1: Fetch the dayId from the Day node using the dayName
        database.child("Days").orderByChild("name").equalTo(dayName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dayId = snapshot.children.firstOrNull()?.key

                    if (dayId != null) {
                        // Step 2: Use the fetched dayId to query the Timetable node
                        database.child("Timetable").orderByChild("dayId").equalTo(dayId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val timetable =
                                        snapshot.children.mapNotNull { it.getValue(Timetable::class.java) }
                                    onTimetableFetched(timetable)
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    onTimetableFetched(null)
                                }
                            })
                    } else {
                        // Day not found
                        onTimetableFetched(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onTimetableFetched(null)
                }
            })
    }


    fun editTimetable(timetable: Timetable, onComplete: (Boolean) -> Unit) {
        database.child("Timetable").child(timetable.id).setValue(timetable)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun deleteTimetable(timetableId: String, onComplete: (Boolean) -> Unit) {
        database.child("Timetable").child(timetableId).removeValue().addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }


    fun writeDays(day: Day, onComplete: (Boolean) -> Unit) {
        database.child("Days").child(day.id).setValue(day).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun getDays(onCoursesFetched: (List<Day>?) -> Unit) {
        database.child("Days").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val days = snapshot.children.mapNotNull { it.getValue(Day::class.java) }
                onCoursesFetched(days)
            }

            override fun onCancelled(error: DatabaseError) {
                onCoursesFetched(null)
            }
        })
    }

    fun writeAssignment(assignment: Assignment, onComplete: (Boolean) -> Unit) {
        database.child("Assignments").child(assignment.id).setValue(assignment)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun getAssignments(courseCode: String, onAssignmentsFetched: (List<Assignment>?) -> Unit) {
        database.child("Assignments").orderByChild("courseCode").equalTo(courseCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val assignments =
                        snapshot.children.mapNotNull { it.getValue(Assignment::class.java) }
                    onAssignmentsFetched(assignments)
                }

                override fun onCancelled(error: DatabaseError) {
                    onAssignmentsFetched(null)
                }
            })
    }

    fun deleteAssignment(assignmentId: String, onComplete: (Boolean) -> Unit) {
        database.child("Assignments").child(assignmentId).removeValue()
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }

    fun editAssignment(assignment: Assignment, onComplete: (Boolean) -> Unit) {
        database.child("Assignments").child(assignment.id).setValue(assignment)
            .addOnCompleteListener { task ->
                onComplete(task.isSuccessful)
            }
    }






    fun deleteAnnouncement(announcementId: String) {
        database.child("Announcements").child(announcementId.toString()).removeValue()
    }

    fun loadCourseAndAssignments(callback: (List<Course>?) -> Unit) {
        database.child("Courses").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val courseList = task.result.children.mapNotNull { dataSnapshot ->
                    val course = dataSnapshot.getValue(Course::class.java)
                    if (course?.courseName.isNullOrEmpty()) {
                        Log.e("DataFetch", "Course with missing name: $dataSnapshot")
                        null
                    } else {
                        course
                    }
                }
                callback(courseList)
            } else {
                Log.e("DataFetch", "Error fetching courses: ${task.exception?.message}")
                callback(null)
            }
        }
    }


    fun loadAttendanceRecords(onAttendanceRecordsLoaded: (List<AttendanceRecord>?) -> Unit) {
        database.child("attendanceRecords")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val attendanceRecords = snapshot.children.mapNotNull {
                        val studentId = it.child("studentId").getValue(String::class.java)
                        val dayOfWeek = it.child("dayOfWeek").getValue(String::class.java)
                        val isPresent = it.child("isPresent").getValue(Boolean::class.java)
                        val lesson = it.child("lesson").getValue(String::class.java)
                        if (studentId != null && dayOfWeek != null && isPresent != null && lesson != null) {
                            AttendanceRecord(studentId, dayOfWeek, isPresent, lesson)
                        } else null
                    }
                    onAttendanceRecordsLoaded(attendanceRecords)
                }

                override fun onCancelled(error: DatabaseError) {
                    onAttendanceRecordsLoaded(null)
                }
            }
            )
    }

    fun saveAttendanceState(attendanceState: AttendanceState) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("AttendanceStates").child(attendanceState.courseID).setValue(attendanceState)
    }


    fun fetchAttendanceState(courseCode: String, onStateFetched: (AttendanceState?) -> Unit) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("AttendanceStates").child(courseCode).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val attendanceState = snapshot.getValue(AttendanceState::class.java)
                onStateFetched(attendanceState) // Pass the fetched state or null if not found
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error, maybe pass null to indicate failure
                onStateFetched(null)
            }
        })
    }

    //fetch the day id using the day name
    fun getDayIdByName(dayName: String, onDayIdFetched: (String?) -> Unit) {
        database.child("Days").orderByChild("name").equalTo(dayName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dayId = snapshot.children.firstOrNull()?.key
                    onDayIdFetched(dayId)
                }

                override fun onCancelled(error: DatabaseError) {
                    onDayIdFetched(null)
                }
            }
            )
    }

    fun saveAttendanceRecords(records: List<AttendanceRecord>, onComplete: (Boolean) -> Unit) {
        val batch = database.child("attendanceRecords")
        records.map { record ->
            val key = batch.push().key ?: ""
            batch.child(key).setValue(record)
        }
    }

    fun ExitScreen(context: Context, screenID: String, timeSpent: Long){

        GlobalColors.loadColorScheme(context)
        // Fetch the screen details
        MyDatabase.getScreenDetails(screenID) { screenDetails ->
            if (screenDetails != null) {
                MyDatabase.writeScren(courseScreen = screenDetails) {}
                // Fetch existing screen time
                MyDatabase.getScreenTime(screenID) { existingScreenTime ->
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
                    MyDatabase.saveScreenTime(screenTime = screenTime, onSuccess = {
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


    //Course Content database
    fun writeCourseAnnouncement(courseID: String, courseAnnouncement: CourseAnnouncement, onResult: (Boolean) -> Unit) {
        val courseAnnouncementRef = database.child("CourseContent")
            .child(courseID)
            .child("courseAnnouncements")

        courseAnnouncementRef.child(courseAnnouncement.announcementID).setValue(courseAnnouncement)
            .addOnSuccessListener {
                onResult(true) // Indicate success
            }
            .addOnFailureListener { exception ->
                println("Error writing announcement: ${exception.message}")
                onResult(false) // Indicate failure
            }
    }


    fun getCourseAnnouncements(courseID: String, onResult: (List<CourseAnnouncement>) -> Unit) {
        val courseAnnouncementRef = database.child("CourseContent")
            .child(courseID)
            .child("courseAnnouncements")

        courseAnnouncementRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val announcements = mutableListOf<CourseAnnouncement>()
                for (childSnapshot in snapshot.children) {
                    val announcement = childSnapshot.getValue(CourseAnnouncement::class.java)
                    announcement?.let { announcements.add(it) }
                }
                onResult(announcements)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading announcements: ${error.message}")
            }
        })
    }

    //Course Assignments database
    fun writeCourseAssignments(courseID: String, courseAssignment: CourseAssignment, onResult: (Boolean) -> Unit) {
        val courseAnnouncementRef = database.child("CourseContent")
            .child(courseID)
            .child("courseAssignments")

        courseAnnouncementRef.child(courseAssignment.assignmentID).setValue(courseAssignment)
            .addOnSuccessListener {
                onResult(true) // Indicate success
            }
            .addOnFailureListener { exception ->
                println("Error writing assignment: ${exception.message}")
                onResult(false) // Indicate failure
            }
    }


    fun getCourseAssignments(courseID: String, onResult: (List<CourseAssignment>) -> Unit) {
        val courseAssignmentRef = database.child("CourseContent")
            .child(courseID)
            .child("courseAssignments")

        courseAssignmentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val assignments = mutableListOf<CourseAssignment>()
                for (childSnapshot in snapshot.children) {
                    val assignment = childSnapshot.getValue(CourseAssignment::class.java)
                    assignment?.let { assignments.add(it) }
                }
                onResult(assignments)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading assignment: ${error.message}")
            }
        })
    }

    //Course Timetable database
    fun writeCourseTimetable(courseID: String, courseTimetable: CourseTimetable, onResult: (Boolean) -> Unit) {
        val courseTimetableRef = database.child("CourseContent")
            .child(courseID)
            .child("courseTimetable")

        courseTimetableRef.child(courseTimetable.timetableID).setValue(courseTimetable)
            .addOnSuccessListener {
                onResult(true) // Indicate success
            }
            .addOnFailureListener { exception ->
                println("Error writing assignment: ${exception.message}")
                onResult(false) // Indicate failure
            }
    }


    fun getCourseTimetable(courseID: String, onResult: (List<CourseTimetable>) -> Unit) {
        val courseAnnouncementRef = database.child("CourseContent")
            .child(courseID)
            .child("courseTimetable")

        courseAnnouncementRef.addValueEventListener(object : ValueEventListener {
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
                println("Error reading timetable: ${error.message}")
            }
        })
    }

    //Course Details database
    fun writeCourseDetails(courseID: String, courseDetails: CourseDetails, onResult: (Boolean) -> Unit) {
        val courseTimetableRef = database.child("CourseContent")
            .child(courseID)
            .child("courseDetails")

        courseTimetableRef.child(courseDetails.detailsID).setValue(courseDetails)
            .addOnSuccessListener {
                onResult(true) // Indicate success
            }
            .addOnFailureListener { exception ->
                println("Error writing details: ${exception.message}")
                onResult(false) // Indicate failure
            }
    }


    fun getCourseDetails(courseID: String, onResult: (CourseDetails?) -> Unit) {
        val courseDetailsRef = database.child("CourseContent")
            .child(courseID)
            .child("courseDetails")
            .child(courseID)// Directly access the course node

        courseDetailsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courseDetails = snapshot.getValue(CourseDetails::class.java)
                onResult(courseDetails) // Return the single CourseDetails object
                Log.d("Course Details", "Course details fetched: $courseDetails")
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching course details: ${error.message}")
                onResult(null) // Indicate failure by returning null
            }
        })
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


