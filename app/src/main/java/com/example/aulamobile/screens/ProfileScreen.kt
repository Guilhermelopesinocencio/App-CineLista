package com.example.aulamobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aulamobile.auth.AuthViewModel
import com.example.aulamobile.components.AuthDialogs
import com.example.aulamobile.components.AuthModalMode
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineBorder
import com.example.aulamobile.ui.theme.CineCardBg
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineSurface
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary
import com.google.firebase.auth.FirebaseAuth

// Lista de avatares pré-definidos (URLs de imagens) que o usuário pode escolher ao editar o perfil
private val presetAvatars = listOf(
    "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400",
    "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=400",
    "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=400",
    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400"
)

/**
 * Tela de Perfil do usuário.
 * Mostra foto, nome, e-mail, bio e estatísticas de consumo (filmes e séries assistidos,
 * horas/dias assistidos e notas médias), além dos botões "Editar perfil" e "Sair da conta".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    vm: MovieViewModel,
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit
) {
    // Estado de autenticação e perfil, observados em tempo real
    val authState by authViewModel.state.collectAsState()
    // Usuário do estado; se for null, tenta pegar direto do FirebaseAuth
    val user = authState.user ?: FirebaseAuth.getInstance().currentUser
    val profile = authState.profile
    // Lista pessoal (filmes e séries) observada em tempo real
    val list by vm.myList.collectAsState()

    // Controla se o dialog de edição de perfil está aberto
    var showEditDialog by remember { mutableStateOf(false) }

    // Sempre que a lista mudar, pede ao ViewModel para completar a duração
    // dos itens assistidos que estiverem sem runtime
    LaunchedEffect(list) {
        vm.ensureRuntimesFetched(list)
    }

    // Movies stats
    // Estatísticas de filmes: total, assistidos, tempo total e nota média
    val movieList = list.filter { !it.isSeries }
    val watchedMovies = movieList.filter { it.status == "ASSISTIDO" || it.status == "REASSISTIDO" }
    // Soma das durações (em minutos) dos filmes assistidos
    val movieMinutes = watchedMovies.sumOf { it.runtime }
    // Converte minutos em texto: "0h", "45m", "2h" ou "2h 30m"; só recalcula se movieMinutes mudar
    val watchedMovieHoursText = remember(movieMinutes) {
        if (movieMinutes <= 0) "0h"
        else {
            val h = movieMinutes / 60
            val m = movieMinutes % 60
            if (h == 0) "${m}m"
            else if (m == 0) "${h}h"
            else "${h}h ${m}m"
        }
    }
    // Nota média considerando apenas os filmes assistidos que têm nota (> 0); formato com vírgula
    val movieRatings = watchedMovies.filter { it.rating > 0 }.map { it.rating }
    val avgMovieRatingText = if (movieRatings.isEmpty()) "0" else "%.1f".format(movieRatings.average()).replace('.', ',')

    // Series stats
    // Estatísticas de séries: mesma lógica, mas o tempo total é exibido em dias
    val seriesList = list.filter { it.isSeries }
    val watchedSeries = seriesList.filter { it.status == "ASSISTIDO" || it.status == "REASSISTIDO" }
    val seriesMinutes = watchedSeries.sumOf { it.runtime }
    // Converte minutos em dias (ex.: "1,5 dias"); se for menos de 0,1 dia, mostra em horas
    val watchedSeriesDaysText = remember(seriesMinutes) {
        if (seriesMinutes <= 0) "0 dias"
        else {
            val totalHours = seriesMinutes / 60f
            val days = totalHours / 24f
            if (days < 0.1f) "${totalHours.toInt()}h"
            else "%.1f dias".format(days).replace('.', ',')
        }
    }
    // Nota média das séries assistidas que têm nota
    val seriesRatings = watchedSeries.filter { it.rating > 0 }.map { it.rating }
    val avgSeriesRatingText = if (seriesRatings.isEmpty()) "0" else "%.1f".format(seriesRatings.average()).replace('.', ',')

    // Nome exibido, por ordem de prioridade: nome do perfil no Firestore, displayName do Auth,
    // parte do e-mail antes do "@" (primeira letra maiúscula) ou "Usuário"
    val displayName = profile.name.ifBlank {
        user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Usuário"
    }
    // Foto: usa a do perfil e, se vazia, a do Firebase Auth
    val photoUrl = profile.photoUrl.ifBlank { user?.photoUrl?.toString().orEmpty() }
    // Bio: texto do perfil ou uma mensagem padrão se estiver vazia
    val bioText = profile.bio.ifBlank { "Nenhuma biografia cadastrada." }
    // Inicial do nome, usada no avatar quando não há foto
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

    // Dialog de edição de perfil: aparece quando showEditDialog é true e há usuário logado.
    // Ao salvar, atualiza o perfil pelo AuthViewModel e fecha o dialog.
    if (showEditDialog && user != null) {
        EditProfileDialog(
            currentName = displayName,
            currentPhotoUrl = photoUrl,
            currentBio = profile.bio,
            onDismiss = { showEditDialog = false },
            onSave = { name, photo, bio ->
                authViewModel.updateProfile(name, photo, bio)
                showEditDialog = false
            }
        )
    }

    // Conteúdo da tela, rolável e centralizado horizontalmente
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CineBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Photo
        // Foto de perfil circular com borda dourada; se não houver foto, mostra a inicial do nome
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(2.dp, CineGold, CircleShape)
                .background(CineSurface),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de Perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.displaySmall,
                    color = CineTextPrimary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Nome e e-mail do usuário
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = CineTextPrimary
        )
        Text(
            text = user?.email.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = CineTextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(Modifier.height(12.dp))

        // Edit Profile Button
        // Botão de contorno dourado que abre o dialog de edição
        OutlinedButton(
            onClick = { showEditDialog = true },
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CineGold),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CineGold)
        ) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text("Editar perfil", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }

        Spacer(Modifier.height(24.dp))

        // Bio Section
        // Seção de biografia
        SectionTitle("Bio")
        Text(
            text = bioText,
            color = CineTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Stats Section
        // Seção de estatísticas: uma linha (rótulo à esquerda, valor à direita) para cada dado
        SectionTitle("Estatísticas")
        StatRow("Total de filmes", movieList.size.toString())
        StatRow("Filmes Assistidos", watchedMovies.size.toString())
        StatRow("Horas assistidas", watchedMovieHoursText)
        StatRow("Nota media filme", avgMovieRatingText)
        StatRow("Total de Series", seriesList.size.toString())
        StatRow("Series Assistidas", watchedSeries.size.toString())
        StatRow("Dias assistidas", watchedSeriesDaysText)
        StatRow("Nota Media Serie", avgSeriesRatingText)

        Spacer(Modifier.height(32.dp))

        // Botão de sair da conta (largura total, contorno dourado)
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CineGold),
            border = androidx.compose.foundation.BorderStroke(1.dp, CineGold)
        ) {
            Text(
                text = "Sair da conta",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

/**
 * Dialog para editar o perfil: permite escolher um avatar entre os pré-definidos,
 * alterar o nome de exibição e a biografia.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditProfileDialog(
    currentName: String,
    currentPhotoUrl: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (name: String, photoUrl: String, bio: String) -> Unit
) {
    // Estados locais dos campos; só são enviados ao salvar.
    // Se o usuário não tem foto, o primeiro avatar da lista vem selecionado por padrão.
    var name by remember { mutableStateOf(currentName) }
    var selectedPhotoUrl by remember { mutableStateOf(currentPhotoUrl.ifBlank { presetAvatars.first() }) }
    var bio by remember { mutableStateOf(currentBio) }

    Dialog(onDismissRequest = onDismiss) {
        // Cartão arredondado com cor de fundo e borda do tema
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CineCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, CineBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cabeçalho: título e botão "X" para fechar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Editar Perfil",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CineTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = CineTextSecondary)
                    }
                }

                // Selected Avatar Preview
                // Pré-visualização do avatar escolhido (ícone de pessoa se não houver nenhum)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, CineGold, CircleShape)
                        .background(CineSurface)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = selectedPhotoUrl,
                            contentDescription = "Avatar Selecionado",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = CineTextSecondary)
                    }
                }

                // Avatar Selection Row
                // Seletor de avatares: miniaturas clicáveis; a selecionada ganha borda dourada mais grossa
                Column {
                    Text(
                        text = "Escolha seu avatar:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CineGold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // FlowRow quebra para a linha seguinte se as miniaturas não couberem
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        presetAvatars.forEach { url ->
                            val isSelected = selectedPhotoUrl == url
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) CineGold else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    // Tocar na miniatura a seleciona
                                    .clickable { selectedPhotoUrl = url }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                // Name Input
                // Campo de nome de exibição
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome de Exibição", color = CineTextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CineGold,
                        unfocusedBorderColor = CineBorder,
                        focusedTextColor = CineTextPrimary,
                        unfocusedTextColor = CineTextPrimary,
                        focusedContainerColor = CineSurface,
                        unfocusedContainerColor = CineSurface
                    )
                )

                // Bio Input
                // Campo de biografia (2 a 3 linhas)
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Biografia / Frase", color = CineTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CineGold,
                        unfocusedBorderColor = CineBorder,
                        focusedTextColor = CineTextPrimary,
                        unfocusedTextColor = CineTextPrimary,
                        focusedContainerColor = CineSurface,
                        unfocusedContainerColor = CineSurface
                    )
                )

                // Botões de ação alinhados à direita: Cancelar (fecha) e Salvar (envia os dados)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = CineTextSecondary)
                    }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = { onSave(name, selectedPhotoUrl, bio) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CineGold,
                            contentColor = CineBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Título de seção (ex.: "Bio", "Estatísticas"): texto em negrito, largura total
 * e um pequeno espaço abaixo.
 * - text: texto do título
 */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = CineTextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

/**
 * Linha de estatística: rótulo em cinza à esquerda e valor em destaque à direita.
 * - label: nome da estatística (ex.: "Total de filmes")
 * - value: valor já formatado como texto (ex.: "12")
 */
@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = CineTextSecondary)
        Text(text = value, color = CineTextPrimary, fontWeight = FontWeight.SemiBold)
    }
}