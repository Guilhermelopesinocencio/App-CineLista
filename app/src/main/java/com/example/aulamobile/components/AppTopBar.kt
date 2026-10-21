package com.example.aulamobile.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Barra superior (TopAppBar) do aplicativo.
 * Mostra o título "CineLista" à esquerda e, à direita, um botão com ícone de pessoa
 * que leva o usuário ao perfil.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        // Título exibido na barra
        title = { Text("CineLista") },
        // Área de ações à direita: botão de perfil
        actions = {
            IconButton(onClick = onProfileClick) {
                // contentDescription serve para leitores de tela (acessibilidade)
                Icon(Icons.Outlined.Person, contentDescription = "Perfil")
            }
        },
        modifier = modifier
    )
}