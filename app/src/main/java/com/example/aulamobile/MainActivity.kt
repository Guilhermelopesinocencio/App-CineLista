package com.example.aulamobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.aulamobile.navigation.AppNav
import com.example.aulamobile.ui.theme.CineListaTheme

/**
 * Ponto de entrada principal do aplicativo CineLista.
 *
 * É a primeira tela que o Android abre (declarada no AndroidManifest como activity principal).
 * Herda de ComponentActivity, a base necessária para usar o Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    /**
     * Chamada pelo Android quando a Activity é criada. É aqui que a interface é montada.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilita o modo Edge-to-Edge com barras de sistema transparentes
        // (o app desenha por baixo da barra de status e da barra de navegação do Android;
        // SystemBarStyle.dark deixa os ícones das barras claros, adequados ao fundo escuro)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        // Define o conteúdo da interface do usuário usando Jetpack Compose
        // O CineListaTheme aplica cores e tipografia a tudo que está dentro dele,
        // e o AppNav é a raiz da navegação (login, abas e demais telas)
        setContent {
            CineListaTheme {
                AppNav()
            }
        }
    }
}