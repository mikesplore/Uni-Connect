package com.mike.uniadmin.backEnd.userchat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserChatViewModel(private val repository: UserChatRepository) : ViewModel() {
    private val _userChats = MutableLiveData<List<UserChatEntity>>()
    val userChats: LiveData<List<UserChatEntity>> = _userChats


    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userChatsMap = MutableLiveData<Map<String, List<UserChatEntity>>>()
    private val userChatsMap: LiveData<Map<String, List<UserChatEntity>>> get() = _userChatsMap

    private val _isTyping = MutableLiveData<Boolean>()
    val isTyping: LiveData<Boolean> = _isTyping



    fun markMessageAsRead(message: UserChatEntity, path: String) {
        viewModelScope.launch {
            repository.markMessageAsRead(message.id, path)
        }
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
        _userChatsMap.postValue(emptyMap())
        _isTyping.postValue(false)
    }

    fun fetchCardUserChats(conversationId: String) {
        viewModelScope.launch {
            repository.fetchUserChats(conversationId) { userChats ->
                _userChatsMap.postValue(_userChatsMap.value?.toMutableMap()?.also {
                    it[conversationId] = userChats
                })
            }
        }
    }

    fun getCardUserChats(conversationId: String): LiveData<List<UserChatEntity>> {
        return userChatsMap.map { it[conversationId] ?: emptyList() }
    }



     fun fetchUserChats(path: String) {
         _isLoading.postValue(true)
        repository.fetchUserChats(path) { userChats ->
            _userChats.postValue(userChats)
            _isLoading.postValue(false)
        }
    }


    fun saveMessage(message: UserChatEntity, path: String, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
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
    }

    fun deleteMessage(messageId: String, path: String, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
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
    }




    class UserChatViewModelFactory(private val repository: UserChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserChatViewModel::class.java)) {
                return UserChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for UserChat")
        }
    }
}
