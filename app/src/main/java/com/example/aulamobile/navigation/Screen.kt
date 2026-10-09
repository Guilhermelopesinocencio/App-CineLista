package com.example.aulamobile.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object SignIn : Screen("signin")

    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Detail : Screen("detail/{id}") { fun go(id: Int) = "detail/$id" }
    data object MyList : Screen("mylist")
    data object Notifications : Screen("notifications")
    data object Review : Screen("review/{id}") { fun go(id: Int) = "review/$id" }
    data object Profile : Screen("profile")
}
