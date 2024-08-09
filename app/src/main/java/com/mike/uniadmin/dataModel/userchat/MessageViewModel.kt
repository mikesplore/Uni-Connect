package com.mike.uniadmin.dataModel.userchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MessageViewModel(private val repository: MessageRepository) : ViewModel() {
    private val _messages = MutableLiveData<List<MessageEntity>>()
    val messages: LiveData<List<MessageEntity>> = _messages

    private val _cardMessages = MutableLiveData<List<MessageEntity>>()
    val cardMessages: LiveData<List<MessageEntity>> = _cardMessages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _messagesMap = MutableLiveData<Map<String, List<MessageEntity>>>()
    private val messagesMap: LiveData<Map<String, List<MessageEntity>>> get() = _messagesMap

    init {
        _messagesMap.value = emptyMap()
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


    fun getCardMessages(conversationId: String): LiveData<List<MessageEntity>> {
        return messagesMap.map { it[conversationId] ?: emptyList() }
    }

     fun fetchMessages(path: String) {
         _isLoading.value = true
        repository.fetchMessages(path) { messages ->
            _messages.value = messages
            _isLoading.value = false
        }
    }

    fun saveMessage(message: MessageEntity, path: String, onSuccess: (Boolean) -> Unit) {
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