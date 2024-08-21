package com.mike.uniadmin

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mike.uniadmin.backEnd.groupchat.ChatViewModel
import com.mike.uniadmin.backEnd.userchat.MessageViewModel
import com.mike.uniadmin.backEnd.users.UserViewModel
import com.mike.uniadmin.backEnd.users.UserViewModelFactory
import com.mike.uniadmin.localDatabase.UniAdmin


//messageViewModel
@Composable
fun getMessageViewModel(context: Context): MessageViewModel {
    val application = context.applicationContext as UniAdmin
    val messageRepository = application.messageRepository
    return viewModel(factory = MessageViewModel.MessageViewModelFactory(messageRepository))
}

//userViewModel
@Composable
fun getUserViewModel(context: Context): UserViewModel {
    val application = context.applicationContext as UniAdmin
    val userRepository = application.userRepository
    return viewModel(factory = UserViewModelFactory(userRepository))
}

//groupViewModel
@Composable
fun getChatViewModel(context: Context): ChatViewModel {
    val application = context.applicationContext as UniAdmin
    val chatRepository = application.chatRepository
    return viewModel(factory = ChatViewModel.ChatViewModelFactory(chatRepository))

}

