package com.example.fridge.chatGpt.api

import com.example.fridge.chatGpt.dto.ChatRequest
import com.example.fridge.chatGpt.dto.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    fun getChatResponse(@Body request: ChatRequest): Call<ChatResponse>
}