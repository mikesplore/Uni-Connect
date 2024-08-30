package com.mike.uniadmin.backEnd.moduleContent.moduleTimetable

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.mike.uniadmin.UniAdminPreferences
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime


class ModuleTimetableRepository(private val moduleTimetableDao: ModuleTimetableDao) {
    private val courseCode = UniAdminPreferences.courseCode.value
    private val database = FirebaseDatabase.getInstance().reference.child(courseCode).child("ModuleContent")

    init {
        // Start listening for changes in Firebase when the repository is initialized
        // You may call `listenForFirebaseUpdates` with the appropriate moduleID in practice
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
                Log.d("ModuleTimetableRepository", "Inserted timetable locally for module: $moduleID")

                // Insert into Firebase
                val moduleTimetableRef = database.child(moduleID).child("Module Timetable")
                moduleTimetableRef.child(moduleTimetable.timetableID).setValue(moduleTimetable)
                    .addOnSuccessListener {
                        Log.d("ModuleTimetableRepository", "Timetable written to Firebase for module: $moduleID")
                        onResult(true) // Indicate success
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ModuleTimetableRepository", "Error writing timetable to Firebase: ${exception.message}")
                        onResult(false) // Indicate failure
                    }
            } catch (e: Exception) {
                Log.e("ModuleTimetableRepository", "Error writing timetable locally: ${e.message}")
                onResult(false) // Indicate failure
            }
        }
    }

    // Fetch data from local database for a specific module
    fun getModuleTimetables(moduleID: String, onResult: (List<ModuleTimetable>) -> Unit) {
        uniConnectScope.launch {
            Log.d("ModuleTimetableRepository", "Fetching timetables from local database for module: $moduleID")
            val cachedData = moduleTimetableDao.getModuleTimetables(moduleID)
            Log.d("ModuleTimetableRepository", "Fetched ${cachedData.size} timetables from local database for module: $moduleID")
            onResult(cachedData)
        }
    }

    // Fetch all module timetables from the local database
    fun getAllModuleTimetables(onResult: (List<ModuleTimetable>) -> Unit) {
        uniConnectScope.launch {
            Log.d("ModuleTimetableRepository", "Fetching all timetables from local database")
            val cachedData = moduleTimetableDao.getAllModuleTimetables()
            Log.d("ModuleTimetableRepository", "Fetched ${cachedData.size} timetables from local database")
            onResult(cachedData)
        }
    }

    // Listen for changes in Firebase for a specific module and update local database accordingly
    fun listenForFirebaseUpdates(moduleID: String) {
        val moduleTimetableRef = database.child(moduleID).child("Module Timetable")
        Log.d("ModuleTimetableRepository", "Listening for Firebase updates on path: $moduleTimetableRef")

        moduleTimetableRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                updateLocalDatabase(snapshot)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                updateLocalDatabase(snapshot)
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val timetable = snapshot.getValue(ModuleTimetable::class.java)
                timetable?.let {
                    Log.d("ModuleTimetableRepository", "Removing timetable from local database: ${it.timetableID}")
                    uniConnectScope.launch {
                        moduleTimetableDao.deleteModuleTimetable(it.timetableID)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Handle if necessary
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ModuleTimetableRepository", "Error listening for updates: ${error.message}")
            }

            private fun updateLocalDatabase(snapshot: DataSnapshot) {
                val timetable = snapshot.getValue(ModuleTimetable::class.java)
                timetable?.let {
                    uniConnectScope.launch {
                        moduleTimetableDao.insertModuleTimetable(it)
                        Log.d("ModuleTimetableRepository", "Updated local database with timetable: ${it.timetableID}")
                    }
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
