package com.mike.uniadmin.dataModel.users

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    private val _user = MutableLiveData<User?>()
    var user: MutableLiveData<User?> = _user
    private val _user2 = MutableLiveData<User>()
    var user2: MutableLiveData<User> = _user2
    private val _userStates = MutableLiveData<Map<String, UserState>>()
    val userStates: LiveData<Map<String, UserState>> = _userStates

    private val _userState = MutableLiveData<UserState?>()
    var userState: LiveData<UserState?> = _userState

    override fun onCleared() {
        super.onCleared()
        // Clear all necessary data here
        user.value = null
    }

    fun checkUserStateByID(userID: String) = viewModelScope.launch {
        repository.fetchUserStateByUserId(userID) { fetchedUserState ->
            _userState.value = fetchedUserState // Update the value of the LiveData
            Log.d("UserStates", "UserState for user Id: $userID: $fetchedUserState")
        }
    }

    fun checkAllUserStatuses() {
        repository.fetchAllUserStatuses { userStates ->
            _userStates.value = userStates.associateBy { it.userID }
        }
    }



    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        repository.fetchUsers { users ->
            _users.value = users
        }
    }



    fun findUserByEmail(email: String, onUserFetched: (User?) -> Unit) {
        repository.fetchUserDataByEmail(email) { user ->
            _user.postValue(user)
            onUserFetched(user)
        }
    }


    fun findUserByAdmissionNumber(admissionNumber: String){
        repository.fetchUserDataByAdmissionNumber(admissionNumber){ fetchedUser ->
            _user2.value = fetchedUser
        }
    }

    fun updateUser(user: User) {
        _user.value = user
    }

    fun writeUser(user: User, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.saveUser(user) { success ->
                if (success) {
                    onSuccess(true)
                    fetchUsers() // Refresh the user list after saving
                } else {
                    onSuccess(false)
                    // Handle save failure if needed
                }
            }
        }
    }

    fun writeAccountDeletionData(accountDeletion: AccountDeletion, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.writeAccountDeletionData(accountDeletion, onSuccess = { success ->
                if (success) {
                    onSuccess(true)
                } else {
                    onSuccess(false)
                    Log.e(
                        "writeAccountDeletionData",
                        "Failed to write writeAccountDeletionData data"
                    )
                }

            })
        }
    }
    fun fetchPreferences(userID: String, onPreferencesFetched: (UserPreferences?) -> Unit){
        viewModelScope.launch {
            repository.fetchPreferences(userID, onPreferencesFetched)
        }
    }

    fun writePreferences(preferences: UserPreferences, onSuccess: (Boolean) -> Unit){
        viewModelScope.launch {
            repository.writePreferences(preferences, onSuccess = { success ->
                if (success) {
                    onSuccess(true)
                } else {
                    onSuccess(false)
                    Log.e("writePreferences", "Failed to write preferences data")
                }
            })
        }
    }
}



class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}