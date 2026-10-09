package com.example.aulamobile.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun authState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSaved(): Flow<List<SavedMovie>> = authState().flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList())
        else callbackFlow {
            val reg = db.collection("users").document(uid).collection("movies")
                .addSnapshotListener { snap, err ->
                    if (err != null) { close(err); return@addSnapshotListener }
                    trySend(snap?.toObjects(SavedMovie::class.java).orEmpty())
                }
            awaitClose { reg.remove() }
        }
    }

    private fun col() = db.collection("users")
        .document(auth.currentUser!!.uid).collection("movies")

    suspend fun save(m: SavedMovie) {
        if (auth.currentUser != null) {
            col().document(m.id.toString()).set(m).await()
        }
    }

    suspend fun remove(id: Int) {
        if (auth.currentUser != null) {
            col().document(id.toString()).delete().await()
        }
    }
}
