package com.example.aulamobile.firebase

import com.google.firebase.firestore.PropertyName

/**
 * Modelo de dados representando um filme ou série salvo na coleção do Firebase Firestore do usuário.
 * É o objeto gravado/lido em users/{uid}/movies pelo FirebaseRepository.
 * Todos os campos têm valor padrão, o que permite ao Firestore criar o objeto
 * automaticamente a partir do documento (toObjects).
 */
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
    @get:PropertyName("isSeries") @set:PropertyName("isSeries") @field:PropertyName("isSeries")
    var isSeries: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)