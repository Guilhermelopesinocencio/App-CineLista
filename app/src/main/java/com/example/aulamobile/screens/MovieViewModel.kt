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
import com.example.aulamobile.firebase.TvShowDto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GenreSection(val title: String, val movies: List<MovieDto>)
data class GenreSectionTv(val title: String, val shows: List<TvShowDto>)

class MovieViewModel : ViewModel() {
    private val api by lazy { TmdbApi.create() }
    private val repo = FirebaseRepository()

    var popular by mutableStateOf<List<MovieDto>>(emptyList()); private set
    var genreSections by mutableStateOf<List<GenreSection>>(emptyList()); private set

    var popularTv by mutableStateOf<List<TvShowDto>>(emptyList()); private set
    var genreSectionsTv by mutableStateOf<List<GenreSectionTv>>(emptyList()); private set

    var results by mutableStateOf<List<MovieDto>>(emptyList()); private set
    var selected by mutableStateOf<MovieDto?>(null); private set
    var selectedTv by mutableStateOf<TvShowDto?>(null); private set

    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    val myList: StateFlow<List<SavedMovie>> = repo.observeSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadPopular()
        loadGenreSections()
        loadPopularTv()
        loadGenreSectionsTv()
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

    fun loadPopularTv() = launchSafe { popularTv = api.popularTv().results }

    fun loadGenreSectionsTv() = launchSafe {
        genreSectionsTv = listOf(
            GenreSectionTv("Ação e Aventura", api.discoverTvByGenre(10759).results.take(12)),
            GenreSectionTv("Drama", api.discoverTvByGenre(18).results.take(12)),
            GenreSectionTv("Comédia", api.discoverTvByGenre(35).results.take(12)),
            GenreSectionTv("Sci-Fi & Fantasy", api.discoverTvByGenre(10765).results.take(12)),
            GenreSectionTv("Animação", api.discoverTvByGenre(16).results.take(12)),
            GenreSectionTv("Mistério", api.discoverTvByGenre(9648).results.take(12))
        )
    }

    fun search(q: String) = launchSafe {
        results = if (q.isBlank()) emptyList() else api.search(q).results
    }

    fun loadDetail(id: Int, isSeries: Boolean) = launchSafe {
        if (isSeries) {
            selectedTv = runCatching { api.tvDetail(id) }.getOrNull()
            selected = null
        } else {
            selected = runCatching { api.detail(id) }.getOrNull()
            selectedTv = null
        }
    }

    fun saveItem(id: Int, isSeries: Boolean) = viewModelScope.launch {
        if (isSeries) {
            val show = selectedTv ?: runCatching { api.tvDetail(id) }.getOrNull()
            val totalEpisodes = show?.number_of_episodes ?: 10
            val epRuntime = show?.episode_run_time?.firstOrNull() ?: 45
            val totalRuntimeMinutes = totalEpisodes * epRuntime

            repo.save(
                SavedMovie(
                    id = id,
                    title = show?.name.orEmpty().ifBlank { "Série" },
                    posterPath = show?.poster_path,
                    overview = show?.overview,
                    status = "VAI_ASSISTIR",
                    runtime = totalRuntimeMinutes,
                    isSeries = true,
                    addedAt = System.currentTimeMillis()
                )
            )
        } else {
            val m = selected ?: runCatching { api.detail(id) }.getOrNull()
            val runtimeMinutes = m?.runtime ?: runCatching { api.detail(id).runtime }.getOrNull() ?: 0

            repo.save(
                SavedMovie(
                    id = id,
                    title = m?.title.orEmpty().ifBlank { "Filme" },
                    posterPath = m?.poster_path,
                    overview = m?.overview,
                    status = "VAI_ASSISTIR",
                    runtime = runtimeMinutes,
                    isSeries = false,
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveMovie(m: MovieDto) = viewModelScope.launch {
        val runtimeMinutes = m.runtime ?: runCatching { api.detail(m.id).runtime }.getOrNull() ?: 0
        repo.save(
            SavedMovie(
                id = m.id,
                title = m.title,
                posterPath = m.poster_path,
                overview = m.overview,
                runtime = runtimeMinutes,
                isSeries = false,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    fun saveTvShow(show: TvShowDto) = viewModelScope.launch {
        val detail = runCatching { api.tvDetail(show.id) }.getOrNull()
        val totalEpisodes = detail?.number_of_episodes ?: show.number_of_episodes ?: 10
        val epRuntime = detail?.episode_run_time?.firstOrNull() ?: show.episode_run_time?.firstOrNull() ?: 45
        val totalRuntimeMinutes = totalEpisodes * epRuntime

        repo.save(
            SavedMovie(
                id = show.id,
                title = show.name,
                posterPath = show.poster_path,
                overview = show.overview,
                runtime = totalRuntimeMinutes,
                isSeries = true,
                addedAt = System.currentTimeMillis()
            )
        )
    }

    fun saveReview(m: SavedMovie) = viewModelScope.launch {
        val updated = if (m.addedAt <= 0L) m.copy(addedAt = System.currentTimeMillis()) else m
        repo.save(updated)
    }

    fun ensureRuntimesFetched(savedList: List<SavedMovie>) {
        viewModelScope.launch {
            savedList.filter { (it.status == "ASSISTIDO" || it.status == "REASSISTIDO") && it.runtime <= 0 }.forEach { item ->
                try {
                    val minutes = if (item.isSeries) {
                        val detail = api.tvDetail(item.id)
                        (detail.number_of_episodes ?: 10) * (detail.episode_run_time?.firstOrNull() ?: 45)
                    } else {
                        val detail = api.detail(item.id)
                        detail.runtime ?: 0
                    }
                    if (minutes > 0) {
                        repo.save(item.copy(runtime = minutes))
                    }
                } catch (e: Exception) {
                    // Ignore network errors
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
