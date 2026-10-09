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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aulamobile.auth.AuthViewModel
import com.example.aulamobile.components.MovieListRow
import com.example.aulamobile.components.MoviePosterCard
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary

@Composable
fun HomeScreen(
    vm: MovieViewModel,
    authViewModel: AuthViewModel = viewModel(),
    onMovie: (Int) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val authState by authViewModel.state.collectAsState()
    val user = authState.user
    val profile = authState.profile

    val displayName = profile.name.ifBlank {
        user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Usuário"
    }
    val photoUrl = profile.photoUrl.ifBlank { user?.photoUrl?.toString().orEmpty() }
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CineBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Olá, $displayName!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CineTextPrimary
            )
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { vm.search(query) })
        )
        Spacer(Modifier.height(8.dp))
        if (vm.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = CineGold)
        }
        vm.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
        }

        if (query.isNotBlank() && vm.results.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum filme encontrado", color = CineTextSecondary)
            }
        } else {
            if (vm.genreSections.isEmpty() && vm.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CineGold)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        Text(
                            text = "Filmes Populares",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CineTextPrimary
                        )
                    }
                    items(vm.genreSections, key = { it.title }) { section ->
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CineTextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(section.movies, key = { it.id }) { movie ->
                                MoviePosterCard(
                                    title = movie.title,
                                    posterPath = movie.poster_path,
                                    onClick = { onMovie(movie.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
