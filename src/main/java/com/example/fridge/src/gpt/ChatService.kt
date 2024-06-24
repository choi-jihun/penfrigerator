package com.example.fridge.src.gpt

import android.util.Log
import com.example.fridge.util.Constants
import com.example.fridge.chatGpt.dto.ChatContent
import com.example.fridge.chatGpt.Sender
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

private const val TAG = "ChatService_싸피"

class ChatService {
    private val database = FirebaseDatabase.getInstance()

    private fun getUserMessagesRef(uid: String) =
        database.reference.child(Constants.USERS_COLLECTION).child(uid).child(Constants.USER_CHAT)

    suspend fun getUserMessages(uid: String): List<ChatContent> {
        val snapshot = getUserMessagesRef(uid).get().await()
        val messageList = mutableListOf<ChatContent>()

        snapshot.children.forEach { messageSnapshot ->
            val messageMap = messageSnapshot.value as? Map<*, *>
            if (messageMap != null) {
                try {
                    val message = ChatContent(
                        id = messageSnapshot.key ?: "",
                        sender = Sender.valueOf(messageMap["sender"] as String),
                        content = messageMap["content"] as? String
                    )
                    messageList.add(message)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message item", e)
                }
            } else {
                Log.e(TAG, "messageMap is null for dataSnapshot: ${messageSnapshot.key}")
            }
        }

        return messageList
    }

    suspend fun saveUserMessage(uid: String, message: ChatContent) {
        val messageData = mapOf(
            "id" to message.id,
            "sender" to message.sender.name,
            "content" to message.content
        )
        getUserMessagesRef(uid).push().setValue(messageData).await()
    }
}
