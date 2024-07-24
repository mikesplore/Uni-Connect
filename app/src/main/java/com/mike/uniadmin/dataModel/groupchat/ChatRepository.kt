package com.mike.uniadmin.dataModel.groupchat

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val viewModelScope = CoroutineScope(Dispatchers.Main)

class ChatRepository(private val chatDao: ChatDao, private val groupDao: GroupDao) {
val database = FirebaseDatabase.getInstance().getReference()

    fun fetchChats(path: String, onResult: (List<ChatEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedChats = chatDao.getChats(path)
            if (cachedChats.isNotEmpty()) {
                onResult(cachedChats)
            } else {
                database.child(path).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val chats = mutableListOf<ChatEntity>()
                        for (childSnapshot in snapshot.children) {
                            val chat = childSnapshot.getValue(ChatEntity::class.java)
                            chat?.let { chats.add(it) }
                        }
                        viewModelScope.launch {
                            chatDao.insertChats(chats)
                            onResult(chats)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the read error (e.g., log the error)
                        println("Error reading chats: ${error.message}")
                    }
                })
            }
        }
    }

    fun saveGroup(group: GroupEntity, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            groupDao.insertGroups(listOf(group))
            database.child("Groups").child(group.id).setValue(group).addOnCompleteListener { success ->
                if (success.isSuccessful) {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
        }
    }

    fun fetchGroups(onResult: (List<GroupEntity>) -> Unit) {
        viewModelScope.launch {
            val cachedGroups = groupDao.getGroups()
            if (cachedGroups.isNotEmpty()) {
                onResult(cachedGroups)
            } else {
                database.child("Groups").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val groups = mutableListOf<GroupEntity>()
                        for (childSnapshot in snapshot.children) {
                            val group = childSnapshot.getValue(GroupEntity::class.java)
                            group?.let { groups.add(it) }
                        }
                        viewModelScope.launch {
                            groupDao.insertGroups(groups)
                            onResult(groups)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the read error (e.g., log the error)
                        println("Error reading groups: ${error.message}")
                    }
                })
            }
        }
    }

    fun fetchGroupByID(groupID: String, onResult: (GroupEntity?) -> Unit) {
        viewModelScope.launch {
            val cachedGroup = groupDao.getGroups().find { it.id == groupID }
            if (cachedGroup != null) {
                onResult(cachedGroup)
            } else {
                database.child("Groups").child(groupID).get().addOnSuccessListener { snapshot ->
                    val group = snapshot.getValue(GroupEntity::class.java)
                    viewModelScope.launch {
                        if (group != null) {
                            groupDao.insertGroups(listOf(group))
                        }
                        onResult(group)
                    }
                }.addOnFailureListener { exception ->
                    println("Error fetching group: ${exception.message}")
                    onResult(null)
                }
            }
        }
    }

    fun saveChat(chat: ChatEntity, path: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            chatDao.insertChats(listOf(chat))
            database.child(path).push().setValue(chat)
                .addOnCompleteListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
    }

    fun deleteChat(chatId: String, onSuccess: () -> Unit, onFailure: (Exception?) -> Unit) {
        viewModelScope.launch {
            chatDao.deleteChat(chatId)
            database.child(chatId).removeValue()
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        }
    }

    fun deleteGroup(groupId: String, oComplete: () -> Unit) {
        viewModelScope.launch {
            groupDao.deleteGroup(groupId)
            database.child("Groups").child(groupId).removeValue()
                .addOnSuccessListener {
                    oComplete()
                }
                .addOnFailureListener { exception ->
                    println("Error deleting group: ${exception.message}")
                }
        }
    }
}
