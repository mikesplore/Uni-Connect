package com.mike.uniadmin.backEnd.userchat

import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mike.uniadmin.backEnd.announcements.uniConnectScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserChatViewModel(private val repository: UserChatRepository) : ViewModel() {
    private val _userChats = MutableLiveData<List<UserChatEntity>>()
    val userChats: LiveData<List<UserChatEntity>> = _userChats


    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var _userChatWithDetails = MutableLiveData<List<UserChatsWithDetails>>()
    val userChatWithDetails: LiveData<List<UserChatsWithDetails>> = _userChatWithDetails


    private val _userCardLoading = MutableLiveData(false)
    val userCardLoading: LiveData<Boolean> = _userCardLoading


    private val _isTyping = MutableLiveData<Boolean>()
    val isTyping: LiveData<Boolean> = _isTyping


    fun markMessageAsRead(message: UserChatEntity, path: String) {
        repository.markMessageAsRead(message.id, path)

    }

    fun updateTypingStatus(path: String, userId: String, isTyping: Boolean) {
        repository.updateTypingStatus(path, userId, isTyping)
    }

    fun listenForTypingStatus(path: String, userId: String) {
        repository.listenForTypingStatus(path, userId) { isUserTyping ->
            _isTyping.postValue(isUserTyping)
        }
    }


    init {
        _isTyping.postValue(false)
        fetchCardUserChats()
    }

    private val userChatsObserver = Observer<List<UserChatsWithDetails>> { userChats ->
        _userChatWithDetails.value = userChats
        _userCardLoading.value = false
    }

    fun fetchCardUserChats() {
        _userCardLoading.value = true
        uniConnectScope.launch(Dispatchers.Main) { // Use a coroutine scope
            val latestChats = withContext(Dispatchers.IO) { // Fetch on a background thread
                repository.getLatestUserChats()
            }
            latestChats.observeForever(userChatsObserver) // Observe the LiveData
        }
        _userCardLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        repository.getLatestUserChats().removeObserver(userChatsObserver) // Remove the observer
    }

    fun fetchUserChats(path: String) {
        _isLoading.value = true
        repository.fetchUserChats(path) { userChats ->
            _userChats.value = userChats
            _isLoading.value = false
        }
    }


    fun saveMessage(message: UserChatEntity, path: String, onSuccess: (Boolean) -> Unit) {
        repository.saveMessage(message, path) { success ->
            if (success) {
                onSuccess(true)
                fetchUserChats(path) // Refresh the message list after saving
            } else {
                onSuccess(false)
                // Handle save failure if needed
            }
        }
    }


    fun deleteMessage(messageId: String, path: String, onSuccess: (Boolean) -> Unit) {
        repository.deleteMessage(messageId, path,
            onSuccess = {
                onSuccess(true)
                Log.d("MessageViewModel", "Message deleted successfully")
            },
            onFailure = {
                onSuccess(false)
                Log.e("MessageViewModel", "Failed to delete message", it)
                // Handle delete failure if needed
            }
        )

    }


    class UserChatViewModelFactory(private val repository: UserChatRepository) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserChatViewModel::class.java)) {
                return UserChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for UserChat")
        }
    }
}
