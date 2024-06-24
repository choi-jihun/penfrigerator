package com.example.fridge.chatGpt.dto

import com.example.fridge.chatGpt.Sender

data class ChatContent(
    val id: String = System.currentTimeMillis().toString(),
    val sender: Sender,
    val content: String?
)
