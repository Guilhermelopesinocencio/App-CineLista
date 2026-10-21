package com.example.aulamobile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Cartão que representa um filme em uma lista.
 * Mostra o pôster à esquerda e, à direita, o título e um subtítulo opcional.
 * O cartão inteiro é clicável.
 */
@Composable
fun MovieCard(
    title: String,
    poster: String?,
    subtitle: String = "",
    onClick: () -> Unit
) {
    // Cartão que ocupa toda a largura e responde a toques
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(8.dp)) {
            // Pôster carregado pela biblioteca Coil. A URL completa é montada juntando o
            // endereço base de imagens do TMDB (largura 200) ao caminho recebido.
            // Se poster for null, o model fica null e nenhuma imagem é carregada.
            AsyncImage(
                model = poster?.let { "https://image.tmdb.org/t/p/w200$it" },
                contentDescription = title,
                modifier = Modifier
                    .width(80.dp)
                    .height(120.dp),
                // Crop preenche toda a área da imagem, cortando o excesso se necessário
                contentScale = ContentScale.Crop
            )
            // Espaço horizontal entre o pôster e os textos
            Spacer(modifier = Modifier.width(12.dp))
            // Coluna com os textos do filme
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                // O subtítulo só aparece se tiver conteúdo
                if (subtitle.isNotBlank()) {
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}