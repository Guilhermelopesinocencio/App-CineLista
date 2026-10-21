package com.example.aulamobile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aulamobile.ui.theme.CineBorder
import com.example.aulamobile.ui.theme.CineCardBg
import com.example.aulamobile.ui.theme.CineOrange
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary

/**
 * Modos possíveis do modal de autenticação:
 * - NONE: modal fechado (nada é exibido)
 * - SIGN_IN: formulário de login
 * - SIGN_UP: formulário de cadastro
 */
enum class AuthModalMode { NONE, SIGN_IN, SIGN_UP }

/**
 * Janela (Dialog) de autenticação que exibe o formulário de login ou de cadastro,
 * conforme o modo recebido. É um componente "sem estado próprio de modo": quem o usa
 * controla qual formulário aparece e reage aos eventos pelos callbacks.
 */
@Composable
fun AuthDialogs(
    mode: AuthModalMode,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit,
    onSwitchMode: (AuthModalMode) -> Unit
) {
    // Se o modo é NONE, não desenha nada e sai da função
    if (mode == AuthModalMode.NONE) return

    Dialog(onDismissRequest = onDismiss) {
        // Cartão arredondado com cor de fundo e borda do tema
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CineCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, CineBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Cabeçalho: título + subtítulo à esquerda e botão de fechar à direita
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Título muda conforme o modo
                        Text(
                            text = if (mode == AuthModalMode.SIGN_IN) "Entrar" else "Criar Conta",
                            color = CineTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        // Subtítulo também muda conforme o modo
                        Text(
                            text = if (mode == AuthModalMode.SIGN_IN) "Bem-vindo de volta ao CineLista" else "Junte-se à comunidade CineLista",
                            color = CineTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Botão "X" que fecha o dialog
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = CineTextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Exibe a mensagem de erro (em vermelho) apenas se existir
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Escolhe qual formulário mostrar conforme o modo atual
                if (mode == AuthModalMode.SIGN_IN) {
                    SignInForm(
                        isLoading = isLoading,
                        onSignIn = onSignIn,
                        onSwitchToSignUp = { onSwitchMode(AuthModalMode.SIGN_UP) }
                    )
                } else {
                    SignUpForm(
                        isLoading = isLoading,
                        onSignUp = onSignUp,
                        onSwitchToSignIn = { onSwitchMode(AuthModalMode.SIGN_IN) }
                    )
                }
            }
        }
    }
}

/**
 * Formulário de login (e-mail e senha).
 * Guarda localmente o que o usuário digita e se a senha está visível.
 */
@Composable
private fun SignInForm(
    isLoading: Boolean,
    onSignIn: (String, String) -> Unit,
    onSwitchToSignUp: () -> Unit
) {
    // Estados locais dos campos; "remember" mantém os valores entre recomposições
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Campo de e-mail (teclado próprio para e-mail)
        Text("E-MAIL", color = CineTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
            placeholder = { Text("seu@email.com", color = CineTextSecondary) },
            leadingIcon = { Icon(Icons.Outlined.Email, null, tint = CineTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )

        Spacer(Modifier.height(14.dp))

        // Campo de senha com botão (olho) para mostrar/ocultar o texto
        Text("SENHA", color = CineTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            placeholder = { Text("••••••••", color = CineTextSecondary) },
            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = CineTextSecondary) },
            trailingIcon = {
                // Alterna a visibilidade da senha e troca o ícone correspondente
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = CineTextSecondary
                    )
                }
            },
            // Mostra o texto puro ou mascarado com bolinhas, conforme showPassword
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )

        Spacer(Modifier.height(20.dp))

        // Botão "Entrar": só fica habilitado se não está carregando e ambos os campos têm conteúdo
        Button(
            onClick = { onSignIn(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = CineOrange),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            // Durante o carregamento mostra um spinner no lugar do texto
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Divisor visual "──── ou ────"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = CineBorder)
            Text(" ou ", color = CineTextSecondary, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = CineBorder)
        }

        Spacer(Modifier.height(12.dp))

        // Link de texto para alternar para o formulário de cadastro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Não tem uma conta? ", color = CineTextSecondary, fontSize = 13.sp)
            Text(
                text = "Cadastrar-se",
                color = CineOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSwitchToSignUp() }
            )
        }
    }
}

/**
 * Formulário de cadastro (nome de usuário, e-mail, senha e confirmação de senha).
 * Cada campo de senha tem seu próprio controle de visibilidade.
 */
@Composable
private fun SignUpForm(
    isLoading: Boolean,
    onSignUp: (String, String, String) -> Unit,
    onSwitchToSignIn: () -> Unit
) {
    // Estados locais dos campos de texto
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Estados de visibilidade independentes para cada campo de senha
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Campo de nome de usuário
        Text("NOME DE USUÁRIO", color = CineTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            singleLine = true,
            placeholder = { Text("SeuNome", color = CineTextSecondary) },
            leadingIcon = { Icon(Icons.Outlined.Person, null, tint = CineTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )

        Spacer(Modifier.height(10.dp))

        // Campo de e-mail
        Text("E-MAIL", color = CineTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
            placeholder = { Text("seu@email.com", color = CineTextSecondary) },
            leadingIcon = { Icon(Icons.Outlined.Email, null, tint = CineTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )

        Spacer(Modifier.height(10.dp))

        // Campo de senha com botão de mostrar/ocultar
        Text("SENHA", color = CineTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            placeholder = { Text("••••••••", color = CineTextSecondary) },
            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = CineTextSecondary) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = CineTextSecondary
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )

        Spacer(Modifier.height(10.dp))

        // Campo de confirmação de senha (com seu próprio botão de mostrar/ocultar)
        Text("CONFIRMAR SENHA", color = CineTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            singleLine = true,
            placeholder = { Text("••••••••", color = CineTextSecondary) },
            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = CineTextSecondary) },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = CineTextSecondary
                    )
                }
            },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors()
        )

        Spacer(Modifier.height(18.dp))

        // Botão "Criar Conta": habilitado apenas se não está carregando, todos os campos
        // estão preenchidos e a senha é igual à confirmação
        Button(
            onClick = { onSignUp(username, email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = CineOrange),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && password == confirmPassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            // Spinner durante o carregamento; texto normal caso contrário
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Criar Conta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        // Divisor visual "──── ou ────"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = CineBorder)
            Text(" ou ", color = CineTextSecondary, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = CineBorder)
        }

        Spacer(Modifier.height(10.dp))

        // Link de texto para voltar ao formulário de login
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Já tem uma conta? ", color = CineTextSecondary, fontSize = 13.sp)
            Text(
                text = "Entrar",
                color = CineOrange,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSwitchToSignIn() }
            )
        }
    }
}

/**
 * Define as cores padrão dos campos de texto (OutlinedTextField) usados nos formulários:
 * fundo escuro, borda laranja quando focado, borda padrão quando não focado e texto claro.
 * Centraliza o estilo para não repetir a mesma configuração em cada campo.
 */
@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF141418),
    unfocusedContainerColor = Color(0xFF141418),
    focusedBorderColor = CineOrange,
    unfocusedBorderColor = CineBorder,
    focusedTextColor = CineTextPrimary,
    unfocusedTextColor = CineTextPrimary
)