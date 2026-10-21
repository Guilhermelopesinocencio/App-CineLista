package com.example.aulamobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aulamobile.auth.AuthViewModel
import com.example.aulamobile.screens.DetailScreen
import com.example.aulamobile.screens.FilmesScreen
import com.example.aulamobile.screens.HomeScreen
import com.example.aulamobile.screens.LoginGateScreen
import com.example.aulamobile.screens.MovieViewModel
import com.example.aulamobile.screens.ProfileScreen
import com.example.aulamobile.screens.ReviewScreen
import com.example.aulamobile.screens.SeriesScreen
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineTextSecondary

/**
 * Composable de navegação principal da aplicação.
 * Controla o estado de autenticação (exibindo a tela de bloqueio se não houver usuário)
 * e configura o [NavHost] com a barra de navegação inferior (`NavigationBar`).
 */
@Composable
fun AppNav(authViewModel: AuthViewModel = viewModel()) {
    // Estado de autenticação observado pela UI; recompõe quando o login/logout mudar
    val authState by authViewModel.state.collectAsState()

    // Bloqueia o acesso se o usuário não estiver autenticado no Firebase
    if (authState.user == null) {
        LoginGateScreen(authViewModel)
        return
    }

    // Controlador de navegação entre as telas
    val nav = rememberNavController()
    // ViewModel de filmes/séries compartilhado por todas as telas do NavHost
    val movieVm: MovieViewModel = viewModel()
    // Rota atual (null se ainda não houver destino); usada para saber qual aba está ativa
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route

    // Abas de navegação da barra inferior: (rota, rótulo, ícone)
    val tabs = listOf(
        Triple(Screen.Home.route, "Início", Icons.Default.Home),
        Triple(Screen.MyList.route, "Filmes", Icons.Default.Movie),
        Triple(Screen.Series.route, "Séries", Icons.Default.Tv),
        Triple(Screen.Profile.route, "Perfil", Icons.Default.Person)
    )

    // Definição do argumento "id" (inteiro) usado pelas rotas de detalhes e avaliação
    val idArgs = listOf(navArgument("id") { type = NavType.IntType })

    Scaffold(
        containerColor = CineBackground,
        bottomBar = {
            // Exibe a barra de navegação apenas se a rota atual for uma das abas principais
            // (nas telas de detalhes e avaliação a barra fica escondida)
            if (currentRoute in tabs.map { it.first }) {
                NavigationBar(containerColor = CineBackground) {
                    // Cria um item da barra para cada aba
                    tabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                nav.navigate(route) {
                                    // Volta até a Home ao trocar de aba, salvando o estado da aba atual
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    // Evita empilhar várias cópias da mesma tela
                                    launchSingleTop = true
                                    // Restaura o estado salvo da aba de destino (ex.: posição da rolagem)
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            // Cores: dourado para o item selecionado e cinza para os demais
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CineGold,
                                selectedTextColor = CineGold,
                                unselectedIconColor = CineTextSecondary,
                                unselectedTextColor = CineTextSecondary,
                                indicatorColor = CineGold.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        // Contêiner das telas; o padding evita que o conteúdo fique sob a barra inferior
        NavHost(
            navController = nav,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            // Rota: Tela Inicial
            // Ao clicar em um filme ou série, navega para a respectiva tela de detalhes
            composable(Screen.Home.route) {
                HomeScreen(
                    vm = movieVm,
                    onMovie = { id -> nav.navigate(Screen.MovieDetail.go(id)) },
                    onSeries = { id -> nav.navigate(Screen.TvDetail.go(id)) }
                )
            }

            // Rota: Detalhes do Filme
            // Lê o "id" recebido na rota e abre o DetailScreen em modo filme (isSeries = false)
            composable(Screen.MovieDetail.route, arguments = idArgs) { backStack ->
                val id = backStack.arguments!!.getInt("id")
                DetailScreen(
                    vm = movieVm,
                    id = id,
                    isSeries = false,
                    onBack = { nav.popBackStack() },
                    onReview = { nav.navigate(Screen.Review.go(id)) }
                )
            }

            // Rota: Detalhes da Série
            // Igual à anterior, mas abre o DetailScreen em modo série (isSeries = true)
            composable(Screen.TvDetail.route, arguments = idArgs) { backStack ->
                val id = backStack.arguments!!.getInt("id")
                DetailScreen(
                    vm = movieVm,
                    id = id,
                    isSeries = true,
                    onBack = { nav.popBackStack() },
                    onReview = { nav.navigate(Screen.Review.go(id)) }
                )
            }

            // Rota: Lista Pessoal de Filmes
            // Ao clicar em um item, abre os detalhes do filme
            composable(Screen.MyList.route) {
                FilmesScreen(movieVm) { id -> nav.navigate(Screen.MovieDetail.go(id)) }
            }

            // Rota: Lista Pessoal de Séries
            // Ao clicar em um item, abre os detalhes da série
            composable(Screen.Series.route) {
                SeriesScreen(movieVm) { id -> nav.navigate(Screen.TvDetail.go(id)) }
            }

            // Rota: Avaliar e Editar Status
            // Recebe o "id" do item e, ao concluir, volta para a tela anterior
            composable(Screen.Review.route, arguments = idArgs) { backStack ->
                val id = backStack.arguments!!.getInt("id")
                ReviewScreen(movieVm, id) { nav.popBackStack() }
            }

            // Rota: Perfil do Usuário
            // Ao sair da conta, chama signOut(); com isso o estado fica sem usuário
            // e o AppNav volta a exibir a LoginGateScreen automaticamente
            composable(Screen.Profile.route) {
                ProfileScreen(
                    vm = movieVm,
                    authViewModel = authViewModel,
                    onLogout = { authViewModel.signOut() }
                )
            }
        }
    }
}