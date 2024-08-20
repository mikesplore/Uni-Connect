package com.mike.uniadmin.backEnd.users

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> = _users

    private val _user = MutableLiveData<UserEntity?>()
    var user: MutableLiveData<UserEntity?> = _user

    private val _user2 = MutableLiveData<UserEntity?>()
    var user2: MutableLiveData<UserEntity?> = _user2

    private val _userStates = MutableLiveData<Map<String, UserStateEntity>>()
    val userStates: LiveData<Map<String, UserStateEntity>> = _userStates

    private val _userState = MutableLiveData<UserStateEntity?>()
    var userState: LiveData<UserStateEntity?> = _userState

    private val _accountStatus = MutableLiveData<AccountDeletionEntity?>()
    var accountStatus: LiveData<AccountDeletionEntity?> = _accountStatus

    private val _signedInUser = MutableLiveData<SignedInUser?>()
    val signedInUser: LiveData<SignedInUser?> = _signedInUser

    private val _isLoading = MutableLiveData(false) // Add isLoading state
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchUsers()
        getSignedInUser()
    }

    fun getSignedInUser() {
        _isLoading.value = true // Set loading to true before fetching
        repository.getSignedInUser { fetchedUser ->
            _signedInUser.value = fetchedUser
            _isLoading.value = false // Set loading to false after fetching
            Log.d("UserViewModel", "Fetched user: $fetchedUser")
        }
    }

    fun setSignedInUser(signedInUser: SignedInUser) {
        viewModelScope.launch {
            repository.setSignedInUser(signedInUser)
        }
    }

    fun deleteSignedInUser() {
        viewModelScope.launch {
            repository.deleteSignedInUser()
        }
    }

    fun deleteAllTables(){
        repository.deleteAllTables()
    }

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


     fun fetchUsers() {
        repository.fetchUsers { users ->
            _users.value = users
        }
    }



    fun findUserByEmail(email: String, onUserFetched: (UserEntity?) -> Unit) {
        _isLoading.value = true // Set loading to true before fetching
        repository.fetchUserDataByEmail(email) { user ->
            _user.postValue(user)
            onUserFetched(user)
            _isLoading.value = false // Set loading to false after fetching
        }
    }


    fun findUserByAdmissionNumber(admissionNumber: String){
        repository.fetchUserDataByAdmissionNumber(admissionNumber){ fetchedUser ->
            _user2.value = fetchedUser
        }
    }


    fun writeUser(user: UserEntity, onSuccess: (Boolean) -> Unit) {
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

    fun writeAccountDeletionData(accountDeletion: AccountDeletionEntity, onSuccess: (Boolean) -> Unit) {
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

    fun checkAccountDeletionData(userID: String) {
        viewModelScope.launch {
            repository.checkAccountDeletionData(userID, onComplete = { fetchedAccountStatus ->
                _accountStatus.value = fetchedAccountStatus

            })
        }
    }

    fun deleteAccount(userId: String, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.deleteUser(userId, onSuccess = { success ->
                if (success) {
                    onSuccess(true)
                } else {
                    onSuccess(false)
                }
            })
        }
    }

    fun fetchPreferences(userID: String, onPreferencesFetched: (UserPreferencesEntity?) -> Unit){
        viewModelScope.launch {
            repository.fetchPreferences(userID, onPreferencesFetched)
        }
    }

    fun writePreferences(preferences: UserPreferencesEntity, onSuccess: (Boolean) -> Unit){
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