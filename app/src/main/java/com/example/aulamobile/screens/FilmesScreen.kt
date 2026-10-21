package com.example.aulamobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.aulamobile.components.MovieListRow
import com.example.aulamobile.firebase.SavedMovie
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineBorder
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineListaTheme
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary

/**
 * Especificação de uma categoria de status de filme.
 *
 * - key: identificador interno da categoria (compara com o campo status do SavedMovie)
 * - label: texto exibido ao usuário no cabeçalho da seção
 */
data class CategorySpec(
    val key: String,
    val label: String
)

/**
 * Lista das 7 categorias organizacionais do catálogo de filmes do usuário.
 * A ordem aqui define a ordem em que as seções aparecem na tela.
 */
val categories = listOf(
    CategorySpec("ASSISTINDO", "Assistindo"),
    CategorySpec("REASSISTINDO", "Reassistindo"),
    CategorySpec("ASSISTIDO", "Assistidos"),
    CategorySpec("PAUSADO", "Pausado"),
    CategorySpec("ABANDONADO", "Abandonado"),
    CategorySpec("VAI_ASSISTIR", "Vai assistir"),
    CategorySpec("FAVORITOS", "Favoritos")
)

/**
 * Tela principal da Lista Pessoal de Filmes do Usuário.
 * Filtra e exibe apenas os itens onde [SavedMovie.isSeries] é `false`.
 *
 * É a versão "conectada" da tela: busca os dados no ViewModel e repassa
 * para o FilmesScreenContent, que apenas desenha a interface.
 */
@Composable
fun FilmesScreen(vm: MovieViewModel, onMovie: (Int) -> Unit) {
    // Lista pessoal (filmes e séries) observada em tempo real
    val list by vm.myList.collectAsState()
    // Mantém somente os filmes, descartando as séries
    val movieList = list.filter { !it.isSeries }
    FilmesScreenContent(
        list = movieList,
        onMovie = onMovie,
        // Ao tocar na lixeira de um item, remove-o da lista pelo ID
        onDelete = { id -> vm.remove(id) }
    )
}

/**
 * Conteúdo visual da Lista de Filmes dividida por categorias.
 * Não depende do ViewModel (recebe tudo por parâmetro), o que facilita
 * reutilização e previews.
 *
 * Parâmetros:
 * - list: filmes a exibir (já filtrados)
 * - onMovie: chamado com o ID do filme ao tocar em um item
 * - onDelete: chamado com o ID ao tocar na lixeira; se for null, o botão de excluir não aparece
 */
@Composable
fun FilmesScreenContent(
    list: List<SavedMovie>,
    onMovie: (Int) -> Unit,
    onDelete: ((Int) -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(CineBackground)
    ) {
        // Título da tela
        Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                text = "Lista de filmes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CineTextPrimary
            )
        }

        if (list.isEmpty()) {
            // Estado vazio: mensagem centralizada quando não há nenhum filme
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum filme na sua lista ainda",
                    color = CineTextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            // Lista rolável e eficiente (só compõe os itens visíveis)
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Para cada categoria, monta uma seção: cabeçalho + filmes + divisória
                categories.forEach { cat ->
                    // Filmes que pertencem a esta categoria
                    val matchingMovies = list.filter { matchesCategory(it, cat.key) }
                    // Categorias sem nenhum filme não aparecem
                    if (matchingMovies.isNotEmpty()) {
                        // Cabeçalho da seção: nome da categoria + quantidade de filmes
                        item(key = "header_${cat.key}") {
                            Column {
                                Text(
                                    text = "${cat.label} (${matchingMovies.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CineGold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        // Uma linha para cada filme da categoria; a key inclui a categoria
                        // porque o mesmo filme pode aparecer em duas seções (ex.: Favoritos)
                        items(matchingMovies, key = { "movie_${cat.key}_${it.id}" }) { m ->
                            MovieListRow(
                                title = m.title,
                                subtitle = subtitleFor(m),
                                // Nota do usuário: sem nota (0) não exibe; nota inteira aparece
                                // sem casa decimal (ex.: 8); as demais com vírgula (ex.: 8,5)
                                ratingLabel = if (m.rating > 0) {
                                    if (m.rating % 1f == 0f) m.rating.toInt().toString()
                                    else "%.1f".format(m.rating).replace('.', ',')
                                } else null,
                                posterPath = m.posterPath,
                                onClick = { onMovie(m.id) },
                                // Só repassa a ação de excluir se onDelete foi informado
                                onDelete = onDelete?.let { { it(m.id) } }
                            )
                        }

                        // Linha divisória ao final da seção
                        item(key = "divider_${cat.key}") {
                            HorizontalDivider(
                                color = CineBorder,
                                thickness = 1.dp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Verifica se um filme pertence à categoria do filtro.
 * Para a maioria das categorias basta o status ser igual à chave. Exceções:
 * - VAI_ASSISTIR: também aceita o status antigo "QUERO_ASSISTIR" (compatibilidade)
 * - FAVORITOS: aceita status "FAVORITOS" ou o campo favorite = true,
 *   então um filme favorito aparece aqui além da sua categoria de status
 * Retorna false para chaves desconhecidas.
 */
private fun matchesCategory(movie: SavedMovie, categoryKey: String): Boolean = when (categoryKey) {
    "ASSISTINDO" -> movie.status == "ASSISTINDO"
    "REASSISTINDO" -> movie.status == "REASSISTINDO"
    "ASSISTIDO" -> movie.status == "ASSISTIDO"
    "PAUSADO" -> movie.status == "PAUSADO"
    "ABANDONADO" -> movie.status == "ABANDONADO"
    "VAI_ASSISTIR" -> movie.status == "VAI_ASSISTIR" || movie.status == "QUERO_ASSISTIR"
    "FAVORITOS" -> movie.status == "FAVORITOS" || movie.favorite
    else -> false
}

/**
 * Formata o subtítulo exibido abaixo do título do filme.
 * Pega os 4 primeiros caracteres do overview (sinopse) e, se forem 4 dígitos,
 * usa como ano; caso contrário, mostra "Filme · —".
 * Atenção: o ano está sendo lido do campo overview, não de uma data de lançamento;
 * como o SavedMovie não tem campo de ano, isso quase sempre cairá no texto padrão.
 */
private fun subtitleFor(m: SavedMovie): String {
    val year = m.overview?.take(4).orEmpty()
    return if (year.matches(Regex("\\d{4}"))) year else "Filme · —"
}