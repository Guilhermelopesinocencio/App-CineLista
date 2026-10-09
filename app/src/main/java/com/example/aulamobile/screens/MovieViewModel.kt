package com.example.aulamobile.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aulamobile.firebase.FirebaseRepository
import com.example.aulamobile.firebase.MovieDto
import com.example.aulamobile.firebase.SavedMovie
import com.example.aulamobile.firebase.TmdbApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GenreSection(val title: String, val movies: List<MovieDto>)

class MovieViewModel : ViewModel() {
    private val api by lazy { TmdbApi.create() }
    private val repo = FirebaseRepository()

    var popular by mutableStateOf<List<MovieDto>>(emptyList()); private set
    var genreSections by mutableStateOf<List<GenreSection>>(emptyList()); private set
    var results by mutableStateOf<List<MovieDto>>(emptyList()); private set
    var selected by mutableStateOf<MovieDto?>(null); private set
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    val myList: StateFlow<List<SavedMovie>> = repo.observeSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadPopular()
        loadGenreSections()
    }

    fun loadPopular() = launchSafe { popular = api.popular().results }

    fun loadGenreSections() = launchSafe {
        genreSections = listOf(
            GenreSection("Ação", api.discoverByGenre(28).results.take(12)),
            GenreSection("Aventura", api.discoverByGenre(12).results.take(12)),
            GenreSection("Drama", api.discoverByGenre(18).results.take(12)),
            GenreSection("Comédia", api.discoverByGenre(35).results.take(12)),
            GenreSection("Fantasia", api.discoverByGenre(14).results.take(12)),
            GenreSection("Ficção científica", api.discoverByGenre(878).results.take(12)),
            GenreSection("Romance", api.discoverByGenre(10749).results.take(12)),
            GenreSection("Terror", api.discoverByGenre(27).results.take(12))
        )
    }

    fun search(q: String) = launchSafe {
        results = if (q.isBlank()) emptyList() else api.search(q).results
    }

    fun loadDetail(id: Int) = launchSafe { selected = api.detail(id) }

    fun saveMovie(m: MovieDto) = viewModelScope.launch {
        val runtimeMinutes = m.runtime ?: runCatching { api.detail(m.id).runtime }.getOrNull() ?: 0
        repo.save(
            SavedMovie(
                id = m.id,
                title = m.title,
                posterPath = m.poster_path,
                overview = m.overview,
                runtime = runtimeMinutes,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    fun saveReview(m: SavedMovie) = viewModelScope.launch {
        val runtimeMinutes = if (m.runtime > 0) m.runtime else runCatching { api.detail(m.id).runtime }.getOrNull() ?: 0
        val updated = if (m.addedAt <= 0L) m.copy(runtime = runtimeMinutes, addedAt = System.currentTimeMillis()) else m.copy(runtime = runtimeMinutes)
        repo.save(updated)
    }

    fun ensureRuntimesFetched(savedList: List<SavedMovie>) {
        viewModelScope.launch {
            savedList.filter { (it.status == "ASSISTIDO" || it.status == "REASSISTIDO") && it.runtime <= 0 }.forEach { movie ->
                try {
                    val detail = api.detail(movie.id)
                    val minutes = detail.runtime ?: 0
                    if (minutes > 0) {
                        repo.save(movie.copy(runtime = minutes))
                    }
                } catch (e: Exception) {
                    // Ignore network errors silently
                }
            }
        }
    }

    fun remove(id: Int) = viewModelScope.launch { repo.remove(id) }

    private fun launchSafe(block: suspend () -> Unit) = viewModelScope.launch {
        loading = true; error = null
        try { block() } catch (e: Exception) { error = "Erro de conexão" }
        loading = false
    }
}
