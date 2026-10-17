package com.example.aulamobile.firebase

/**
 * Representa um gênero cinematográfico ou de TV retornado pela API do TMDB.
 */
data class GenreDto(
    val id: Int = 0,
    val name: String = ""
)

/**
 * Modelo de dados de um filme retornado pela API do TMDB.
 */
data class MovieDto(
    val id: Int = 0,
    val title: String = "",
    val overview: String? = null,
    val poster_path: String? = null,
    val vote_average: Double? = null,
    val release_date: String? = null,
    val runtime: Int? = null,
    val genres: List<GenreDto>? = null
)

/**
 * Resposta paginada de busca ou lista de filmes da API do TMDB.
 */
data class MovieResponse(
    val results: List<MovieDto> = emptyList()
)

/**
 * Modelo de dados de uma série de TV retornada pela API do TMDB.
 */
data class TvShowDto(
    val id: Int = 0,
    val name: String = "",
    val overview: String? = null,
    val poster_path: String? = null,
    val vote_average: Double? = null,
    val first_air_date: String? = null,
    val episode_run_time: List<Int>? = null,
    val number_of_episodes: Int? = null,
    val number_of_seasons: Int? = null,
    val genres: List<GenreDto>? = null
)

/**
 * Resposta paginada de busca ou lista de séries da API do TMDB.
 */
data class TvResponse(
    val results: List<TvShowDto> = emptyList()
)
