package com.example.aulamobile.firebase

data class GenreDto(
    val id: Int = 0,
    val name: String = ""
)

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

data class MovieResponse(
    val results: List<MovieDto> = emptyList()
)
