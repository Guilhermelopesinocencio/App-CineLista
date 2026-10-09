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
import com.example.aulamobile.ui.theme.CineBackground
import com.example.aulamobile.ui.theme.CineBorder
import com.example.aulamobile.ui.theme.CineCardBg
import com.example.aulamobile.ui.theme.CineGold
import com.example.aulamobile.ui.theme.CineSurface
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary
import com.google.firebase.auth.FirebaseAuth

private val presetAvatars = listOf(
    "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=400",
    "https://images.unsplash.com/photo-1514565131-fce0801e5785?w=400",
    "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=400",
    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    vm: MovieViewModel,
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit
) {
    val authState by authViewModel.state.collectAsState()
    val user = authState.user ?: FirebaseAuth.getInstance().currentUser
    val profile = authState.profile
    val list by vm.myList.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(list) {
        vm.ensureRuntimesFetched(list)
    }

    val watched = list.filter { it.status == "ASSISTIDO" || it.status == "REASSISTIDO" }
    val totalWatchedMinutes = watched.sumOf { it.runtime }
    val watchedHoursText = remember(totalWatchedMinutes) {
        if (totalWatchedMinutes <= 0) "0h"
        else {
            val h = totalWatchedMinutes / 60
            val m = totalWatchedMinutes % 60
            if (h == 0) "${m}m"
            else if (m == 0) "${h}h"
            else "${h}h ${m}m"
        }
    }

    val rated = watched.filter { it.rating > 0 }.map { it.rating }
    val avg = if (rated.isEmpty()) 0.0 else rated.average()
    val favorites = list.count { it.favorite || it.status == "FAVORITOS" }
    val rewatched = list.count { it.status == "REASSISTIDO" }

    val displayName = profile.name.ifBlank {
        user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Usuário"
    }
    val photoUrl = profile.photoUrl.ifBlank { user?.photoUrl?.toString().orEmpty() }
    val bioText = profile.bio.ifBlank { "Nenhuma biografia cadastrada." }
    val initial = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CineBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Photo
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
        SectionTitle("Bio")
        Text(
            text = bioText,
            color = CineTextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // Stats Section
        SectionTitle("Estatísticas")
        StatRow("Total de filmes na lista", list.size.toString())
        StatRow("Filmes assistidos", watched.size.toString())
        StatRow("Horas Assistidas", watchedHoursText)
        StatRow("Nota média", if (avg > 0) "%.1f".format(avg).replace('.', ',') else "0")
        StatRow("Filmes favoritos", favorites.toString())
        StatRow("Filmes reassistidos", rewatched.toString())

        Spacer(Modifier.height(32.dp))

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditProfileDialog(
    currentName: String,
    currentPhotoUrl: String,
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (name: String, photoUrl: String, bio: String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var selectedPhotoUrl by remember { mutableStateOf(currentPhotoUrl.ifBlank { presetAvatars.first() }) }
    var bio by remember { mutableStateOf(currentBio) }

    Dialog(onDismissRequest = onDismiss) {
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
                Column {
                    Text(
                        text = "Escolha seu avatar:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = CineGold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
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
