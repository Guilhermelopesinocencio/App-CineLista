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
 * Lista das 7 categorias organizacionais do catálogo de séries do usuário.
 * Usa o mesmo CategorySpec definido em FilmesScreen e tem o mesmo conteúdo da lista
 * "categories" de lá. A ordem aqui define a ordem em que as seções aparecem na tela.
 */
val seriesCategories = listOf(
    CategorySpec("ASSISTINDO", "Assistindo"),
    CategorySpec("REASSISTINDO", "Reassistindo"),
    CategorySpec("ASSISTIDO", "Assistidos"),
    CategorySpec("PAUSADO", "Pausado"),
    CategorySpec("ABANDONADO", "Abandonado"),
    CategorySpec("VAI_ASSISTIR", "Vai assistir"),
    CategorySpec("FAVORITOS", "Favoritos")
)

/**
 * Tela principal da Lista Pessoal de Séries do Usuário.
 * Filtra e exibe apenas os itens onde isSeries é true.
 *
 * É a versão "conectada" da tela: busca os dados no ViewModel e repassa
 * para o SeriesScreenContent, que apenas desenha a interface.
 */
@Composable
fun SeriesScreen(vm: MovieViewModel, onSeriesClick: (Int) -> Unit) {
    // Lista pessoal (filmes e séries) observada em tempo real
    val list by vm.myList.collectAsState()
    // Mantém somente as séries, descartando os filmes
    val seriesList = list.filter { it.isSeries }
    SeriesScreenContent(
        list = seriesList,
        onSeriesClick = onSeriesClick,
        // Ao tocar na lixeira de um item, remove-o da lista pelo ID
        onDelete = { id -> vm.remove(id) }
    )
}

/**
 * Conteúdo visual da Lista de Séries dividida por categorias.
 * Não depende do ViewModel (recebe tudo por parâmetro), o que facilita
 * reutilização e previews.
 */
@Composable
fun SeriesScreenContent(
    list: List<SavedMovie>,
    onSeriesClick: (Int) -> Unit,
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
                text = "Lista de séries",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CineTextPrimary
            )
        }

        if (list.isEmpty()) {
            // Estado vazio: mensagem centralizada quando não há nenhuma série
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhuma série na sua lista ainda",
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
                // Para cada categoria, monta uma seção: cabeçalho + séries + divisória
                seriesCategories.forEach { cat ->
                    // Séries que pertencem a esta categoria
                    val matchingSeries = list.filter { matchesCategory(it, cat.key) }
                    // Categorias sem nenhuma série não aparecem
                    if (matchingSeries.isNotEmpty()) {
                        // Cabeçalho da seção: nome da categoria + quantidade de séries
                        item(key = "header_${cat.key}") {
                            Column {
                                Text(
                                    text = "${cat.label} (${matchingSeries.size})",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CineGold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }

                        // Uma linha para cada série da categoria; a key inclui a categoria
                        // porque a mesma série pode aparecer em duas seções (ex.: Favoritos)
                        items(matchingSeries, key = { "series_${cat.key}_${it.id}" }) { s ->
                            MovieListRow(
                                title = s.title,
                                subtitle = subtitleForSeries(s),
                                // Nota do usuário: sem nota (0) não exibe; nota inteira aparece
                                // sem casa decimal (ex.: 8); as demais com vírgula (ex.: 8,5)
                                ratingLabel = if (s.rating > 0) {
                                    if (s.rating % 1f == 0f) s.rating.toInt().toString()
                                    else "%.1f".format(s.rating).replace('.', ',')
                                } else null,
                                posterPath = s.posterPath,
                                onClick = { onSeriesClick(s.id) },
                                // Só repassa a ação de excluir se onDelete foi informado
                                onDelete = onDelete?.let { { it(s.id) } }
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
 * Verifica se uma série pertence à categoria do filtro.
 * Para a maioria das categorias basta o status ser igual à chave. Exceções:
 * - VAI_ASSISTIR: também aceita o status antigo "QUERO_ASSISTIR" (compatibilidade)
 * - FAVORITOS: aceita status "FAVORITOS" ou o campo favorite = true
 * Retorna false para chaves desconhecidas.
 * É private neste arquivo; existe uma função com o mesmo nome em FilmesScreen.kt
 * (sem conflito, pois cada uma é privada ao seu arquivo).
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
 * Formata o subtítulo exibido abaixo do título da série.
 * Pega os 4 primeiros caracteres do overview (sinopse) e, se forem 4 dígitos,
 * usa como ano; caso contrário, mostra "Série · —".
 * Mesma observação do FilmesScreen: o ano está sendo lido do overview, não de uma data,
 * então quase sempre cairá no texto padrão.
 */
private fun subtitleForSeries(s: SavedMovie): String {
    val year = s.overview?.take(4).orEmpty()
    return if (year.matches(Regex("\\d{4}"))) year else "Série · —"
}