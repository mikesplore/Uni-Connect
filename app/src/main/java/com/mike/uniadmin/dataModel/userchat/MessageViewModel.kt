package com.mike.uniadmin.dataModel.userchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MessageViewModel(private val repository: MessageRepository) : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

     fun fetchMessages(path: String) {
        repository.fetchMessages(path) { messages ->
            _messages.value = messages
        }
    }

    fun saveMessage(message: Message, path: String, onSuccess: (Boolean) -> Unit) {
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

    fun deleteMessage(messageId: String, path: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId,
                onSuccess = {
                    fetchMessages(path) // Refresh the message list after deleting
                },
                onFailure = {
                    // Handle delete failure if needed
                }
            )
        }
    }

    class MessageViewModelFactory(private val repository: MessageRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MessageViewModel::class.java)) {
                return MessageViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for Messages")
        }
    }
}
