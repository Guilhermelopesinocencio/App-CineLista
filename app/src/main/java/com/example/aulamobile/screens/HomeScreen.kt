package com.example.aulamobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aulamobile.auth.AuthViewModel
import com.example.aulamobile.components.MovieListRow
import com.example.aulamobile.components.MoviePosterCard
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineBorder
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary

/**
 * Tela Inicial do aplicativo CineLista.
 * Exibe saudação com foto de perfil, campo de busca rápido e carrosséis horizontais
 * organizados por Filmes e Séries de TV por gênero.
 */
@Composable
fun HomeScreen(
    vm: MovieViewModel,
    authViewModel: AuthViewModel = viewModel(),
    onMovie: (Int) -> Unit,
    onSeries: (Int) -> Unit = onMovie
) {
    // Texto digitado no campo de busca (estado local da tela)
    var query by remember { mutableStateOf("") }
    // Estado de autenticação e perfil, observados em tempo real
    val authState by authViewModel.state.collectAsState()
    val user = authState.user
    val profile = authState.profile

    // Nome exibido na saudação, por ordem de prioridade:
    // 1) nome do perfil no Firestore; 2) displayName do Firebase Auth;
    // 3) parte do e-mail antes do "@" com a primeira letra maiúscula; 4) "Usuário"
    val displayName = profile.name.ifBlank {
        user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Usuário"
    }
    // URL da foto: usa a do perfil e, se estiver vazia, a do Firebase Auth
    val photoUrl = profile.photoUrl.ifBlank { user?.photoUrl?.toString().orEmpty() }
    // Inicial do nome (maiúscula), usada no avatar quando não há foto
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CineBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // Cabeçalho com nome e avatar do usuário
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Saudação personalizada
            Text(
                text = "Olá, $displayName!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CineTextPrimary
            )
            // Avatar circular: mostra a foto se existir; senão, a inicial do nome
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(CineTextSecondary.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(text = initial, color = CineTextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Campo de busca rápido por título; a busca é disparada ao tocar na lupa do teclado
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("O que você procura?", color = CineTextSecondary) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = CineTextSecondary)
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            // Campo branco, sem borda visível e com texto preto
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            // Troca o botão "Enter" do teclado por "Buscar" e chama vm.search ao tocar nele
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { vm.search(query) })
        )

        Spacer(Modifier.height(8.dp))

        // Barra de progresso fina enquanto o ViewModel está carregando dados
        if (vm.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = CineGold)
        }

        // Mensagem de erro (se houver) na cor de erro do tema
        vm.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
        }

        // A área abaixo muda conforme o estado da busca (3 casos):
        // 1) há texto e resultados -> lista de resultados
        // 2) há texto, não está carregando e não há resultados -> mensagem "nenhum resultado"
        // 3) caso contrário -> carrosséis por gênero

        // Resultados da Busca
        if (query.isNotBlank() && vm.results.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Cada resultado vira uma linha com título, data de lançamento,
                // nota (formatada com vírgula) e pôster; tocar abre os detalhes
                items(vm.results, key = { it.id }) { m ->
                    MovieListRow(
                        title = m.title,
                        subtitle = m.release_date.orEmpty(),
                        ratingLabel = m.vote_average?.let { "%.1f".format(it).replace('.', ',') },
                        posterPath = m.poster_path,
                        onClick = { onMovie(m.id) }
                    )
                }
            }
        } else if (query.isNotBlank() && !vm.loading && vm.results.isEmpty()) {
            // Busca concluída sem resultados
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum resultado encontrado", color = CineTextSecondary)
            }
        } else {
            // Seções em carrosséis por Categoria (Filmes e Séries)
            // Se ainda não há nenhuma seção carregada e está carregando, mostra um spinner central
            if (vm.genreSections.isEmpty() && vm.genreSectionsTv.isEmpty() && vm.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CineGold)
                }
            } else {
                // Lista vertical que contém todos os carrosséis horizontais
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    // === SEÇÃO 1: FILMES ===
                    // Título da seção
                    item {
                        Text(
                            text = "Filmes",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = CineGold,
                            fontSize = 24.sp
                        )
                    }

                    // Para cada gênero: nome do gênero + carrossel horizontal de pôsteres
                    items(vm.genreSections, key = { "movie_sec_${it.title}" }) { section ->
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CineTextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(section.movies, key = { "movie_${it.id}" }) { movie ->
                                MoviePosterCard(
                                    title = movie.title,
                                    posterPath = movie.poster_path,
                                    onClick = { onMovie(movie.id) }
                                )
                            }
                        }
                    }

                    // Divisória entre a seção de filmes e a de séries
                    item {
                        HorizontalDivider(
                            color = CineBorder,
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    // === SEÇÃO 2: SÉRIES ===
                    // Título da seção
                    item {
                        Text(
                            text = "Séries",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = CineGold,
                            fontSize = 24.sp
                        )
                    }

                    // Mesma estrutura dos filmes, mas com séries; tocar chama onSeries
                    items(vm.genreSectionsTv, key = { "tv_sec_${it.title}" }) { section ->
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CineTextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(section.shows, key = { "tv_${it.id}" }) { show ->
                                MoviePosterCard(
                                    title = show.name,
                                    posterPath = show.poster_path,
                                    onClick = { onSeries(show.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}