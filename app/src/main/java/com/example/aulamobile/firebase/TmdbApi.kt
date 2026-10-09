package com.example.aulamobile.firebase

import com.example.aulamobile.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/popular")
    suspend fun popular(
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): MovieResponse

    @GET("search/movie")
    suspend fun search(
        @Query("query") query: String,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): MovieResponse

    @GET("discover/movie")
    suspend fun discoverByGenre(
        @Query("with_genres") genreId: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR",
        @Query("sort_by") sort: String = "popularity.desc"
    ): MovieResponse

    @GET("movie/{id}")
    suspend fun detail(
        @Path("id") id: Int,
        @Query("api_key") key: String = BuildConfig.TMDB_KEY,
        @Query("language") lang: String = "pt-BR"
    ): MovieDto

    companion object {
        fun create(): TmdbApi = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }
}
