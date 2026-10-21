package com.example.aulamobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.aulamobile.auth.AuthViewModel
import com.example.aulamobile.components.AuthDialogs
import com.example.aulamobile.components.AuthModalMode
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineBorder
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary

/**
 * Tela de entrada exibida enquanto não há usuário autenticado (usada pelo AppNav como "portão").
 * Mostra uma mensagem de boas-vindas com os botões "Entrar" e "Cadastrar"; ao tocar em um deles,
 * abre o dialog de autenticação (AuthDialogs) no modo correspondente.
 */
@Composable
fun LoginGateScreen(authViewModel: AuthViewModel) {
    // Estado de autenticação observado em tempo real
    val authState by authViewModel.state.collectAsState()
    // Controla qual modal está aberto: NONE (nenhum), SIGN_IN ou SIGN_UP
    var authMode by remember { mutableStateOf(AuthModalMode.NONE) }

    // Fecha o modal automaticamente quando o login/cadastro termina com sucesso
    // (há usuário e não está mais carregando). Roda de novo se user ou isLoading mudarem.
    LaunchedEffect(authState.user, authState.isLoading) {
        if (authState.user != null && !authState.isLoading) {
            authMode = AuthModalMode.NONE
        }
    }

    // Dialog de login/cadastro; só aparece quando authMode != NONE.
    // Conecta os eventos do formulário às ações do ViewModel.
    AuthDialogs(
        mode = authMode,
        isLoading = authState.isLoading,
        errorMessage = authState.error,
        // Fechar o dialog volta o modo para NONE
        onDismiss = { authMode = AuthModalMode.NONE },
        onSignIn = { email, pass -> authViewModel.signIn(email, pass) },
        onSignUp = { name, email, pass -> authViewModel.signUp(name, email, pass) },
        // Alterna entre os formulários de login e cadastro
        onSwitchMode = { authMode = it }
    )

    // Conteúdo da tela, centralizado na vertical e na horizontal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CineBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Círculo com borda e um "?" no centro, funcionando como avatar genérico (usuário não identificado)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, CineBorder, CircleShape)
                .background(CineTextSecondary.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Text("?", style = MaterialTheme.typography.displaySmall, color = CineTextPrimary)
        }
        Spacer(Modifier.height(16.dp))
        // Título de boas-vindas
        Text(
            "Bem-vindo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CineTextPrimary
        )
        // Texto explicativo abaixo do título
        Text(
            "Entre ou cadastre-se para continuar",
            color = CineTextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        // Linha com os dois botões lado a lado, cada um ocupando metade da largura
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botão principal (dourado): abre o dialog no modo login
            Button(
                onClick = { authMode = AuthModalMode.SIGN_IN },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CineGold,
                    contentColor = CineBackground
                )
            ) { Text("Entrar", fontWeight = FontWeight.Bold) }
            // Botão secundário (apenas contorno dourado): abre o dialog no modo cadastro
            OutlinedButton(
                onClick = { authMode = AuthModalMode.SIGN_UP },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CineGold),
                border = androidx.compose.foundation.BorderStroke(1.dp, CineGold)
            ) { Text("Cadastrar", fontWeight = FontWeight.Bold) }
        }
    }
}