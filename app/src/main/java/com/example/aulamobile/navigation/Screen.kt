package com.example.aulamobile.navigation

/**
 * Mapeamento selado de todas as rotas e destinos de navegação do aplicativo CineLista.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object SignIn : Screen("signin")

    /** Tela Inicial (Início - Carrosséis de Filmes e Séries). */
    data object Home : Screen("home")

    /** Tela de Busca. */
    data object Search : Screen("search")

    /** Tela de Detalhes de um Filme especifico. */
    data object MovieDetail : Screen("movie_detail/{id}") {
        fun go(id: Int) = "movie_detail/$id"
    }

    /** Tela de Detalhes de uma Série especifica. */
    data object TvDetail : Screen("tv_detail/{id}") {
        fun go(id: Int) = "tv_detail/$id"
    }

    /** Tela com a Lista Pessoal de Filmes do usuário. */
    data object MyList : Screen("mylist")

    /** Tela com a Lista Pessoal de Séries do usuário. */
    data object Series : Screen("series")

    /** Tela de Avaliação e Troca de Status. */
    data object Review : Screen("review/{id}") {
        fun go(id: Int) = "review/$id"
    }

    /** Tela de Perfil e Estatísticas do usuário. */
    data object Profile : Screen("profile")
}
