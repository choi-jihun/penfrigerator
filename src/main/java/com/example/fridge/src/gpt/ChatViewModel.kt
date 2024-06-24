package com.example.fridge.src.gpt

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fridge.config.ApplicationClass
import com.example.fridge.util.Constants
import com.example.fridge.chatGpt.dto.ChatContent
import kotlinx.coroutines.launch

private const val TAG = "ChatViewModel_싸피"

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ChatContent>>()
    val messages: LiveData<List<ChatContent>> get() = _messages

    private val chatService = ChatService()

    fun loadUserMessages(uid: String) {
        viewModelScope.launch {
            try {
                val messageList = chatService.getUserMessages(uid)
                _messages.postValue(messageList)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading messages", e)
            }
        }
    }

    fun saveUserMessage(message: ChatContent) {
        val userUid = ApplicationClass.sharedPreferences.getString(Constants.USER_UID)
        userUid?.let { uid ->
            viewModelScope.launch {
                try {
                    chatService.saveUserMessage(uid, message)
                    val updatedList = _messages.value.orEmpty().toMutableList()
                    updatedList.add(message)
                    _messages.postValue(updatedList)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving message", e)
                }
            }
        }
    }
}
