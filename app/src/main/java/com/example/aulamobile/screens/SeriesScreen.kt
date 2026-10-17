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

val seriesCategories = listOf(
    CategorySpec("ASSISTINDO", "Assistindo"),
    CategorySpec("REASSISTINDO", "Reassistindo"),
    CategorySpec("ASSISTIDO", "Assistidos"),
    CategorySpec("PAUSADO", "Pausado"),
    CategorySpec("ABANDONADO", "Abandonado"),
    CategorySpec("VAI_ASSISTIR", "Vai assistir"),
    CategorySpec("FAVORITOS", "Favoritos")
)

@Composable
fun SeriesScreen(vm: MovieViewModel, onSeriesClick: (Int) -> Unit) {
    val list by vm.myList.collectAsState()
    val seriesList = list.filter { it.isSeries }
    SeriesScreenContent(
        list = seriesList,
        onSeriesClick = onSeriesClick,
        onDelete = { id -> vm.remove(id) }
    )
}

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
        Column(Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(
                text = "Lista de séries",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CineTextPrimary
            )
        }

        if (list.isEmpty()) {
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
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                seriesCategories.forEach { cat ->
                    val matchingSeries = list.filter { matchesCategory(it, cat.key) }
                    if (matchingSeries.isNotEmpty()) {
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

                        items(matchingSeries, key = { "series_${cat.key}_${it.id}" }) { s ->
                            MovieListRow(
                                title = s.title,
                                subtitle = subtitleForSeries(s),
                                ratingLabel = if (s.rating > 0) {
                                    if (s.rating % 1f == 0f) s.rating.toInt().toString()
                                    else "%.1f".format(s.rating).replace('.', ',')
                                } else null,
                                posterPath = s.posterPath,
                                onClick = { onSeriesClick(s.id) },
                                onDelete = onDelete?.let { { it(s.id) } }
                            )
                        }

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

private fun subtitleForSeries(s: SavedMovie): String {
    val year = s.overview?.take(4).orEmpty()
    return if (year.matches(Regex("\\d{4}"))) year else "Série · —"
}

