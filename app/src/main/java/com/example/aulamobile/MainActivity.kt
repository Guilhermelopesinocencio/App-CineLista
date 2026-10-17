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
 * Configura o tema escuro de borda a borda (Edge-to-Edge) e inicia a navegação principal.
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Habilita o modo Edge-to-Edge com barras de sistema transparentes
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        // Define o conteúdo da interface do usuário usando Jetpack Compose
        setContent {
            CineListaTheme {
                AppNav()
            }
        }
    }
}
