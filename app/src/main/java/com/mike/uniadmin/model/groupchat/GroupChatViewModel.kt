package com.mike.uniadmin.model.groupchat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mike.uniadmin.model.announcements.uniConnectScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupChatViewModel(private val repository: GroupChatRepository) : ViewModel() {
    private val _chats = MutableLiveData<List<GroupChatEntityWithDetails>>()
    val chats: LiveData<List<GroupChatEntityWithDetails>> = _chats

    private val _groups = MutableLiveData<List<GroupEntity>>()
    val groups: LiveData<List<GroupEntity>> = _groups

    private val _group = MutableLiveData<GroupEntity?>()
    val group: MutableLiveData<GroupEntity?> = _group

    fun fetchGroupChats(path: String) {
        repository.fetchGroupChats(path)
    }

    private val userChatsObserver = Observer<List<GroupChatEntityWithDetails>> { groupChats ->
        _chats.value = groupChats
    }

    fun fetchGroupChats() {
        uniConnectScope.launch(Dispatchers.Main) { // Use a coroutine scope
            val latestChats = withContext(Dispatchers.IO) { // Fetch on a background thread
                repository.fetchGroupChatWithDetails()
            }
            latestChats.observeForever(userChatsObserver) // Observe the LiveData
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.fetchGroupChatWithDetails().removeObserver(userChatsObserver) // Remove the observer
    }

    fun fetchGroups() {
        repository.fetchGroups { groups ->
            _groups.postValue(groups)
        }
    }

    fun fetchGroupById(groupId: String) {
        repository.fetchGroupByID(groupId) { group ->
            _group.postValue(group)
        }
    }

    fun deleteGroup(groupId: String) {
        repository.deleteGroup(groupId) {
            fetchGroups()
        }
    }

    fun saveGroup(group: GroupEntity, onSuccess: (Boolean) -> Unit) {
            repository.saveGroup(group, onComplete = { success ->
                if (success) {
                    onSuccess(true)
                    fetchGroups()
                }
            })

    }

    fun saveGroupChat(chat: GroupChatEntity, path: String, onSuccess: (Boolean) -> Unit) {
            repository.saveGroupChat(chat, path, onComplete = { success ->
                if (success) {
                    onSuccess(true)
                    fetchGroupChats(path)
                } else {
                    onSuccess(false)
                    Log.e("Chats", "Could not save chat")
                }
            })

    }

    fun deleteGroupChat(chatId: String, path: String) {
            repository.deleteChat(chatId,
                onSuccess = {
                    fetchGroupChats(path) // Refresh the chat list after deleting
                },
                onFailure = {
                    // Handle delete failure if needed
                    Log.e("Chats", "Could not delete chat", it)
                }
            )

    }

    class GroupChatViewModelFactory(private val repository: GroupChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupChatViewModel::class.java)) {
                return GroupChatViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class for Chats")
        }
    }
}
