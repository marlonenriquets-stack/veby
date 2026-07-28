package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.billing.BillingManager
import com.example.data.local.AudioSettingsDataStore
import com.example.data.local.UserSessionManager
import com.example.data.model.FullArtistProfile
import com.example.data.model.Genre
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.SubscriptionPlan
import com.example.data.model.User
import com.example.data.repository.MusicRepository
import com.example.player.KinemaxAudioPlayer
import com.example.service.AdManager
import com.example.service.DownloadWorker
import com.example.service.GeminiAiService
import com.example.service.PushNotificationManager
import com.example.data.remote.NetworkErrorTracker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = UserSessionManager.getInstance(application)
    val repository = MusicRepository(application)
    val audioPlayer = KinemaxAudioPlayer(application, repository)
    val adManager = AdManager(application)
    val billingManager = BillingManager(application, repository)

    val currentUser: StateFlow<User?> = sessionManager.userFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _dataInfoMessage = MutableStateFlow<String?>(null)
    val dataInfoMessage: StateFlow<String?> = _dataInfoMessage.asStateFlow()

    private val _loginToastMessage = MutableStateFlow<String?>(null)
    val loginToastMessage: StateFlow<String?> = _loginToastMessage.asStateFlow()

    // Home / Catalog state
    private val _topSongs = MutableStateFlow<List<Song>>(emptyList())
    val topSongs: StateFlow<List<Song>> = _topSongs.asStateFlow()

    private val _catalog = MutableStateFlow<List<Song>>(emptyList())
    val catalog: StateFlow<List<Song>> = _catalog.asStateFlow()

    private val _generos = MutableStateFlow<List<Genre>>(emptyList())
    val generos: StateFlow<List<Genre>> = _generos.asStateFlow()

    private val _selectedGenreId = MutableStateFlow<Long?>(null)
    val selectedGenreId: StateFlow<Long?> = _selectedGenreId.asStateFlow()

    private val _subscriptionPlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val subscriptionPlans: StateFlow<List<SubscriptionPlan>> = _subscriptionPlans.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("semana")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    private val _isLoadingCatalog = MutableStateFlow(false)
    val isLoadingCatalog: StateFlow<Boolean> = _isLoadingCatalog.asStateFlow()

    // Audio Settings State
    val crossfadeSeconds: StateFlow<Int> = AudioSettingsDataStore.getCrossfadeSeconds(application).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val eqPreset: StateFlow<String> = AudioSettingsDataStore.getEqPreset(application).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Plano"
    )

    // Library state
    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val downloadedSongs: StateFlow<List<Song>> = repository.downloadedSongsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _remotePlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _remotePlaylists.asStateFlow()

    // Artist Profile state
    private val _selectedArtistProfile = MutableStateFlow<FullArtistProfile?>(null)
    val selectedArtistProfile: StateFlow<FullArtistProfile?> = _selectedArtistProfile.asStateFlow()

    private val _isLoadingArtistProfile = MutableStateFlow(false)
    val isLoadingArtistProfile: StateFlow<Boolean> = _isLoadingArtistProfile.asStateFlow()

    // Player Bottom Sheet state
    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    // Premium upgrade dialog
    private val _showPremiumDialog = MutableStateFlow(false)
    val showPremiumDialog: StateFlow<Boolean> = _showPremiumDialog.asStateFlow()

    init {
        // Observe crossfade setting
        viewModelScope.launch {
            crossfadeSeconds.collect { sec ->
                audioPlayer.crossfadeSeconds = sec
            }
        }

        // Observe billing purchase state for errors
        viewModelScope.launch {
            billingManager.purchaseStatus.collect { state ->
                if (state is BillingManager.PurchaseState.Error) {
                    _errorMessage.value = "Billing error: ${state.message}"
                }
            }
        }

        loadData()
        syncFirebaseSessionOnStart()
    }

    fun setCrossfade(seconds: Int) {
        viewModelScope.launch {
            AudioSettingsDataStore.saveCrossfadeSeconds(getApplication(), seconds)
        }
    }

    fun setEqPreset(preset: String) {
        viewModelScope.launch {
            AudioSettingsDataStore.saveEqPreset(getApplication(), preset)
        }
    }

    fun generateSmartRecommendations(activity: Activity? = null) {
        val currentSong = audioPlayer.currentSong.value ?: return
        val candidates = catalog.value
        viewModelScope.launch {
            val res = GeminiAiService.getSmartRecommendations(currentSong, candidates)
            res.onSuccess { recList ->
                if (recList.isNotEmpty()) {
                    playSong(recList.first(), recList, activity)
                }
            }
        }
    }

    fun purchasePlan(activity: Activity, productId: String) {
        billingManager.launchPurchase(activity, productId)
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun dismissPremiumDialog() {
        _showPremiumDialog.value = false
    }

    fun showPremiumDialog() {
        _showPremiumDialog.value = true
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
        NetworkErrorTracker.clear()
    }

    fun checkForErrors() {
        val err = NetworkErrorTracker.lastError
        if (err != null && !NetworkErrorTracker.hasBeenShown) {
            _errorMessage.value = err
            NetworkErrorTracker.hasBeenShown = true
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoadingCatalog.value = true
            try {
                val tops = repository.getTopSongs(_selectedPeriod.value)
                val cat = repository.getCatalog(generoId = _selectedGenreId.value)
                val plans = repository.getPlanes()
                val pls = repository.getPlaylistsRemote()
                val gens = repository.getGeneros()
                repository.getFavoritosRemote()
                
                _topSongs.value = tops
                _catalog.value = cat
                _subscriptionPlans.value = plans
                _remotePlaylists.value = pls
                _generos.value = gens
                _dataInfoMessage.value = "Se cargaron ${cat.size} canciones, ${gens.size} géneros, ${tops.size} más escuchadas, ${plans.size} planes"
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar datos API: ${e.localizedMessage ?: e.message}"
            } finally {
                _isLoadingCatalog.value = false
                checkForErrors()
            }
        }
    }

    fun selectGenre(genreId: Long?) {
        _selectedGenreId.value = genreId
        viewModelScope.launch {
            _isLoadingCatalog.value = true
            try {
                val cat = repository.getCatalog(generoId = genreId)
                _catalog.value = cat
            } catch (e: Exception) {
                _errorMessage.value = "Error al filtrar por género: ${e.localizedMessage ?: e.message}"
            } finally {
                _isLoadingCatalog.value = false
                checkForErrors()
            }
        }
    }

    fun selectPeriod(period: String) {
        _selectedPeriod.value = period
        viewModelScope.launch {
            _topSongs.value = repository.getTopSongs(period)
        }
    }

    fun playSong(song: Song, playlist: List<Song> = emptyList(), activity: Activity? = null) {
        audioPlayer.playSong(song, playlist)
        val user = currentUser.value
        val quitaAnuncios = user?.quitaAnuncios ?: false
        adManager.onSongPlayed(activity, quitaAnuncios)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isCurrentlyFav = favoriteSongs.value.any { it.id == song.id }
            repository.toggleFavorite(song, isCurrentlyFav)
            repository.getFavoritosRemote()
        }
    }

    fun downloadSong(song: Song) {
        val user = currentUser.value
        val permiteDescargas = user?.permiteDescargas ?: false

        if (!permiteDescargas) {
            _showPremiumDialog.value = true
            return
        }

        val workData = Data.Builder()
            .putLong("song_id", song.id)
            .putString("titulo", song.titulo)
            .putString("artista", song.artista)
            .putString("album", song.album ?: "Single")
            .putInt("duracion", song.duracionSeconds)
            .putString("portada_url", song.portadaUrl)
            .putString("audio_url", song.audioUrl)
            .putString("genero", song.generoNombre)
            .build()

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workData)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(downloadRequest)
    }

    fun createPlaylist(nombre: String, descripcion: String? = null) {
        viewModelScope.launch {
            repository.createPlaylistRemote(nombre, descripcion)
            _remotePlaylists.value = repository.getPlaylistsRemote()
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.addSongToPlaylistRemote(playlistId, songId)
            _remotePlaylists.value = repository.getPlaylistsRemote()
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            repository.removeSongFromPlaylistRemote(playlistId, songId)
            _remotePlaylists.value = repository.getPlaylistsRemote()
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylistRemote(playlistId)
            _remotePlaylists.value = repository.getPlaylistsRemote()
        }
    }

    fun loadArtistProfile(artistId: Long) {
        viewModelScope.launch {
            _isLoadingArtistProfile.value = true
            try {
                val profile = repository.getArtistProfile(artistId)
                if (profile != null) {
                    _selectedArtistProfile.value = profile
                } else {
                    _errorMessage.value = "No se pudo obtener la información del artista"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar artista: ${e.localizedMessage ?: e.message}"
            } finally {
                _isLoadingArtistProfile.value = false
            }
        }
    }

    fun clearSelectedArtistProfile() {
        _selectedArtistProfile.value = null
    }

    // --- Authentication Actions ---
    private fun syncFirebaseSessionOnStart() {
        viewModelScope.launch {
            val firebaseUser = try { FirebaseAuth.getInstance().currentUser } catch (e: Exception) { null }
            if (firebaseUser != null) {
                firebaseUser.getIdToken(true).addOnSuccessListener { tokenResult ->
                    val idToken = tokenResult.token ?: "demo_firebase_token"
                    viewModelScope.launch {
                        sessionManager.updateToken(idToken)
                        val backendUser = repository.syncFirebaseLogin()
                        if (backendUser != null) {
                            sessionManager.saveSession(idToken, backendUser)
                            _authUiState.value = AuthUiState.Success(backendUser)
                            _loginToastMessage.value = "Login: es_premium=${backendUser.esPremium}, descargas=${backendUser.permiteDescargas}, no_ads=${backendUser.quitaAnuncios}"
                            PushNotificationManager.loginUser(backendUser.id, backendUser.email)
                        }
                    }
                }
            }
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authUiState.value = AuthUiState.Error("Ingresa tu correo y contraseña")
            return
        }
        _authUiState.value = AuthUiState.Loading

        val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        if (auth == null) {
            _authUiState.value = AuthUiState.Error("Firebase Auth no disponible")
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                user?.getIdToken(true)?.addOnSuccessListener { result ->
                    val idToken = result.token ?: ""
                    viewModelScope.launch {
                        sessionManager.updateToken(idToken)
                        val backendUser = repository.syncFirebaseLogin() ?: User(
                            id = user.uid,
                            nombre = user.displayName ?: email.substringBefore("@"),
                            email = email,
                            esPremium = false,
                            quitaAnuncios = false,
                            permiteDescargas = false
                        )
                        sessionManager.saveSession(idToken, backendUser)
                        _authUiState.value = AuthUiState.Success(backendUser)
                        _loginToastMessage.value = "Login: es_premium=${backendUser.esPremium}, descargas=${backendUser.permiteDescargas}, no_ads=${backendUser.quitaAnuncios}"
                        PushNotificationManager.loginUser(backendUser.id, backendUser.email)
                    }
                }
            }
            .addOnFailureListener { e ->
                _authUiState.value = AuthUiState.Error(e.localizedMessage ?: "Error al iniciar sesión")
            }
    }

    fun registerWithEmail(nombre: String, email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authUiState.value = AuthUiState.Error("Completa todos los campos")
            return
        }
        _authUiState.value = AuthUiState.Loading

        val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        if (auth == null) {
            _authUiState.value = AuthUiState.Error("Firebase Auth no disponible")
            return
        }

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                user?.getIdToken(true)?.addOnSuccessListener { result ->
                    val idToken = result.token ?: ""
                    viewModelScope.launch {
                        sessionManager.updateToken(idToken)
                        val backendUser = repository.syncFirebaseLogin() ?: User(
                            id = user.uid,
                            nombre = if (nombre.isNotBlank()) nombre else email.substringBefore("@"),
                            email = email,
                            esPremium = false,
                            quitaAnuncios = false,
                            permiteDescargas = false
                        )
                        sessionManager.saveSession(idToken, backendUser)
                        _authUiState.value = AuthUiState.Success(backendUser)
                        _loginToastMessage.value = "Login: es_premium=${backendUser.esPremium}, descargas=${backendUser.permiteDescargas}, no_ads=${backendUser.quitaAnuncios}"
                        PushNotificationManager.loginUser(backendUser.id, backendUser.email)
                    }
                }
            }
            .addOnFailureListener { e ->
                _authUiState.value = AuthUiState.Error(e.localizedMessage ?: "Error al registrarse")
            }
    }

    fun logout() {
        try { FirebaseAuth.getInstance().signOut() } catch (e: Exception) { }
        viewModelScope.launch {
            sessionManager.clearSession()
            PushNotificationManager.logoutUser()
            _authUiState.value = AuthUiState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        billingManager.endConnection()
    }
}

