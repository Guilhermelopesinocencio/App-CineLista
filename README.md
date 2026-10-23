# 🎬 CineLista — Catálogo de Filmes e Séries Android

## 🚀 Como Executar o Projeto Localmente

### 📋 Pré-requisitos

- **Android Studio** (versão Ladybug 2024.2.1 ou superior recomendada).
- **JDK 17** ou superior.
- Dispositivo físico Android ou Emulador com **Android 7.0 (API 24)** ou superior.

---

### 🔧 Passo a Passo de Configuração

#### 1. Clonar o Repositório
```bash
git clone https://github.com/SEU_USUARIO/CineLista.git
cd CineLista
```

#### 2. Configurar a Chave da API do TMDB (`local.properties`)
Crie ou abra o arquivo `local.properties` na raiz do projeto e adicione a sua chave do TMDB:
```properties
sdk.dir=SEU_CAMINHO_DO_ANDROID_SDK
TMDB_KEY=
```
*(Se você tiver sua própria chave do TMDB, substitua pelo seu token).*

#### 3. Configurar o Firebase (`google-services.json`)
O aplicativo utiliza o Firebase para autenticação e salvamento no Firestore.
1. Acesse o [Console do Firebase](https://console.firebase.google.com/).
2. Crie um projeto Firebase e adicione um aplicativo Android com o pacote:
   `com.example.aulamobile`
3. Ative os serviços:
   - **Firebase Authentication** (Método E-mail/Senha).
   - **Cloud Firestore Database** (em modo de teste ou regras de produção para `users/{uid}`).
4. Baixe o arquivo `google-services.json` e coloque-o dentro do diretório `app/` do seu projeto:
   `CineLista/app/google-services.json`

#### 4. Compilar e Executar
1. Abra o projeto no **Android Studio**.
2. Aguarde a sincronização do Gradle (**Gradle Sync**).
3. Selecione o dispositivo físico ou emulador e clique em **Run (Shift + F10)**.

---

## 📂 Estrutura de Pastas do Projeto

```text
app/src/main/java/com/example/aulamobile/
├── auth/                       # Gerenciamento de Autenticação no Firebase
│   └── AuthViewModel.kt
├── components/                 # Componentes reutilizáveis de interface (Cards, Modais, Diálogos)
│   ├── AppTopBar.kt
│   ├── AuthModals.kt
│   ├── MovieCard.kt
│   ├── MovieListRow.kt
│   ├── MoviePosterCard.kt
│   └── ProfileSideDrawer.kt
├── firebase/                   # Modelos, DTOs e Cliente Retrofit / Firestore
│   ├── Dtos.kt
│   ├── FirebaseRepository.kt
│   ├── SavedMovie.kt
│   └── TmdbApi.kt
├── navigation/                 # Definição de Rotas e Navegação (NavHost e NavigationBar)
│   ├── AppNav.kt
│   └── Screen.kt
├── screens/                    # Telas principais do aplicativo
│   ├── DetailScreen.kt         # Detalhes do Filme ou Série
│   ├── FilmesScreen.kt         # Lista Pessoal de Filmes
│   ├── HomeScreen.kt           # Tela Inicial (Destaques e Gêneros)
│   ├── LoginGateScreen.kt      # Tela de Bloqueio/Login Obrigatório
│   ├── MovieViewModel.kt       # ViewModel de Estado de Filmes e Séries
│   ├── ProfileScreen.kt        # Perfil e Estatísticas do Usuário
│   ├── ReviewScreen.kt         # Avaliação e Troca de Status
│   ├── SearchScreen.kt         # Busca por Título
│   └── SeriesScreen.kt         # Lista Pessoal de Séries
└── ui/theme/                   # Tema visual, cores e tipografia (Material 3)
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```
