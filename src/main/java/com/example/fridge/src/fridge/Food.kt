package com.example.fridge.src.fridge

import java.util.Date

data class Food(
    val id: String = "",
    val proName: String,
    val img: String,
    val date: Date,
    val category: String,
    val expirationDate: Date,
    val remainAmount: Int
)
