package com.mike.uniadmin.backEnd.moduleContent.moduleTimetable

import android.util.Log
import com.google.firebase.database.*
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime


class ModuleTimetableRepository(private val moduleTimetableDao: ModuleTimetableDao) {
    private val courseCode = UniAdminPreferences.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("ModuleContent")


    init {
        // Start listening for changes in Firebase
        listenForFirebaseUpdates()
    }

    // Write a module timetable to both local and remote databases
    fun writeModuleTimetable(
        moduleID: String,
        moduleTimetable: ModuleTimetable,
        onResult: (Boolean) -> Unit
    ) {
        uniConnectScope.launch {
            try {
                // Insert into local database
                moduleTimetableDao.insertModuleTimetable(moduleTimetable)
                // Insert into Firebase
                val moduleTimetableRef = database.child(moduleID).child("Module Timetable")
                moduleTimetableRef.child(moduleTimetable.timetableID).setValue(moduleTimetable)
                    .addOnSuccessListener { onResult(true) } // Indicate success
                    .addOnFailureListener { exception ->
                        println("Error writing timetable: ${exception.message}")
                        onResult(false) // Indicate failure
                    }
            } catch (e: Exception) {
                println("Error writing timetable: ${e.message}")
                onResult(false) // Indicate failure
            }
        }
    }

    fun getModuleTimetables(moduleID: String, onResult: (List<ModuleTimetable>) -> Unit) {
        uniConnectScope.launch {
            Log.d("Timetables", "Searching repository timetable for module: $moduleID")

            // Fetch cached data from local database
            val cachedData = moduleTimetableDao.getModuleTimetables(moduleID)

            if (cachedData.isNotEmpty()) {
                // Return cached data if available
                onResult(cachedData)
                Log.d("Timetables", "Found timetable in database")
            } else {
                Log.d("Timetables", "No timetable found in database, fetching from repository")
                val moduleTimetableRef = database.child(moduleID).child("Module Timetable")

                // Fetch data from the remote database
                moduleTimetableRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val timetables = mutableListOf<ModuleTimetable>()
                        for (childSnapshot in snapshot.children) {
                            val timetable = childSnapshot.getValue(ModuleTimetable::class.java)
                            timetable?.let {
                                Log.d("Timetables", "Found timetable in repository")
                                timetables.add(it)
                            }
                        }
                        uniConnectScope.launch {
                            // Update local database with fetched timetables
                            if (timetables.isNotEmpty()) {
                                moduleTimetableDao.insertModuleTimetables(timetables)
                                val updatedData = moduleTimetableDao.getModuleTimetables(moduleID)
                                onResult(updatedData)
                            } else {
                                onResult(emptyList())
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Timetables", "Error reading timetables: ${error.message}")
                        onResult(emptyList())
                    }
                })

                // Listen for real-time updates to the remote database
                moduleTimetableRef.addChildEventListener(object : ChildEventListener {
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val timetable = snapshot.getValue(ModuleTimetable::class.java)
                        timetable?.let {
                            uniConnectScope.launch {
                                moduleTimetableDao.insertModuleTimetable(it)
                                val updatedData = moduleTimetableDao.getModuleTimetables(moduleID)
                                onResult(updatedData)
                            }
                        }
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        val timetable = snapshot.getValue(ModuleTimetable::class.java)
                        timetable?.let {
                            uniConnectScope.launch {
                                moduleTimetableDao.insertModuleTimetable(it)
                                val updatedData = moduleTimetableDao.getModuleTimetables(moduleID)
                                onResult(updatedData)
                            }
                        }
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        val timetable = snapshot.getValue(ModuleTimetable::class.java)
                        timetable?.let {
                            uniConnectScope.launch {
                                moduleTimetableDao.deleteModuleTimetable(it.timetableID)
                                val updatedData = moduleTimetableDao.getModuleTimetables(moduleID)
                                onResult(updatedData)
                            }
                        }
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        // Handle if necessary
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Timetables", "Error handling child event: ${error.message}")
                        onResult(emptyList())
                    }
                })
            }
        }
    }


    fun getAllModuleTimetables(onResult: (List<ModuleTimetable>) -> Unit) {
        uniConnectScope.launch(Dispatchers.IO) { // Ensure this runs on a background thread
            val cachedData = moduleTimetableDao.getAllModuleTimetables()
            onResult(cachedData)
            Log.d("Timetables fetching", "Found timetable in database: $cachedData")
        }
    }



    // Listen for changes in Firebase and update local database accordingly
    private fun listenForFirebaseUpdates() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                updateLocalDatabase(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                updateLocalDatabase(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val timetable = snapshot.getValue(ModuleTimetable::class.java)
                timetable?.let {
                    uniConnectScope.launch {
                        moduleTimetableDao.deleteModuleTimetable(it.timetableID)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                println("Error listening for updates: ${error.message}")
            }

            private fun updateLocalDatabase(snapshot: DataSnapshot) {
                val timetables = mutableListOf<ModuleTimetable>()
                for (childSnapshot in snapshot.children) {
                    val timetable = childSnapshot.getValue(ModuleTimetable::class.java)
                    timetable?.let { timetables.add(it) }
                }
                uniConnectScope.launch {
                    moduleTimetableDao.insertModuleTimetables(timetables)
                }
            }
        })
    }

    fun getUpcomingClass(onResult: (ModuleTimetable?) -> Unit) {
        uniConnectScope.launch(Dispatchers.IO) {
            // Fetch all timetables along with their module names
            val allTimetables = moduleTimetableDao.findNextClass()

            val upcomingTimetable = findUpcomingClass(allTimetables)
            onResult(upcomingTimetable)
        }
    }

    private fun findUpcomingClass(timetables: List<ModuleTimetable>): ModuleTimetable? {
        val currentTime = LocalTime.now()
        val currentDayOfWeek = LocalDate.now().dayOfWeek.value // Monday = 1, Sunday = 7

        // Convert day strings to corresponding day of the week numbers
        val dayMapping = mapOf(
            "Monday" to 1,
            "Tuesday" to 2,
            "Wednesday" to 3,
            "Thursday" to 4,
            "Friday" to 5,
            "Saturday" to 6,
            "Sunday" to 7
        )

        // Sort timetables by day and start time
        val sortedTimetables = timetables.sortedWith(compareBy(
            { dayMapping[it.day] ?: 8 }, // If the day is invalid, it gets sorted last
            { LocalTime.parse(it.startTime) }
        ))

        // Find the first timetable that is after the current time
        for (timetable in sortedTimetables) {
            val timetableDayOfWeek = dayMapping[timetable.day] ?: continue
            val timetableStartTime = LocalTime.parse(timetable.startTime)

            // Check if the timetable is on the same day and after the current time
            if (timetableDayOfWeek == currentDayOfWeek && timetableStartTime.isAfter(currentTime)) {
                return timetable
            }

            // Check if the timetable is on a later day in the week
            if (timetableDayOfWeek > currentDayOfWeek) {
                return timetable
            }
        }

        // If no upcoming timetable is found, return the next week's first class
        return sortedTimetables.firstOrNull()
    }


}
