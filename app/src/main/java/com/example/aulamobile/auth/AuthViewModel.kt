package com.example.aulamobile.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserProfileData(
    val name: String = "",
    val photoUrl: String = "",
    val bio: String = ""
)

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser,
    val profile: UserProfileData = UserProfileData(),
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val currentUser = firebaseAuth.currentUser
            _state.value = _state.value.copy(user = currentUser)
            if (currentUser != null) {
                observeProfile(currentUser.uid)
            } else {
                _state.value = _state.value.copy(profile = UserProfileData())
            }
        }
        val currentUser = auth.currentUser
        if (currentUser != null) {
            observeProfile(currentUser.uid)
        }
    }

    private fun observeProfile(uid: String) {
        db.collection("users").document(uid).collection("profile").document("info")
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null && snap.exists()) {
                    val p = snap.toObject(UserProfileData::class.java)
                    if (p != null) {
                        _state.value = _state.value.copy(profile = p)
                    }
                } else if (auth.currentUser != null) {
                    val u = auth.currentUser!!
                    val fallback = UserProfileData(
                        name = u.displayName ?: "",
                        photoUrl = u.photoUrl?.toString() ?: "",
                        bio = ""
                    )
                    _state.value = _state.value.copy(profile = fallback)
                }
            }
    }

    fun signIn(email: String, password: String) {
        setLoading()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    ok()
                } else {
                    fail(task.exception)
                }
            }
    }

    fun signUp(username: String = "", email: String, password: String) {
        setLoading()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        if (username.isNotBlank()) {
                            val profileUpdates = userProfileChangeRequest {
                                displayName = username
                            }
                            user.updateProfile(profileUpdates)
                        }
                        saveProfileToFirestore(
                            uid = user.uid,
                            data = UserProfileData(name = username, photoUrl = "", bio = "")
                        )
                    }
                    ok()
                } else {
                    fail(task.exception)
                }
            }
    }

    fun updateProfile(name: String, photoUrl: String, bio: String) {
        val user = auth.currentUser ?: return
        setLoading()

        val profileUpdates = userProfileChangeRequest {
            displayName = name
            if (photoUrl.isNotBlank()) {
                photoUri = Uri.parse(photoUrl)
            }
        }

        user.updateProfile(profileUpdates).addOnCompleteListener {
            val updatedData = UserProfileData(name = name, photoUrl = photoUrl, bio = bio)
            saveProfileToFirestore(user.uid, updatedData)
            _state.value = _state.value.copy(
                isLoading = false,
                user = auth.currentUser,
                profile = updatedData
            )
        }
    }

    private fun saveProfileToFirestore(uid: String, data: UserProfileData) {
        db.collection("users").document(uid).collection("profile").document("info")
            .set(data)
    }

    fun signOut() {
        auth.signOut()
        _state.value = AuthUiState(user = null, profile = UserProfileData())
    }

    private fun setLoading() {
        _state.value = _state.value.copy(isLoading = true, error = null)
    }

    private fun ok() {
        val user = auth.currentUser
        _state.value = _state.value.copy(isLoading = false, user = user)
        if (user != null) {
            observeProfile(user.uid)
        }
    }

    private fun fail(ex: Exception?) {
        _state.value = _state.value.copy(isLoading = false, user = null, error = mapError(ex))
    }

    private fun mapError(ex: Exception?): String {
        val e = ex ?: return "Falha na autenticação. Por Favor, tente novamente."
        return when (e) {
            is FirebaseAuthUserCollisionException -> "E-mail já cadastrado."
            is FirebaseAuthInvalidCredentialsException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                "ERROR_WRONG_PASSWORD" -> "Senha incorreta."
                else -> "Falha na autenticação. Por Favor, tente novamente."
            }

            is FirebaseAuthInvalidUserException -> when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "Usuário não encontrado."
                "ERROR_USER_DISABLED" -> "Usuário desativado."
                else -> "Falha na autenticação. Por Favor, tente novamente."
            }

            is FirebaseNetworkException -> "Sem conexão com a internet."
            else -> {
                val code = (e as? FirebaseAuthException)?.errorCode
                when (code) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "E-mail já cadastrado."
                    "ERROR_WEAK_PASSWORD" -> "Senha fraca."
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Operação não permitida."
                    "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas. Tente novamente mais tarde"
                    else -> e.localizedMessage?.substringBefore('\n')
                        ?: "Falha na autenticação. Por Favor, tente novamente."
                }
            }
        }
    }
}
