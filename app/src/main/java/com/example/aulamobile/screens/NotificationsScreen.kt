package com.example.aulamobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.aulamobile.firebase.SavedMovie
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineListaTheme
import com.example.aulamobile.ui.theme.CineSurface
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(vm: MovieViewModel, onOpenMovie: (Int) -> Unit) {
    val list by vm.myList.collectAsState()
    val watchedList = list
        .filter { it.status == "ASSISTIDO" }
        .sortedByDescending { it.addedAt }

    Column(
        Modifier
            .fillMaxSize()
            .background(CineBackground)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Notificações",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = CineTextPrimary,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        if (watchedList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nenhum filme assistido recente", color = CineTextSecondary)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(watchedList, key = { it.id }) { movie ->
                    NotificationCard(
                        movie = movie,
                        onClick = { onOpenMovie(movie.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(movie: SavedMovie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!movie.posterPath.isNullOrBlank()) {
                AsyncImage(
                    model = "https://image.tmdb.org/t/p/w200${movie.posterPath}",
                    contentDescription = movie.title,
                    modifier = Modifier
                        .width(56.dp)
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF33333D)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Movie,
                        contentDescription = null,
                        tint = CineTextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = movie.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CineTextPrimary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatAddedDate(movie.addedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = CineTextSecondary
                    )
                }
                Text(
                    text = "Ver detalhes",
                    color = CineGold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun formatAddedDate(timestamp: Long): String {
    if (timestamp <= 0L) return "Atividade recente"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

