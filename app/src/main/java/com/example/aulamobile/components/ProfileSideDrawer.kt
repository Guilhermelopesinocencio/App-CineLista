package com.example.aulamobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aulamobile.ui.theme.CineBorder
import com.example.aulamobile.ui.theme.CineOrange
import com.example.aulamobile.ui.theme.CineSurface
import com.example.aulamobile.ui.theme.CineTextPrimary
import com.example.aulamobile.ui.theme.CineTextSecondary
import com.google.firebase.auth.FirebaseUser

/**
 * Menu lateral (drawer) de perfil que desliza pelo lado direito da tela.
 * Mostra avatar, nome, e-mail, botão de sair da conta, uma opção de tema e a versão do app.
 * Quando aberto, escurece o restante da tela; tocar na área escura fecha o menu.
 */
@Composable
fun ProfileSideDrawer(
    isOpen: Boolean,
    user: FirebaseUser?,
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Só desenha algo se o drawer estiver aberto
    if (isOpen) {
        // Caixa que cobre a tela inteira e empilha o fundo escuro e o painel
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            // Fundo escurecido (overlay): clicar nele fecha o drawer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { onDismiss() }
            )

            // Painel lateral de 300dp de largura, alinhado à direita e com altura total
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .align(Alignment.CenterEnd),
                color = CineSurface,
                tonalElevation = 16.dp
            ) {
                // Coluna principal: conteúdo no topo e rodapé (versão) na parte de baixo
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Bloco superior: avatar, dados do usuário, botão de sair e opções
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.height(32.dp))

                        // Avatar circular com borda laranja e ícone de pessoa (foto não é usada aqui)
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF24242C))
                                .border(2.dp, CineOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = CineTextSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Nome exibido: displayName se houver; senão a parte do e-mail antes do "@";
                        // se nada existir, mostra "Usuário"
                        Text(
                            text = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email?.substringBefore('@') ?: "Usuário",
                            color = CineTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // E-mail do usuário (texto vazio se não houver usuário)
                        Text(
                            text = user?.email.orEmpty(),
                            color = CineTextSecondary,
                            fontSize = 13.sp
                        )

                        Spacer(Modifier.height(24.dp))

                        // Botão que encerra a sessão do usuário
                        Button(
                            onClick = onLogoutClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CineOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = "Sair da Conta",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(Modifier.height(32.dp))
                        // Linha divisória entre o botão e as opções
                        HorizontalDivider(color = CineBorder, thickness = 1.dp)
                        Spacer(Modifier.height(20.dp))

                        // Opção "Tema": por enquanto é apenas visual (o clique ainda não faz nada)
                        // e mostra o valor atual fixo "Sistema"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Lado esquerdo: ícone de paleta + rótulo "Tema"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.ColorLens,
                                    contentDescription = null,
                                    tint = CineOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Tema",
                                    color = CineTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Lado direito: valor atual ("Sistema") + seta indicando que é navegável
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sistema",
                                    color = CineTextSecondary,
                                    fontSize = 13.sp
                                )
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = CineTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Rodapé com a versão do aplicativo, centralizado na parte de baixo
                    Text(
                        text = "CineLista V1.0.0",
                        color = CineTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}