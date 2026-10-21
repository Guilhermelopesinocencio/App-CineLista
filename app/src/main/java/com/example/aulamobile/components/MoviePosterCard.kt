package com.example.aulamobile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Cartão vertical compacto de um filme: pôster em cima e título embaixo.
 * Pensado para carrosséis/listas horizontais (ex.: "Em alta", "Populares").
 * O conjunto inteiro é clicável.
 */
@Composable
fun MoviePosterCard(
    title: String,
    posterPath: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Coluna de largura fixa (120dp) que serve de área clicável para o cartão todo
    Column(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        // Pôster carregado pelo Coil: monta a URL juntando o endereço base do TMDB
        // (largura 342, maior resolução que a das listas) ao caminho recebido.
        // Se posterPath for null, nenhuma imagem é carregada.
        AsyncImage(
            model = posterPath?.let { "https://image.tmdb.org/t/p/w342$it" },
            contentDescription = title,
            modifier = Modifier
                .width(120.dp)
                .height(180.dp)
                // Arredonda os cantos da imagem
                .clip(RoundedCornerShape(8.dp)),
            // Crop preenche a área toda, cortando o excesso se necessário
            contentScale = ContentScale.Crop
        )
        // Título abaixo do pôster, limitado a 2 linhas; se for maior, termina com "..."
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(120.dp)
        )
    }
}