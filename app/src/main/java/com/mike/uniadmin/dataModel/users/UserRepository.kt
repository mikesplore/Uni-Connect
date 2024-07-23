package com.mike.uniadmin.dataModel.users

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mike.uniadmin.model.MyDatabase

class UserRepository {
    private val database = FirebaseDatabase.getInstance().reference.child("Users")

    fun fetchUsers(onResult: (List<User>) -> Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (childSnapshot in snapshot.children) {
                    val user = childSnapshot.getValue(User::class.java)
                    user?.let { users.add(it) }
                }
                onResult(users)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading users: ${error.message}")
            }
        })
    }

    fun saveUser(user: User, onComplete: (Boolean) -> Unit) {
        database.child(user.id).setValue(user).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    fun fetchUserDataByEmail(email: String, callback: (User?) -> Unit) {
        MyDatabase.database.child("Users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userSnapshot = snapshot.children.firstOrNull() // Get the first matching user
                    val user = userSnapshot?.getValue(User::class.java)
                    callback(user) // Return the User object or null if not found
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null) // Handle or log the error as needed
                }
            })
    }

    fun fetchUserDataByAdmissionNumber(admissionNumber: String, callback: (User?) -> Unit) {
        MyDatabase.database.child("Users").orderByChild("id").equalTo(admissionNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userSnapshot = snapshot.children.firstOrNull() // Get the first matching user
                    val user = userSnapshot?.getValue(User::class.java)
                    callback(user) // Return the User object or null if not found
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null) // Handle or log the error as needed
                }
            })
    }


    fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        database.child(userId).removeValue() // Use the consistent database reference
            .addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun writeAccountDeletionData(accountDeletion: AccountDeletion, onSuccess: (Boolean) -> Unit) {
        MyDatabase.database.child("Account Deletion").child(accountDeletion.id).setValue(accountDeletion)
            .addOnSuccessListener {
                onSuccess(true)
            }
            .addOnFailureListener {
                onSuccess(false)
            }
    }

    fun writePreferences(preferences: UserPreferences, onSuccess: (Boolean) -> Unit) {
        MyDatabase.database.child(" User Preferences").child(preferences.studentID).setValue(preferences)
            .addOnSuccessListener {
                onSuccess(true)
            }.addOnFailureListener {
                onSuccess(false)
            }
    }

    fun fetchPreferences(userId: String, onPreferencesFetched: (UserPreferences?) -> Unit) {
        MyDatabase.database.child(" User Preferences").child(userId).get()
            .addOnSuccessListener { snapshot ->
                val preferences = snapshot.getValue(UserPreferences::class.java)
                onPreferencesFetched(preferences)
            }
            .addOnFailureListener {
                onPreferencesFetched(null) // Handle failure, e.g., by returning null
            }
    }


    fun fetchAllUserStatuses(onUserStatesFetched: (List<UserState>) -> Unit) {
        MyDatabase.database.child("UsersState").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userStates = mutableListOf<UserState>()
                for (childSnapshot in snapshot.children) {
                    val userState = childSnapshot.getValue(UserState::class.java)
                    userState?.let { userStates.add(it) }
                }
                onUserStatesFetched(userStates)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading users: ${error.message}")
            }
        })
    }

    fun fetchUserStateByUserId(userId: String, onUserStateFetched: (UserState?) -> Unit) {
        MyDatabase.database.child("UsersState").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userState = snapshot.getValue(UserState::class.java)
                onUserStateFetched(userState)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the read error (e.g., log the error)
                println("Error reading user state: ${error.message}")
                onUserStateFetched(null) // Indicate failure by passing null
            }
        })
    }



}