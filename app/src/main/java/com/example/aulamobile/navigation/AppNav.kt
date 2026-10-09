package com.example.aulamobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import com.example.aulamobile.screens.HomeScreen
import com.example.aulamobile.screens.LoginGateScreen
import com.example.aulamobile.screens.MovieViewModel
import com.example.aulamobile.screens.MyListScreen
import com.example.aulamobile.screens.NotificationsScreen
import com.example.aulamobile.screens.ProfileScreen
import com.example.aulamobile.screens.ReviewScreen
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineTextSecondary

@Composable
fun AppNav(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.state.collectAsState()
    if (authState.user == null) {
        LoginGateScreen(authViewModel)
        return
    }

    val nav = rememberNavController()
    val movieVm: MovieViewModel = viewModel()
    val current = nav.currentBackStackEntryAsState().value?.destination?.route

    val tabs = listOf(
        Triple(Screen.Home.route, "Início", Icons.Default.Home),
        Triple(Screen.MyList.route, "Filmes", Icons.Default.Movie),
        Triple(Screen.Notifications.route, "Notificações", Icons.Default.Notifications),
        Triple(Screen.Profile.route, "Perfil", Icons.Default.Person)
    )
    val idArg = listOf(navArgument("id") { type = NavType.IntType })

    Scaffold(
        containerColor = CineBackground,
        bottomBar = {
            if (current in tabs.map { it.first }) {
                NavigationBar(containerColor = CineBackground) {
                    tabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = current == route,
                            onClick = {
                                nav.navigate(route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
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
        NavHost(
            navController = nav,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(movieVm) { id -> nav.navigate(Screen.Detail.go(id)) }
            }
            composable(Screen.Detail.route, arguments = idArg) { backStack ->
                val id = backStack.arguments!!.getInt("id")
                DetailScreen(
                    vm = movieVm,
                    id = id,
                    onBack = { nav.popBackStack() },
                    onReview = { nav.navigate(Screen.Review.go(id)) }
                )
            }
            composable(Screen.MyList.route) {
                MyListScreen(movieVm) { id -> nav.navigate(Screen.Detail.go(id)) }
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(movieVm) { id -> nav.navigate(Screen.Detail.go(id)) }
            }
            composable(Screen.Review.route, arguments = idArg) { backStack ->
                val id = backStack.arguments!!.getInt("id")
                ReviewScreen(movieVm, id) { nav.popBackStack() }
            }
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
