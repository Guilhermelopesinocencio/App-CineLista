package com.example.aulamobile.firebase

import com.example.aulamobile.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit responsável por definir todas as chamadas HTTP à API do TMDB (The Movie Database).
 */
interface TmdbApi {

    // === ENDPOINTS DE FILMES ===

    /** Busca filmes populares no TMDB em português. */
    @GET("movie/popular")
    suspend fun popular(
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): MovieResponse

    /** Pesquisa filmes pelo título informado. */
    @GET("search/movie")
    suspend fun search(
        @Query("query") query: String,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): MovieResponse

    /** Descobre filmes filtrados pelo ID do gênero. */
    @GET("discover/movie")
    suspend fun discoverByGenre(
        @Query("with_genres") genreId: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR",
        @Query("sort_by") sort: String = "popularity.desc"
    ): MovieResponse

    /** Obtém os detalhes completos de um filme específico pelo seu ID. */
    @GET("movie/{id}")
    suspend fun detail(
        @Path("id") id: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): MovieDto

    // === ENDPOINTS DE SÉRIES DE TV ===

    /** Busca séries de TV populares no TMDB em português. */
    @GET("tv/popular")
    suspend fun popularTv(
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): TvResponse

    /** Pesquisa séries de TV pelo título informado. */
    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): TvResponse

    /** Descobre séries de TV filtradas pelo ID do gênero. */
    @GET("discover/tv")
    suspend fun discoverTvByGenre(
        @Query("with_genres") genreId: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR",
        @Query("sort_by") sort: String = "popularity.desc"
    ): TvResponse

    /** Obtém os detalhes completos de uma série de TV específica pelo seu ID. */
    @GET("tv/{id}")
    suspend fun tvDetail(
        @Path("id") id: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): TvShowDto

    companion object {
        /** Cria e configura a instância do cliente API Retrofit. */
        fun create(): TmdbApi = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }
}
