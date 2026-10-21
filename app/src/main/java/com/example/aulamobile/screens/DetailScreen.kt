package com.example.aulamobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineSurface
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary

// Paleta de cores usada para os "chips" de gênero; as cores são repetidas em ciclo
// (índice % tamanho da lista) caso haja mais gêneros do que cores
private val genreChipColors = listOf(
    Color(0xFF3D5A6E),
    Color(0xFF5A4A3A),
    Color(0xFF4A3D5C),
    Color(0xFF3A5A48),
    Color(0xFF5C3D3D)
)

/**
 * Tela de detalhes de um filme ou série.
 * Mostra pôster, título, ano, nota, gêneros e sinopse, além do botão para adicionar
 * o item à lista pessoal ou, se já estiver na lista, o acesso à avaliação/edição.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    vm: MovieViewModel,
    id: Int,
    isSeries: Boolean,
    onBack: () -> Unit,
    onReview: () -> Unit
) {
    // Dispara o carregamento dos detalhes ao abrir a tela (e de novo se id ou isSeries mudarem)
    LaunchedEffect(id, isSeries) { vm.loadDetail(id, isSeries) }

    // Filme/série carregado no ViewModel; takeIf garante que seja o item deste id
    // (evita mostrar dados de um item aberto anteriormente)
    val movie = vm.selected?.takeIf { it.id == id }
    val tvShow = vm.selectedTv?.takeIf { it.id == id }
    // Lista pessoal do usuário, observada em tempo real
    val list by vm.myList.collectAsState()
    // Item salvo correspondente a este id (null = ainda não está na lista)
    val saved = list.find { it.id == id }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CineBackground)
    ) {
        // Enquanto nenhum dado chegou, mostra um indicador de carregamento centralizado
        if (movie == null && tvShow == null) {
            CircularProgressIndicator(
                color = CineGold,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // Extrai os dados a exibir. Para cada campo, prioriza a fonte de acordo com o tipo
            // (série: tvShow primeiro; filme: movie primeiro) e usa a outra como alternativa.
            val title = if (isSeries) (tvShow?.name ?: movie?.title.orEmpty()) else (movie?.title ?: tvShow?.name.orEmpty())
            val posterPath = if (isSeries) (tvShow?.poster_path ?: movie?.poster_path) else (movie?.poster_path ?: tvShow?.poster_path)
            val dateStr = if (isSeries) (tvShow?.first_air_date ?: movie?.release_date) else (movie?.release_date ?: tvShow?.first_air_date)
            // Ano = 4 primeiros caracteres da data ("AAAA-MM-DD")
            val year = dateStr?.take(4).orEmpty()
            val rating = if (isSeries) (tvShow?.vote_average ?: movie?.vote_average) else (movie?.vote_average ?: tvShow?.vote_average)
            // Nota com 1 casa decimal e vírgula (ex.: 8,5); "—" se não houver nota
            val ratingText = rating?.let { "%.1f".format(it).replace('.', ',') } ?: "—"
            val overview = if (isSeries) (tvShow?.overview ?: movie?.overview) else (movie?.overview ?: tvShow?.overview)
            val genres = if (isSeries) (tvShow?.genres ?: movie?.genres) else (movie?.genres ?: tvShow?.genres)

            // Conteúdo rolável da tela
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Botão circular "X" para fechar a tela e voltar
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(CineSurface, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CineTextPrimary)
                }

                Spacer(Modifier.height(8.dp))

                // Cabeçalho: pôster à esquerda e título + ano à direita
                Row(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
                        contentDescription = title,
                        modifier = Modifier
                            .width(130.dp)
                            .height(195.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, top = 8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = CineTextPrimary
                        )
                        // O ano só aparece se houver data disponível
                        if (year.isNotBlank()) {
                            Text(
                                text = year,
                                style = MaterialTheme.typography.bodyLarge,
                                color = CineTextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Linha de ações: caixa da nota (35% da largura) + área do botão da lista (65%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Caixa com ícone de estrela, a nota média do TMDB e o rótulo "Nota"
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CineSurface,
                        modifier = Modifier.weight(0.35f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = CineTextPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = ratingText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CineTextPrimary
                            )
                            Text(
                                text = "Nota",
                                style = MaterialTheme.typography.bodySmall,
                                color = CineTextSecondary
                            )
                        }
                    }

                    if (saved == null) {
                        // Item ainda fora da lista: botão dourado que salva o item no Firestore
                        // via ViewModel; o texto muda conforme seja filme ou série
                        Button(
                            onClick = { vm.saveItem(id = id, isSeries = isSeries) },
                            modifier = Modifier
                                .weight(0.65f)
                                .height(72.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CineGold,
                                contentColor = CineBackground
                            )
                        ) {
                            Text(
                                text = if (isSeries) "Adicionar à lista de séries" else "Adicionar à lista de filmes",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    } else {
                        // Item já salvo: mostra um botão desabilitado "Na sua lista"
                        // e, abaixo, o link para avaliar/editar
                        Column(
                            modifier = Modifier.weight(0.65f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botão apenas informativo (desabilitado)
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = CineSurface,
                                    disabledContentColor = CineTextSecondary
                                )
                            ) {
                                Text("Na sua lista")
                            }
                            // Abre a tela de avaliação/edição do item
                            TextButton(
                                onClick = onReview,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Avaliar / Editar", color = CineGold)
                            }
                        }
                    }
                }

                // Gêneros: só aparecem se a lista existir e não estiver vazia
                if (!genres.isNullOrEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    // FlowRow quebra os chips em novas linhas quando não cabem na largura
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cada gênero vira um chip arredondado; a cor vem da paleta em ciclo
                        genres.forEachIndexed { index, genre ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = genreChipColors[index % genreChipColors.size]
                            ) {
                                Text(
                                    text = genre.name,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    color = CineTextPrimary,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Seção de sinopse
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Sinopse",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CineTextPrimary
                )
                Spacer(Modifier.height(8.dp))
                // Se a sinopse for nula ou em branco, mostra uma mensagem padrão
                Text(
                    text = overview?.takeIf { it.isNotBlank() } ?: "Sem sinopse disponível.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CineTextSecondary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}