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

/**
 * Repositório responsável pelo acesso e sincronização de dados no Firebase Firestore
 * para a lista pessoal de filmes e séries salvas do usuário autenticado.
 */
class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    /**
     * Emite um Flow com o UID do usuário atual sempre que o estado da autenticação mudar.
     */
    private fun authState(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    /**
     * Observa em tempo real a coleção de filmes e séries salvos na conta do usuário atual no Firestore.
     * Retorna um [Flow] contendo a lista atualizada de [SavedMovie].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeSaved(): Flow<List<SavedMovie>> = authState().flatMapLatest { uid ->
        if (uid == null) {
            flowOf(emptyList())
        } else {
            callbackFlow {
                val reg = db.collection("users").document(uid).collection("movies")
                    .addSnapshotListener { snap, err ->
                        if (err != null) {
                            close(err)
                            return@addSnapshotListener
                        }
                        trySend(snap?.toObjects(SavedMovie::class.java).orEmpty())
                    }
                awaitClose { reg.remove() }
            }
        }
    }

    /** Referência para a coleção do Firestore do usuário autenticado. */
    private fun userMoviesCollection() = db.collection("users")
        .document(auth.currentUser!!.uid).collection("movies")

    /**
     * Salva ou atualiza um filme/série na coleção pessoal do usuário no Firestore.
     */
    suspend fun save(m: SavedMovie) {
        if (auth.currentUser != null) {
            userMoviesCollection().document(m.id.toString()).set(m).await()
        }
    }

    /**
     * Remove um filme/série da coleção pessoal do usuário no Firestore pelo seu ID.
     */
    suspend fun remove(id: Int) {
        if (auth.currentUser != null) {
            userMoviesCollection().document(id.toString()).delete().await()
        }
    }
}
