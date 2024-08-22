package com.mike.uniadmin.backEnd.userchat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserGroupChatViewModel(private val repository: UserGroupChatRepository) : ViewModel() {
    private val _messages = MutableLiveData<List<UserChatEntity>>()
    val messages: LiveData<List<UserChatEntity>> = _messages


    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _messagesMap = MutableLiveData<Map<String, List<UserChatEntity>>>()
    private val messagesMap: LiveData<Map<String, List<UserChatEntity>>> get() = _messagesMap

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
            _isTyping.value = isUserTyping
        }
    }


    init {
        _messagesMap.value = emptyMap()
        _isTyping.value = false
    }

    fun fetchCardMessages(conversationId: String) {
        viewModelScope.launch {
            repository.fetchMessages(conversationId) { messages -> // Use the callback
                _messagesMap.value = _messagesMap.value?.toMutableMap()?.apply {
                    this[conversationId] = messages
                }
            }
        }
    }


    fun getCardMessages(conversationId: String): LiveData<List<UserChatEntity>> {
        return messagesMap.map { it[conversationId] ?: emptyList() }
    }



     fun fetchMessages(path: String) {
         _isLoading.value = true
        repository.fetchMessages(path) { messages ->
            _messages.value = messages
            _isLoading.value = false
        }
    }

    fun saveMessage(message: UserChatEntity, path: String, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.saveMessage(message, path) { success ->
                if (success) {
                    onSuccess(true)
                    fetchMessages(path) // Refresh the message list after saving
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




    class UserGroupChatViewModelFactory(private val repository: UserGroupChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserGroupChatViewModel::class.java)) {
                return UserGroupChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for UserChat")
        }
    }
}