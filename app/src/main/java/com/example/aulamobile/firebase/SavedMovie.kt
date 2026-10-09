package com.example.aulamobile.firebase

data class SavedMovie(
    val id: Int = 0,
    val title: String = "",
    val posterPath: String? = null,
    val overview: String? = null,
    val status: String = "VAI_ASSISTIR",
    val favorite: Boolean = false,
    val rating: Float = 0f,
    val comment: String = "",
    val runtime: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
