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

/**
 * Seção de filmes de um gênero na Home (um carrossel).
 */
data class GenreSection(val title: String, val movies: List<MovieDto>)

/**
 * Seção de séries de um gênero na Home (um carrossel).
 */
data class GenreSectionTv(val title: String, val shows: List<TvShowDto>)

/**
 * ViewModel central do app: carrega dados da API do TMDB (populares, gêneros, busca, detalhes)
 * e gerencia a lista pessoal do usuário no Firestore (salvar, avaliar, remover).
 * É compartilhado entre as telas pelo AppNav.
 */
class MovieViewModel : ViewModel() {
    // Cliente da API do TMDB, criado só no primeiro uso (lazy)
    private val api by lazy { TmdbApi.create() }
    // Repositório que acessa a lista pessoal no Firestore
    private val repo = FirebaseRepository()

    // Estados observados pela UI (mutableStateOf faz a tela recompor ao mudar).
    // "private set" permite alteração só aqui dentro; a UI apenas lê.
    // Filmes populares e seções de filmes por gênero
    var popular by mutableStateOf<List<MovieDto>>(emptyList()); private set
    var genreSections by mutableStateOf<List<GenreSection>>(emptyList()); private set

    // Séries populares e seções de séries por gênero
    var popularTv by mutableStateOf<List<TvShowDto>>(emptyList()); private set
    var genreSectionsTv by mutableStateOf<List<GenreSectionTv>>(emptyList()); private set

    // Resultados da busca e filme/série atualmente abertos na tela de detalhes
    var results by mutableStateOf<List<MovieDto>>(emptyList()); private set
    var selected by mutableStateOf<MovieDto?>(null); private set
    var selectedTv by mutableStateOf<TvShowDto?>(null); private set

    // Indicador de carregamento e mensagem de erro (null = sem erro)
    var loading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    // Lista pessoal do usuário (filmes e séries salvos), atualizada em tempo real pelo Firestore.
    // stateIn transforma o Flow em StateFlow; WhileSubscribed(5000) mantém a escuta ativa
    // por 5s após a última tela parar de observar (evita reconectar em rotações de tela).
    val myList: StateFlow<List<SavedMovie>> = repo.observeSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Bloco de inicialização: ao criar o ViewModel, já dispara o carregamento
     * dos filmes e séries populares e das seções por gênero.
     */
    init {
        loadPopular()
        loadGenreSections()
        loadPopularTv()
        loadGenreSectionsTv()
    }

    /** Carrega a lista de filmes populares do TMDB e guarda em [popular]. */
    fun loadPopular() = launchSafe { popular = api.popular().results }

    /**
     * Carrega as seções de filmes por gênero da Home (Ação, Aventura, Drama, Comédia,
     * Fantasia, Ficção científica, Romance e Terror). Para cada gênero, busca pelo ID do
     * gênero no TMDB e mantém só os 12 primeiros filmes.
     */
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

    /** Carrega a lista de séries populares do TMDB e guarda em [popularTv]. */
    fun loadPopularTv() = launchSafe { popularTv = api.popularTv().results }

    /**
     * Carrega as seções de séries por gênero da Home (Ação e Aventura, Drama, Comédia,
     * Sci-Fi & Fantasy, Animação e Mistério), com os 12 primeiros de cada gênero.
     */
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

    /**
     * Busca filmes pelo título e guarda em [results].
     * Se o texto estiver em branco, limpa os resultados sem chamar a API.
     * - q: termo digitado pelo usuário
     */
    fun search(q: String) = launchSafe {
        results = if (q.isBlank()) emptyList() else api.search(q).results
    }

    /**
     * Carrega os detalhes de um filme ou série e guarda em [selected] ou [selectedTv].
     * Zera o outro estado para não misturar dados. Se a chamada falhar, o resultado vira null
     * (runCatching evita que a exceção derrube a função).
     * - id: ID do item no TMDB
     * - isSeries: true = série, false = filme
     */
    fun loadDetail(id: Int, isSeries: Boolean) = launchSafe {
        if (isSeries) {
            selectedTv = runCatching { api.tvDetail(id) }.getOrNull()
            selected = null
        } else {
            selected = runCatching { api.detail(id) }.getOrNull()
            selectedTv = null
        }
    }

    /**
     * Salva um filme ou série na lista pessoal do usuário (usada pelo botão "Adicionar" da tela
     * de detalhes), com status inicial "VAI_ASSISTIR".
     * - Série: usa os dados já carregados (ou busca na API) e calcula a duração total
     *   como nº de episódios x duração do episódio (padrões: 10 episódios e 45 min).
     * - Filme: usa os dados já carregados (ou busca na API) e pega o runtime (0 se indisponível).
     * - id: ID do item no TMDB
     * - isSeries: true = série, false = filme
     */
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

    /**
     * Salva na lista pessoal um filme a partir de um MovieDto já em mãos.
     * Se o DTO não trouxer a duração, tenta buscá-la na API (0 se falhar).
     * - m: filme a salvar
     */
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

    /**
     * Salva na lista pessoal uma série a partir de um TvShowDto.
     * Busca os detalhes na API para obter nº de episódios e duração por episódio
     * (se falhar, usa os dados do DTO ou os padrões 10 e 45 min) e calcula a duração total.
     * - show: série a salvar
     */
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

    /**
     * Salva as alterações feitas na tela de avaliação (status, nota, comentário, favorito etc.).
     * Se o item não tiver data de adição válida (<= 0), define como o momento atual.
     * Como o ID do documento é o ID do filme, isso atualiza o registro existente.
     * - m: item já modificado a ser gravado
     */
    fun saveReview(m: SavedMovie) = viewModelScope.launch {
        val updated = if (m.addedAt <= 0L) m.copy(addedAt = System.currentTimeMillis()) else m
        repo.save(updated)
    }

    /**
     * Corrige itens já assistidos que ficaram sem duração (runtime <= 0).
     * Para cada um (status "ASSISTIDO" ou "REASSISTIDO"), busca os dados na API e, se obtiver
     * uma duração válida, salva o item atualizado. Erros de rede são ignorados.
     * - savedList: lista pessoal a ser verificada
     * Observação: o app usa o status "REASSISTINDO" (gerúndio) nas categorias, mas esta função
     * procura "REASSISTIDO"; por isso itens "REASSISTINDO" nunca entram aqui.
     */
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

    /**
     * Remove um item da lista pessoal do usuário.
     * - id: ID do filme/série no TMDB (mesmo ID do documento no Firestore)
     */
    fun remove(id: Int) = viewModelScope.launch { repo.remove(id) }

    /**
     * Executa uma tarefa assíncrona controlando o estado de loading e de erro:
     * liga o loading e limpa o erro, roda o bloco e, se der exceção, define a mensagem
     * "Erro de conexão"; no fim, desliga o loading. Usada pelas funções de carregamento de dados.
     * - block: trabalho suspend a executar
     */
    private fun launchSafe(block: suspend () -> Unit) = viewModelScope.launch {
        loading = true; error = null
        try { block() } catch (e: Exception) { error = "Erro de conexão" }
        loading = false
    }
}