package com.example.aulamobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import kotlinx.coroutines.launch

/**
 * Tela Inicial do aplicativo CineLista.
 * Exibe saudação com foto de perfil, busca com bottom sheet modal para seleção
 * entre Filmes e Séries, e carrosséis de destaques e gêneros.
 *
 * @OptIn(ExperimentalMaterial3Api::class) é necessário porque o ModalBottomSheet
 * e o SheetState ainda são APIs experimentais do Material 3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MovieViewModel,
    authViewModel: AuthViewModel = viewModel(),
    onMovie: (Int) -> Unit,
    onSeries: (Int) -> Unit = onMovie
) {
    // Texto digitado no campo de busca (estado local da tela)
    var query by remember { mutableStateOf("") }
    // Tipo de busca escolhido: true = séries, false = filmes. Começa com o valor guardado no ViewModel
    var searchInSeries by remember { mutableStateOf(vm.isSearchSeries) }
    // Controla se o bottom sheet de escolha (Filmes/Séries) está aberto
    var showBottomSheet by remember { mutableStateOf(false) }

    // Permite dar foco ao campo de busca por código (usado depois de escolher a opção no sheet)
    val focusRequester = remember { FocusRequester() }
    // Estado do bottom sheet; skipPartiallyExpanded = true faz ele abrir direto na altura total
    // do conteúdo, sem a posição intermediária
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Escopo de coroutine para chamar funções suspend (como sheetState.hide()) a partir de cliques
    val scope = rememberCoroutineScope()

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

    // Caixa raiz: permite empilhar o conteúdo da tela e o bottom sheet por cima
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CineBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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

            // Campo de busca: clicar no campo ou no ícone abre a mini tela de opções
            // (Box necessário para sobrepor a camada transparente clicável ao campo)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    // A cada letra digitada, atualiza o texto e já dispara a busca
                    // (no tipo escolhido: filmes ou séries), sem precisar tocar na lupa do teclado
                    onValueChange = {
                        query = it
                        if (it.isNotBlank()) {
                            vm.search(it, searchInSeries)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    // O texto de dica muda conforme o tipo de busca escolhido
                    placeholder = {
                        Text(
                            text = if (searchInSeries) "Buscar série..." else "Buscar filme...",
                            color = CineTextSecondary
                        )
                    },
                    // Ícone de lupa dourado; tocar nele abre o bottom sheet
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = CineGold,
                            modifier = Modifier.clickable { showBottomSheet = true }
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    // Campo branco com texto preto; borda dourada quando focado e invisível quando não
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = CineGold,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    // O botão do teclado vira "Buscar". Ao tocar nele: se o campo estiver vazio,
                    // abre o bottom sheet para escolher o tipo; se tiver texto, executa a busca
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (query.isBlank()) {
                            showBottomSheet = true
                        } else {
                            vm.search(query, searchInSeries)
                        }
                    })
                )

                // Overlay transparente que detecta o clique inicial para abrir o bottom sheet
                // (matchParentSize cobre exatamente a área do campo; indication = null remove o
                // efeito de ondulação ao tocar)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(28.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showBottomSheet = true
                        }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Barra de progresso fina enquanto o ViewModel está carregando dados
            if (vm.loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth(), color = CineGold)
            }

            // Mensagem de erro (se houver) na cor de erro do tema
            vm.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }

            // A área abaixo muda conforme o estado da busca (4 casos):
            // 1) há texto, busca de filmes e resultados -> lista de filmes
            // 2) há texto, busca de séries e resultados -> lista de séries
            // 3) há texto, não está carregando e a lista do tipo atual está vazia -> "nenhum resultado"
            // 4) caso contrário -> carrosséis por gênero

            // Resultados da Busca de Filmes
            if (query.isNotBlank() && !vm.isSearchSeries && vm.results.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Cada filme vira uma linha com título, data de lançamento,
                    // nota (formatada com vírgula) e pôster; tocar abre os detalhes do filme.
                    // A key tem o prefixo "m_" para não colidir com IDs de séries
                    items(vm.results, key = { "m_${it.id}" }) { m ->
                        MovieListRow(
                            title = m.title,
                            subtitle = m.release_date.orEmpty(),
                            ratingLabel = m.vote_average?.let { "%.1f".format(it).replace('.', ',') },
                            posterPath = m.poster_path,
                            onClick = { onMovie(m.id) }
                        )
                    }
                }
            } else if (query.isNotBlank() && vm.isSearchSeries && vm.tvResults.isNotEmpty()) {
                // Resultados da Busca de Séries
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    // Igual à lista de filmes, mas usa os campos de série (name e first_air_date)
                    // e, ao tocar, chama onSeries para abrir os detalhes da série
                    items(vm.tvResults, key = { "tv_${it.id}" }) { s ->
                        MovieListRow(
                            title = s.name,
                            subtitle = s.first_air_date.orEmpty(),
                            ratingLabel = s.vote_average?.let { "%.1f".format(it).replace('.', ',') },
                            posterPath = s.poster_path,
                            onClick = { onSeries(s.id) }
                        )
                    }
                }
            } else if (query.isNotBlank() && !vm.loading && ((!vm.isSearchSeries && vm.results.isEmpty()) || (vm.isSearchSeries && vm.tvResults.isEmpty()))) {
                // Busca concluída sem resultados (na lista do tipo que está selecionado)
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

        // Mini tela cinza (ModalBottomSheet) subindo de baixo para cima
        // Só é composta quando showBottomSheet é true
        if (showBottomSheet) {
            ModalBottomSheet(
                // Fecha ao tocar fora ou arrastar o sheet para baixo
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF28292E), // Tom cinza escuro elegante
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Opção 1: Buscar Filmes
                    // Ao tocar: define o modo "filmes", anima o fechamento do sheet e, quando
                    // terminar, remove o sheet da tela, devolve o foco ao campo de busca e,
                    // se já havia texto digitado, refaz a busca como filmes
                    Text(
                        text = "Buscar Filmes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                searchInSeries = false
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showBottomSheet = false
                                    focusRequester.requestFocus()
                                    if (query.isNotBlank()) vm.search(query, false)
                                }
                            }
                            .padding(vertical = 12.dp)
                    )

                    // Linha divisória entre as duas opções
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)

                    // Opção 2: Buscar Séries
                    // Mesmo fluxo da opção anterior, mas com o modo "séries"
                    Text(
                        text = "Buscar Séries",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                searchInSeries = true
                                scope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showBottomSheet = false
                                    focusRequester.requestFocus()
                                    if (query.isNotBlank()) vm.search(query, true)
                                }
                            }
                            .padding(vertical = 12.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
