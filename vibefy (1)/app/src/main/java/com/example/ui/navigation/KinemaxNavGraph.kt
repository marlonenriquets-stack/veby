package com.example.ui.navigation

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Song
import com.example.service.RemoteConfigManager
import com.example.ui.MainViewModel
import androidx.compose.ui.zIndex
import com.example.ui.components.FullPlayerBottomSheet
import com.example.ui.components.InAppNotificationBanner
import com.example.ui.components.NotificationsCenterBottomSheet
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.PremiumDialog
import com.example.ui.components.QueueBottomSheet
import com.example.data.model.ArtistAlbum
import com.example.ui.screens.artist.AlbumDetailScreen
import com.example.ui.screens.artist.ArtistProfileScreen
import com.example.ui.screens.auth.LoginRegisterScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxBackground
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

sealed class NavDestination(
    val route: String,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    object Home : NavDestination("home", "Inicio", Icons.Filled.Home, Icons.Outlined.Home)
    object Search : NavDestination("search", "Buscar", Icons.Filled.Search, Icons.Outlined.Search)
    object Library : NavDestination("library", "Biblioteca", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic)
    object Profile : NavDestination("profile", "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun KinemaxNavGraph(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val authUiState by viewModel.authUiState.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    // Check if user is authenticated
    if (currentUser == null) {
        LoginRegisterScreen(
            authUiState = authUiState,
            onLoginClick = { email, pass -> viewModel.loginWithEmail(email, pass) },
            onRegisterClick = { nombre, email, pass -> viewModel.registerWithEmail(nombre, email, pass) },
            modifier = modifier
        )
        return
    }

    // Main App Navigation State
    var currentDestination by remember { mutableStateOf<NavDestination>(NavDestination.Home) }
    var showAudioSettings by remember { mutableStateOf(false) }
    var showAiChatSheet by remember { mutableStateOf(false) }
    var selectedSongForPlaylistDialog by remember { mutableStateOf<Song?>(null) }
    var selectedArtistId by remember { mutableStateOf<Long?>(null) }
    var selectedAlbum by remember { mutableStateOf<ArtistAlbum?>(null) }
    var selectedAlbumArtistName by remember { mutableStateOf("") }

    val albumes by viewModel.albumes.collectAsState()
    val selectedAlbumDetail by viewModel.selectedAlbumDetail.collectAsState()

    // Cuando se abre un álbum desde Inicio, lo convertimos al mismo modelo
    // (ArtistAlbum) que ya usa AlbumDetailScreen para los álbumes de un artista,
    // así reusamos la misma pantalla sin duplicar UI.
    androidx.compose.runtime.LaunchedEffect(selectedAlbumDetail) {
        selectedAlbumDetail?.let { detalle ->
            selectedAlbum = ArtistAlbum(
                nombre = detalle.nombre,
                portadaUrl = detalle.portadaUrl,
                numCanciones = detalle.numCanciones,
                canciones = detalle.canciones
            )
            selectedAlbumArtistName = detalle.artistaNombre ?: ""
            viewModel.clearSelectedAlbumDetail()
        }
    }

    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiChatLoading by viewModel.isAiChatLoading.collectAsState()
    val djStyle by viewModel.djStyle.collectAsState()

    val selectedArtistProfile by viewModel.selectedArtistProfile.collectAsState()
    val isLoadingArtistProfile by viewModel.isLoadingArtistProfile.collectAsState()

    val onArtistClick: (Long) -> Unit = { artistId ->
        selectedArtistId = artistId
        viewModel.loadArtistProfile(artistId)
    }

    val topSongs by viewModel.topSongs.collectAsState()
    val catalog by viewModel.catalog.collectAsState()
    val generos by viewModel.generos.collectAsState()
    val selectedGenreId by viewModel.selectedGenreId.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val dataInfoMessage by viewModel.dataInfoMessage.collectAsState()
    val loginToastMessage by viewModel.loginToastMessage.collectAsState()

    val favoriteSongs by viewModel.favoriteSongs.collectAsState()
    val downloadedSongs by viewModel.downloadedSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val subscriptionPlans by viewModel.subscriptionPlans.collectAsState()

    // Show Toast for login info message if present
    androidx.compose.runtime.LaunchedEffect(loginToastMessage) {
        loginToastMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    val currentPlayingSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsState()
    val progressMs by viewModel.audioPlayer.progressMs.collectAsState()
    val durationMs by viewModel.audioPlayer.durationMs.collectAsState()
    val isShuffle by viewModel.audioPlayer.isShuffle.collectAsState()
    val repeatMode by viewModel.audioPlayer.repeatMode.collectAsState()
    val isDjModeActive by viewModel.audioPlayer.isDjModeActive.collectAsState()
    val isSpeakingDj by viewModel.audioPlayer.isSpeakingDj.collectAsState()
    val crossfadeTransition by viewModel.audioPlayer.crossfadeTransition.collectAsState()
    val crossfadeProgress by viewModel.audioPlayer.crossfadeProgress.collectAsState()

    val crossfadeSeconds by viewModel.crossfadeSeconds.collectAsState()
    val eqPreset by viewModel.eqPreset.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val volumeNormalizationEnabled by viewModel.volumeNormalizationEnabled.collectAsState()
    val volumeLevel by viewModel.volumeLevel.collectAsState()
    val bassBoostStrength by viewModel.bassBoostStrength.collectAsState()
    val virtualizerStrength by viewModel.virtualizerStrength.collectAsState()

    val isPlayerExpanded by viewModel.isPlayerExpanded.collectAsState()
    val showPremiumDialog by viewModel.showPremiumDialog.collectAsState()

    // Notification State
    val inAppNotifications by viewModel.inAppNotifications.collectAsState()
    val currentNotificationBanner by viewModel.currentNotificationBanner.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    val showNotificationsCenter by viewModel.showNotificationsCenter.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KinemaxBackground,
        bottomBar = {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_area")
            ) {
                // Persistent MiniPlayerBar if song is active
                AnimatedVisibility(
                    visible = currentPlayingSong != null && !isPlayerExpanded && !showAudioSettings,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    currentPlayingSong?.let { song ->
                        MiniPlayerBar(
                            song = song,
                            isPlaying = isPlaying,
                            progressMs = progressMs,
                            durationMs = durationMs,
                            crossfadeTransition = crossfadeTransition,
                            crossfadeProgress = crossfadeProgress,
                            onPlayPauseClick = { viewModel.audioPlayer.togglePlayPause() },
                            onNextClick = { viewModel.audioPlayer.next() },
                            onBarClick = { viewModel.setPlayerExpanded(true) }
                        )
                    }
                }

                // Bottom Navigation Bar
                if (!showAudioSettings) {
                    NavigationBar(
                        containerColor = KinemaxSurface,
                        contentColor = KinemaxAccent,
                        tonalElevation = 8.dp
                    ) {
                        val destinations = listOf(
                            NavDestination.Home,
                            NavDestination.Search,
                            NavDestination.Library,
                            NavDestination.Profile
                        )

                        destinations.forEach { dest ->
                            val isSelected = currentDestination.route == dest.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) dest.activeIcon else dest.inactiveIcon,
                                        contentDescription = dest.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = KinemaxAccent,
                                    selectedTextColor = KinemaxAccent,
                                    unselectedIconColor = KinemaxTextSecondary,
                                    unselectedTextColor = KinemaxTextSecondary,
                                    indicatorColor = KinemaxAccent.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_${dest.route}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (selectedAlbum != null) {
                AlbumDetailScreen(
                    album = selectedAlbum!!,
                    artistName = selectedArtistProfile?.nombre ?: selectedAlbumArtistName.ifBlank { "Artista" },
                    currentPlayingSong = currentPlayingSong,
                    isPlaying = isPlaying,
                    permiteDescargas = currentUser?.permiteDescargas ?: false,
                    onBackClick = { selectedAlbum = null; selectedAlbumArtistName = "" },
                    onSongClick = { song, playlist -> viewModel.playSong(song, playlist, activity) },
                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                    onDownloadClick = { viewModel.downloadSong(it) },
                    onAddToPlaylistClick = { selectedSongForPlaylistDialog = it },
                    onAddToQueueClick = { viewModel.addToQueue(it) }
                )
            } else if (selectedArtistId != null) {
                ArtistProfileScreen(
                    artistProfile = selectedArtistProfile,
                    isLoading = isLoadingArtistProfile,
                    currentPlayingSong = currentPlayingSong,
                    isPlaying = isPlaying,
                    permiteDescargas = currentUser?.permiteDescargas ?: false,
                    onBackClick = {
                        selectedArtistId = null
                        viewModel.clearSelectedArtistProfile()
                    },
                    onSongClick = { song, playlist -> viewModel.playSong(song, playlist, activity) },
                    onFavoriteClick = { viewModel.toggleFavorite(it) },
                    onDownloadClick = { viewModel.downloadSong(it) },
                    onAddToPlaylistClick = { selectedSongForPlaylistDialog = it },
                    onAddToQueueClick = { viewModel.addToQueue(it) },
                    onAlbumClick = { album -> selectedAlbum = album }
                )
            } else if (showAudioSettings) {
                com.example.ui.screens.settings.AudioSettingsScreen(
                    crossfadeSeconds = crossfadeSeconds,
                    selectedPreset = eqPreset,
                    audioQuality = audioQuality,
                    volumeNormalizationEnabled = volumeNormalizationEnabled,
                    volumeLevel = volumeLevel,
                    bassBoostStrength = bassBoostStrength,
                    virtualizerStrength = virtualizerStrength,
                    djStyle = djStyle,
                    onCrossfadeChanged = { viewModel.setCrossfade(it) },
                    onPresetSelected = { viewModel.setEqPreset(it) },
                    onAudioQualitySelected = { viewModel.setAudioQuality(it) },
                    onVolumeNormalizationChanged = { viewModel.setVolumeNormalization(it) },
                    onVolumeLevelSelected = { viewModel.setVolumeLevel(it) },
                    onBassBoostChanged = { viewModel.setBassBoostStrength(it) },
                    onVirtualizerChanged = { viewModel.setVirtualizerStrength(it) },
                    onDjStyleSelected = { viewModel.setDjStyle(it) },
                    onBackClick = { showAudioSettings = false }
                )
            } else {
                when (currentDestination) {
                    NavDestination.Home -> HomeScreen(
                        user = currentUser,
                        topSongs = topSongs,
                        catalog = catalog,
                        currentPlayingSong = currentPlayingSong,
                        isPlaying = isPlaying,
                        selectedPeriod = selectedPeriod,
                        dataInfoMessage = dataInfoMessage,
                        onPeriodSelected = { viewModel.selectPeriod(it) },
                        onSongClick = { song, playlist -> viewModel.playSong(song, playlist, activity) },
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        onDownloadClick = { viewModel.downloadSong(it) },
                        onAddToPlaylistClick = { selectedSongForPlaylistDialog = it },
                        onAddToQueueClick = { viewModel.addToQueue(it) },
                        onArtistClick = onArtistClick,
                        albumes = albumes,
                        onAlbumClick = { viewModel.openAlbumFromHome(it) },
                        onOpenAiChatClick = { showAiChatSheet = true }
                    )
                    NavDestination.Search -> SearchScreen(
                        catalog = catalog,
                        generos = generos,
                        selectedGenreId = selectedGenreId,
                        user = currentUser,
                        currentPlayingSong = currentPlayingSong,
                        isPlaying = isPlaying,
                        onGenreSelected = { viewModel.selectGenre(it) },
                        onSongClick = { song, playlist -> viewModel.playSong(song, playlist, activity) },
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        onDownloadClick = { viewModel.downloadSong(it) },
                        onAddToPlaylistClick = { selectedSongForPlaylistDialog = it },
                    onAddToQueueClick = { viewModel.addToQueue(it) },
                        onArtistClick = onArtistClick
                    )
                    NavDestination.Library -> LibraryScreen(
                        user = currentUser,
                        playlists = playlists,
                        favoriteSongs = favoriteSongs,
                        downloadedSongs = downloadedSongs,
                        currentPlayingSong = currentPlayingSong,
                        isPlaying = isPlaying,
                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                        onDeletePlaylist = { viewModel.deletePlaylist(it) },
                        onRemoveSongFromPlaylist = { pId, sId -> viewModel.removeSongFromPlaylist(pId, sId) },
                        onSongClick = { song, playlist -> viewModel.playSong(song, playlist, activity) },
                        onFavoriteClick = { viewModel.toggleFavorite(it) },
                        onDownloadClick = { viewModel.downloadSong(it) },
                        onShowPremiumUpgrade = { viewModel.showPremiumDialog() },
                        onArtistClick = onArtistClick
                    )
                    NavDestination.Profile -> ProfileScreen(
                        user = currentUser,
                        onShowPremiumUpgrade = { viewModel.showPremiumDialog() },
                        onAudioSettingsClick = { showAudioSettings = true },
                        onLogoutClick = { viewModel.logout() }
                    )
                }
            }
        }
    }

    // Full Screen Player Overlay Sheet
    FullPlayerBottomSheet(
        isVisible = isPlayerExpanded,
        song = currentPlayingSong,
        isPlaying = isPlaying,
        progressMs = progressMs,
        durationMs = durationMs,
        isShuffle = isShuffle,
        repeatMode = repeatMode,
        isDjModeActive = isDjModeActive,
        isSpeakingDj = isSpeakingDj,
        crossfadeTransition = crossfadeTransition,
        crossfadeProgress = crossfadeProgress,
        permiteDescargas = currentUser?.permiteDescargas ?: false,
        onDismiss = { viewModel.setPlayerExpanded(false) },
        onPlayPauseClick = { viewModel.audioPlayer.togglePlayPause() },
        onNextClick = { viewModel.audioPlayer.next() },
        onPreviousClick = { viewModel.audioPlayer.previous() },
        onSeekTo = { viewModel.audioPlayer.seekTo(it) },
        onShuffleClick = { viewModel.audioPlayer.toggleShuffle() },
        onRepeatClick = { viewModel.audioPlayer.toggleRepeat() },
        onFavoriteClick = { viewModel.toggleFavorite(it) },
        onDownloadClick = { viewModel.downloadSong(it) },
        onToggleDjMode = { viewModel.audioPlayer.toggleDjMode() },
        onRecommendationsClick = { viewModel.generateSmartRecommendations(activity) },
        onOpenAiChat = { showAiChatSheet = true },
        onArtistClick = onArtistClick,
        onQueueClick = { viewModel.showQueue() }
    )

    // Interactive AI Chat Assistant Sheet
    com.example.ui.components.AiChatBottomSheet(
        isVisible = showAiChatSheet,
        messages = chatMessages,
        isLoading = isAiChatLoading,
        onDismiss = { showAiChatSheet = false },
        onSendMessage = { viewModel.sendChatMessage(it) },
        onClearChat = { viewModel.clearChatHistory() },
        onPlaySong = { song -> viewModel.playSong(song, catalog, activity) },
        onAddToQueue = { song -> viewModel.addToQueue(song) }
    )

    // Cola de reproducción
    val showQueueSheet by viewModel.showQueueSheet.collectAsState()
    val queueSongs by viewModel.queue.collectAsState()
    if (showQueueSheet) {
        QueueBottomSheet(
            currentSong = currentPlayingSong,
            queue = queueSongs,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismiss = { viewModel.dismissQueue() },
            onPlayFromQueue = { viewModel.playFromQueue(it) },
            onRemoveFromQueue = { viewModel.removeFromQueue(it) }
        )
    }

    // Premium Upgrade Dialog for Free users
    PremiumDialog(
        isOpen = showPremiumDialog,
        plans = subscriptionPlans,
        onDismiss = { viewModel.dismissPremiumDialog() },
        onSelectPlan = { plan ->
            val sku = plan.googlePlayProductId
            if (activity != null && !sku.isNullOrBlank()) {
                viewModel.purchasePlan(activity, sku)
            }
            viewModel.dismissPremiumDialog()
        }
    )

    // Add To Playlist Dialog
    selectedSongForPlaylistDialog?.let { song ->
        com.example.ui.components.AddToPlaylistDialog(
            song = song,
            playlists = playlists,
            onDismiss = { selectedSongForPlaylistDialog = null },
            onSelectPlaylist = { playlist ->
                viewModel.addSongToPlaylist(playlist.id, song.id)
                selectedSongForPlaylistDialog = null
            },
            onCreateNewPlaylist = { newName ->
                viewModel.createPlaylist(newName)
                selectedSongForPlaylistDialog = null
            }
        )
    }

    // Network / API Error Popup
    val errorMessage by viewModel.errorMessage.collectAsState()
    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Reporte de Conexión / Error API",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = errorMessage ?: "Ocurrió un error al conectar con el servidor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Servidor: ${RemoteConfigManager.serverUrl}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "API Key: ${RemoteConfigManager.apiKey}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearErrorMessage()
                        viewModel.loadData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reintentar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.clearErrorMessage() }
                ) {
                    Text("Cerrar")
                }
            },
            containerColor = KinemaxSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
