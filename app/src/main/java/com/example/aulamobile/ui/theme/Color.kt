package com.example.aulamobile.ui.theme

import androidx.compose.ui.graphics.Color

// === Cores padrão ===
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// CineLista Theme Palette
// === Paleta personalizada do CineLista (usada nas telas e componentes do app) ===

// Fundo principal das telas (preto-acinzentado bem escuro)
val CineBackground = Color(0xFF121214)
// Superfície de elementos sobre o fundo: cartões das listas, chips, caixas de nota etc.
val CineSurface = Color(0xFF1E1E22)
// Fundo dos cartões de dialog (login, cadastro, editar perfil)
val CineCardBg = Color(0xFF1A1A1E)
/** Cor de destaque dos mockups (abas ativas, botões, links). */
// Dourado: cor de destaque do app (botões, títulos de seção, item selecionado da barra inferior)
val CineGold = Color(0xFFC5A365)
// Versão mais escura do dourado (para variações como estado pressionado)
val CineGoldDark = Color(0xFF9A7B42)
/** Mantido para arquivos que ainda referenciam o nome antigo. */
// Nomes antigos (quando a cor de destaque era laranja): são apenas apelidos que apontam
// para o dourado, então os arquivos que ainda usam CineOrange (ex.: AuthDialogs e
// ProfileSideDrawer) passam a exibir dourado sem precisar de alteração
val CineOrange = CineGold
val CineOrangeDark = CineGoldDark
// Texto principal (branco)
val CineTextPrimary = Color(0xFFFFFFFF)
// Texto secundário (cinza): subtítulos, placeholders, ícones discretos
val CineTextSecondary = Color(0xFFA0A0A0)
// Cor das bordas e divisórias
val CineBorder = Color(0xFF2D2D35)