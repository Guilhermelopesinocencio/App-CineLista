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

@Composable
fun LoginGateScreen(authViewModel: AuthViewModel) {
    val authState by authViewModel.state.collectAsState()
    var authMode by remember { mutableStateOf(AuthModalMode.NONE) }

    LaunchedEffect(authState.user, authState.isLoading) {
        if (authState.user != null && !authState.isLoading) {
            authMode = AuthModalMode.NONE
        }
    }

    AuthDialogs(
        mode = authMode,
        isLoading = authState.isLoading,
        errorMessage = authState.error,
        onDismiss = { authMode = AuthModalMode.NONE },
        onSignIn = { email, pass -> authViewModel.signIn(email, pass) },
        onSignUp = { name, email, pass -> authViewModel.signUp(name, email, pass) },
        onSwitchMode = { authMode = it }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CineBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
        Text(
            "Bem-vindo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = CineTextPrimary
        )
        Text(
            "Entre ou cadastre-se para continuar",
            color = CineTextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { authMode = AuthModalMode.SIGN_IN },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CineGold,
                    contentColor = CineBackground
                )
            ) { Text("Entrar", fontWeight = FontWeight.Bold) }
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
