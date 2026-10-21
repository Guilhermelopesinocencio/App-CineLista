package com.example.aulamobile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineSurface

/**
 * Linha de lista que representa um filme (usada em listas como "favoritos" ou "assistidos").
 * Mostra o pôster à esquerda, o título, o subtítulo e a nota ao centro e,
 * opcionalmente, um botão de excluir à direita. O cartão inteiro é clicável.
 */
@Composable
fun MovieListRow(
    title: String,
    subtitle: String,
    ratingLabel: String?,
    posterPath: String?,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Cartão com cor de fundo e cantos arredondados do tema, ocupando toda a largura
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        // Linha principal: pôster | textos | botão de excluir (opcional)
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pôster carregado pelo Coil: monta a URL juntando o endereço base do TMDB
            // (largura 200) ao caminho recebido. Se posterPath for null, nada é carregado.
            AsyncImage(
                model = posterPath?.let { "https://image.tmdb.org/t/p/w200$it" },
                contentDescription = title,
                modifier = Modifier
                    .width(72.dp)
                    .height(108.dp)
                    // Arredonda os cantos da imagem
                    .clip(RoundedCornerShape(8.dp)),
                // Crop preenche a área toda, cortando o excesso se necessário
                contentScale = ContentScale.Crop
            )
            // Coluna de textos; weight(1f) faz ocupar o espaço restante,
            // empurrando o botão de excluir para a extremidade direita
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                // Título em destaque
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Subtítulo com cor mais discreta
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // A nota só é exibida se existir e não estiver em branco: estrela dourada + texto
                if (!ratingLabel.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = CineGold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = ratingLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }
            }
            // Botão de lixeira (vermelho) exibido apenas quando onDelete foi informado
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Excluir da lista",
                        tint = Color(0xFFE53935)
                    )
                }
            }
        }
    }
}