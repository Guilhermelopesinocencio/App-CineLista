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

/**
 * Dados do perfil do usuário armazenados no Firestore.
 * Os valores padrão vazios permitem que o Firestore converta o documento
 * em objeto (toObject) mesmo se algum campo estiver ausente.
 */
data class UserProfileData(
    val name: String = "",
    val photoUrl: String = "",
    val bio: String = ""
)

/**
 * Estado único da tela de autenticação/perfil.
 * - isLoading: indica se há uma operação em andamento (para mostrar progresso)
 * - user: usuário logado no Firebase Auth (null = ninguém logado)
 * - profile: dados de perfil vindos do Firestore
 * - error: mensagem de erro amigável para exibir na UI (null = sem erro)
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser,
    val profile: UserProfileData = UserProfileData(),
    val error: String? = null
)

/**
 * ViewModel responsável por login, cadastro, logout e perfil do usuário.
 * Expõe o estado para a UI através de um StateFlow somente leitura.
 */
class AuthViewModel : ViewModel() {
    // Instâncias do Firebase Auth (autenticação) e Firestore (banco de dados)
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Estado mutável interno; a UI só enxerga a versão imutável (state)
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    /**
     * Bloco de inicialização: registra um listener que reage a mudanças de login/logout.
     * - Se há usuário logado: atualiza o estado e passa a observar o perfil dele no Firestore.
     * - Se não há: limpa os dados de perfil.
     * Também já observa o perfil caso o usuário esteja logado desde o início (sessão salva).
     */
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

    /**
     * Escuta em tempo real o documento de perfil do usuário no Firestore
     * (users/{uid}/profile/info). Sempre que o documento mudar, o estado é atualizado.
     * Se o documento não existir ou ocorrer erro, usa como alternativa (fallback) os dados
     * do próprio Firebase Auth (nome e foto), com bio vazia.
     */
    private fun observeProfile(uid: String) {
        db.collection("users").document(uid).collection("profile").document("info")
            .addSnapshotListener { snap, err ->
                if (err == null && snap != null && snap.exists()) {
                    // Documento existe: converte para UserProfileData e atualiza o estado
                    val p = snap.toObject(UserProfileData::class.java)
                    if (p != null) {
                        _state.value = _state.value.copy(profile = p)
                    }
                } else if (auth.currentUser != null) {
                    // Documento ausente ou erro: monta o perfil a partir do Firebase Auth
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

    /**
     * Faz login com e-mail e senha.
     * Ativa o loading, chama o Firebase e, ao terminar, trata sucesso (ok) ou falha (fail).
     */
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

    /**
     * Cria uma nova conta com e-mail e senha.
     * Em caso de sucesso:
     *  1. Se um username foi informado, atualiza o displayName no Firebase Auth;
     *  2. Salva o perfil inicial (nome, foto vazia, bio vazia) no Firestore;
     *  3. Finaliza com ok().
     * Em caso de falha, trata o erro com fail().
     */
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

    /**
     * Atualiza o perfil do usuário logado (nome, foto e bio).
     * Nome e foto são atualizados no Firebase Auth; os três campos são salvos no Firestore.
     * Ao final, desativa o loading e atualiza o estado com os novos dados.
     * Se não houver usuário logado, a função simplesmente retorna sem fazer nada.
     */
    fun updateProfile(name: String, photoUrl: String, bio: String) {
        val user = auth.currentUser ?: return
        setLoading()

        val profileUpdates = userProfileChangeRequest {
            displayName = name
            // Só altera a foto se uma URL foi informada
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

    /**
     * Grava (ou sobrescreve) o documento de perfil do usuário no Firestore,
     * no caminho users/{uid}/profile/info.
     */
    private fun saveProfileToFirestore(uid: String, data: UserProfileData) {
        db.collection("users").document(uid).collection("profile").document("info")
            .set(data)
    }

    /**
     * Encerra a sessão no Firebase Auth e reseta o estado da UI
     * (sem usuário e com perfil vazio).
     */
    fun signOut() {
        auth.signOut()
        _state.value = AuthUiState(user = null, profile = UserProfileData())
    }

    /**
     * Marca o estado como "carregando" e limpa qualquer erro anterior.
     * Chamada no início de operações assíncronas (login, cadastro, atualização de perfil).
     */
    private fun setLoading() {
        _state.value = _state.value.copy(isLoading = true, error = null)
    }

    /**
     * Chamada quando login/cadastro dá certo: desativa o loading,
     * atualiza o usuário no estado e começa a observar o perfil dele.
     */
    private fun ok() {
        val user = auth.currentUser
        _state.value = _state.value.copy(isLoading = false, user = user)
        if (user != null) {
            observeProfile(user.uid)
        }
    }

    /**
     * Chamada quando login/cadastro falha: desativa o loading, zera o usuário
     * e guarda no estado a mensagem de erro já traduzida por mapError().
     */
    private fun fail(ex: Exception?) {
        _state.value = _state.value.copy(isLoading = false, user = null, error = mapError(ex))
    }

    /**
     * Verifica o tipo da exceção (e-mail duplicado, credenciais inválidas, usuário inexistente,
     * sem internet etc.) e, quando necessário, o errorCode específico.
     * Para casos não previstos, usa a mensagem original da exceção ou um texto genérico.
     */
    private fun mapError(ex: Exception?): String {
        val e = ex ?: return "Falha na autenticação. Por Favor, tente novamente."
        return when (e) {
            // E-mail já usado em outra conta
            is FirebaseAuthUserCollisionException -> "E-mail já cadastrado."

            // Credenciais inválidas: diferencia e-mail mal formatado de senha errada
            is FirebaseAuthInvalidCredentialsException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "E-mail inválido."
                "ERROR_WRONG_PASSWORD" -> "Senha incorreta."
                else -> "Falha na autenticação. Por Favor, tente novamente."
            }

            // Problemas com a conta: não existe ou foi desativada
            is FirebaseAuthInvalidUserException -> when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "Usuário não encontrado."
                "ERROR_USER_DISABLED" -> "Usuário desativado."
                else -> "Falha na autenticação. Por Favor, tente novamente."
            }

            // Falha de rede
            is FirebaseNetworkException -> "Sem conexão com a internet."

            // Demais erros: tenta mapear pelo errorCode do FirebaseAuthException
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