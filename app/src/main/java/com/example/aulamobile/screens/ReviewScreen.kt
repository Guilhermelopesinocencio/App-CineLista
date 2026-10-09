package com.example.aulamobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineBorder
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineSurface
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary

private val statusOptions = listOf(
    "ASSISTINDO" to "Assistindo",
    "REASSISTINDO" to "Reassistindo",
    "ASSISTIDO" to "Assistido",
    "PAUSADO" to "Pausado",
    "ABANDONADO" to "Abandonado",
    "VAI_ASSISTIR" to "Vai assistir",
    "FAVORITOS" to "Favoritos"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewScreen(vm: MovieViewModel, id: Int, onDone: () -> Unit) {
    val list by vm.myList.collectAsState()
    val movie = list.find { it.id == id } ?: return

    var selectedStatus by remember(movie.id) {
        mutableStateOf(
            if (movie.status == "QUERO_ASSISTIR") "VAI_ASSISTIR" else movie.status
        )
    }
    var rating by remember(movie.id) { mutableFloatStateOf(movie.rating) }
    var comment by remember(movie.id) { mutableStateOf(movie.comment) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CineBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDone,
                    modifier = Modifier
                        .size(40.dp)
                        .background(CineSurface, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CineTextPrimary)
                }

                Text(
                    text = "Avaliar e Editar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CineTextPrimary
                )

                Spacer(Modifier.size(40.dp))
            }

            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = CineTextPrimary
            )

            // Status Selection Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Status no Filme",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CineGold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    statusOptions.forEach { (key, label) ->
                        val isSelected = selectedStatus == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) CineGold else CineSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) CineGold else CineBorder
                            ),
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).clickable {
                                selectedStatus = key
                            }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) CineBackground else CineTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Rating Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Star,
                        contentDescription = null,
                        tint = CineGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Sua Nota: ${"%.1f".format(rating).replace('.', ',')}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CineTextPrimary
                    )
                }

                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 0f..10f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = CineGold,
                        activeTrackColor = CineGold,
                        inactiveTrackColor = CineSurface
                    )
                )
            }

            // Comment Section
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comentário ou anotações", color = CineTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CineGold,
                    unfocusedBorderColor = CineBorder,
                    focusedTextColor = CineTextPrimary,
                    unfocusedTextColor = CineTextPrimary,
                    focusedContainerColor = CineSurface,
                    unfocusedContainerColor = CineSurface
                )
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    vm.saveReview(
                        movie.copy(
                            rating = rating,
                            comment = comment,
                            status = selectedStatus,
                            favorite = (selectedStatus == "FAVORITOS")
                        )
                    )
                    onDone()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CineGold,
                    contentColor = CineBackground
                )
            ) {
                Text(
                    text = "Salvar Alterações",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
