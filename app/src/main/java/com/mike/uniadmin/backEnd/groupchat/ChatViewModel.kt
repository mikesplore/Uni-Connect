package com.mike.uniadmin.backEnd.groupchat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {
    private val _chats = MutableLiveData<List<ChatEntity>>()
    val chats: LiveData<List<ChatEntity>> = _chats

    private val _groups = MutableLiveData<List<GroupEntity>>()
    val groups: LiveData<List<GroupEntity>> = _groups

    private val _group = MutableLiveData<GroupEntity?>()
    val group: MutableLiveData<GroupEntity?> = _group

    fun fetchChats(path: String) {
        repository.fetchChats(path) { chats ->
            _chats.value = chats
        }
    }

    fun fetchGroups() {
        repository.fetchGroups { groups ->
            _groups.value = groups
        }
    }

    fun fetchGroupById(groupId: String) {
        repository.fetchGroupByID(groupId) { group ->
            _group.value = group
        }
    }

    fun deleteGroup(groupId: String) {
        repository.deleteGroup(groupId) {
            fetchGroups()
        }
    }

    fun saveGroup(group: GroupEntity, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.saveGroup(group, onComplete = { success ->
                if (success) {
                    onSuccess(true)
                    fetchGroups()
                }
            })
        }
    }

    fun saveChat(chat: ChatEntity, path: String, onSuccess: (Boolean) -> Unit) {
        viewModelScope.launch {
            repository.saveChat(chat, path, onComplete = { success ->
                if (success) {
                    onSuccess(true)
                    fetchChats(path)
                } else {
                    onSuccess(false)
                    Log.e("Chats", "Could not save chat")
                }
            })
        }
    }

    fun deleteChat(chatId: String, path: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId,
                onSuccess = {
                    fetchChats(path) // Refresh the chat list after deleting
                },
                onFailure = {
                    // Handle delete failure if needed
                }
            )
        }
    }

    class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for Chats")
        }
    }
}
