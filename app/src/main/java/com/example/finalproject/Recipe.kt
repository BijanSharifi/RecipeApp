package com.example.finalproject

data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val difficulty: Int = 0,
    val imageData: String="",
    val rating: Float= 0f,
    val ratingCount: Int = 0,
    val ingredients: String = ""
)