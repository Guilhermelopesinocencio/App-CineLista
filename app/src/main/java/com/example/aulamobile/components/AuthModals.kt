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

enum class AuthModalMode { NONE, SIGN_IN, SIGN_UP }

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
    if (mode == AuthModalMode.NONE) return

    Dialog(onDismissRequest = onDismiss) {
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (mode == AuthModalMode.SIGN_IN) "Entrar" else "Criar Conta",
                            color = CineTextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (mode == AuthModalMode.SIGN_IN) "Bem-vindo de volta ao CineLista" else "Junte-se à comunidade CineLista",
                            color = CineTextSecondary,
                            fontSize = 12.sp
                        )
                    }

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

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

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

@Composable
private fun SignInForm(
    isLoading: Boolean,
    onSignIn: (String, String) -> Unit,
    onSwitchToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
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

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { onSignIn(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = CineOrange),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = CineBorder)
            Text(" ou ", color = CineTextSecondary, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = CineBorder)
        }

        Spacer(Modifier.height(12.dp))

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

@Composable
private fun SignUpForm(
    isLoading: Boolean,
    onSignUp: (String, String, String) -> Unit,
    onSwitchToSignIn: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
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

        Button(
            onClick = { onSignUp(username, email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = CineOrange),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading && username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && password == confirmPassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Criar Conta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = CineBorder)
            Text(" ou ", color = CineTextSecondary, fontSize = 12.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = CineBorder)
        }

        Spacer(Modifier.height(10.dp))

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

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF141418),
    unfocusedContainerColor = Color(0xFF141418),
    focusedBorderColor = CineOrange,
    unfocusedBorderColor = CineBorder,
    focusedTextColor = CineTextPrimary,
    unfocusedTextColor = CineTextPrimary
)
