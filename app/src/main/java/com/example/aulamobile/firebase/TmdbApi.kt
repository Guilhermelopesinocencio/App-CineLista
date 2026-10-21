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
    /**
     * Busca filmes populares no TMDB em português.
     * Chama GET movie/popular e retorna a lista em um MovieResponse.
     */
    @GET("movie/popular")
    suspend fun popular(
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): MovieResponse

    /**
     * Pesquisa filmes pelo título informado.
     * Chama GET search/movie enviando o texto digitado no parâmetro "query".
     */
    @GET("search/movie")
    suspend fun search(
        @Query("query") query: String,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): MovieResponse

    /**
     * Descobre filmes filtrados pelo ID do gênero.
     * Chama GET discover/movie, ordenando por padrão do mais popular para o menos popular.
     */
    @GET("discover/movie")
    suspend fun discoverByGenre(
        @Query("with_genres") genreId: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR",
        @Query("sort_by") sort: String = "popularity.desc"
    ): MovieResponse

    /**
     * Obtém os detalhes completos de um filme específico pelo seu ID.
     * Chama GET movie/{id}; o valor de "id" é inserido no caminho da URL.
     * Retorna um único MovieDto (aqui vêm campos como runtime e genres).
     * - id: identificador do filme no TMDB
     */
    @GET("movie/{id}")
    suspend fun detail(
        @Path("id") id: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): MovieDto

    // === ENDPOINTS DE SÉRIES DE TV ===

    /**
     * Busca séries de TV populares no TMDB em português.
     * Chama GET tv/popular e retorna a lista em um TvResponse.
     */
    @GET("tv/popular")
    suspend fun popularTv(
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): TvResponse

    /**
     * Pesquisa séries de TV pelo título informado.
     * Chama GET search/tv enviando o texto digitado no parâmetro "query".
     * - query: termo de busca (ex.: nome da série)
     */
    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): TvResponse

    /**
     * Descobre séries de TV filtradas pelo ID do gênero.
     * Chama GET discover/tv, ordenando por padrão do mais popular para o menos popular.
     * - genreId: ID do gênero no TMDB
     * - sort: critério de ordenação (padrão "popularity.desc")
     */
    @GET("discover/tv")
    suspend fun discoverTvByGenre(
        @Query("with_genres") genreId: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR",
        @Query("sort_by") sort: String = "popularity.desc"
    ): TvResponse

    /**
     * Obtém os detalhes completos de uma série de TV específica pelo seu ID.
     * Chama GET tv/{id}; o valor de "id" é inserido no caminho da URL.
     * Retorna um único TvShowDto (aqui vêm campos como temporadas, episódios e genres).
     * - id: identificador da série no TMDB
     */
    @GET("tv/{id}")
    suspend fun tvDetail(
        @Path("id") id: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): TvShowDto

    companion object {
        /**
         * Cria e configura a instância do cliente API Retrofit.
         * Define a URL base do TMDB (versão 3) e o Gson como conversor, que transforma
         * o JSON das respostas nos objetos MovieResponse, MovieDto, TvResponse etc.
         * Uso: val api = TmdbApi.create()
         */
        fun create(): TmdbApi = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }
}