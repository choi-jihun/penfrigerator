package com.example.fridge.chatGpt.dto


data class ChatRequest(
    val model: String = "gpt-4o",
    val messages: List<Message>,
    val max_tokens: Int = 2000, // 변경할 수 있는 토큰 수
    val temperature: Double = 0.7
)