package com.example.aulamobile.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.aulamobile.components.MovieCard

/**
 * Tela de busca de filmes.
 * Mostra um campo de texto e, abaixo, a lista de resultados retornados pelo ViewModel.
 * A busca é disparada ao tocar na lupa do teclado.
 */
@Composable
fun SearchScreen(vm: MovieViewModel, onMovie: (Int) -> Unit) {
    // Texto digitado no campo de busca (estado local da tela)
    var query by remember { mutableStateOf("") }
    Column(Modifier.padding(16.dp)) {
        // Campo de busca de uma linha; o botão do teclado vira "Buscar"
        // e, ao ser tocado, chama vm.search com o texto digitado
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Buscar filme") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { vm.search(query) })
        )
        Spacer(Modifier.height(8.dp))
        // Lista rolável dos resultados; cada item é um MovieCard com título, pôster
        // e data de lançamento, e tocar nele abre os detalhes do filme
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.results) { m ->
                MovieCard(m.title, m.poster_path, m.release_date.orEmpty()) { onMovie(m.id) }
            }
        }
    }
}