package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SynthEngine
import com.example.data.db.AppDatabase
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MusicRepository(db.musicDao())

    private val synthEngine = SynthEngine()

    // Database Flows
    val allSongs: StateFlow<List<Song>> = repository.allSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<Song>> = repository.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Player State Flows
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _playSpeed = MutableStateFlow(1.0f)
    val playSpeed: StateFlow<Float> = _playSpeed

    private val _loopActive = MutableStateFlow(false)
    val loopActive: StateFlow<Boolean> = _loopActive

    private val _shuffleActive = MutableStateFlow(false)
    val shuffleActive: StateFlow<Boolean> = _shuffleActive

    // Live streams from SynthEngine
    val visualizerData: StateFlow<FloatArray> = synthEngine.visualizerFlow
    val playbackProgressMs: StateFlow<Long> = synthEngine.playbackProgressMs

    // Current playlist tracking
    private val _activePlaylistId = MutableStateFlow<Int?>(null)
    val activePlaylistId: StateFlow<Int?> = _activePlaylistId

    val activePlaylistSongs: StateFlow<List<Song>> = _activePlaylistId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getSongsForPlaylist(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedDefaultSongsIfEmpty()
            // Set first song as default current track
            allSongs.first { it.isNotEmpty() }.let { seeded ->
                if (_currentSong.value == null) {
                    _currentSong.value = seeded.first()
                }
            }
        }
        
        // Setup observer to trigger automatic song-looping or track-ending skip
        viewModelScope.launch {
            playbackProgressMs.collect { progress ->
                val song = _currentSong.value ?: return@collect
                if (progress >= song.durationMs && progress > 0) {
                    if (_loopActive.value) {
                        // Restart synth
                        playSong(song)
                    } else {
                        // Skip to next
                        playNext()
                    }
                }
            }
        }
    }

    fun selectPlaylist(playlistId: Int?) {
        _activePlaylistId.value = playlistId
    }

    fun playSong(song: Song) {
        _currentSong.value = song
        _isPlaying.value = true
        synthEngine.start(song.synthStyle, song.baseFrequency, song.durationMs)
        synthEngine.setSpeed(_playSpeed.value)
    }

    fun togglePlayPause() {
        val song = _currentSong.value ?: return
        if (_isPlaying.value) {
            synthEngine.stop()
            _isPlaying.value = false
        } else {
            _isPlaying.value = true
            synthEngine.start(song.synthStyle, song.baseFrequency, song.durationMs)
            synthEngine.setSpeed(_playSpeed.value)
        }
    }

    fun setSpeed(speed: Float) {
        _playSpeed.value = speed
        synthEngine.setSpeed(speed)
    }

    fun toggleLoop() {
        _loopActive.value = !_loopActive.value
    }

    fun toggleShuffle() {
        _shuffleActive.value = !_shuffleActive.value
    }

    fun playNext() {
        val current = _currentSong.value ?: return
        val currentList = getActiveSongQueue()
        if (currentList.isEmpty()) return

        val nextSong = if (_shuffleActive.value) {
            currentList.random()
        } else {
            val idx = currentList.indexOfFirst { it.id == current.id }
            if (idx == -1 || idx == currentList.size - 1) {
                currentList.first()
            } else {
                currentList[idx + 1]
            }
        }
        playSong(nextSong)
    }

    fun playPrevious() {
        val current = _currentSong.value ?: return
        val currentList = getActiveSongQueue()
        if (currentList.isEmpty()) return

        val prevSong = if (_shuffleActive.value) {
            currentList.random()
        } else {
            val idx = currentList.indexOfFirst { it.id == current.id }
            if (idx == -1 || idx == 0) {
                currentList.last()
            } else {
                currentList[idx - 1]
            }
        }
        playSong(prevSong)
    }

    private fun getActiveSongQueue(): List<Song> {
        val songsInPlaylist = activePlaylistSongs.value
        return if (_activePlaylistId.value != null && songsInPlaylist.isNotEmpty()) {
            songsInPlaylist
        } else {
            allSongs.value
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val updated = song.copy(isFavorite = !song.isFavorite)
            repository.updateSong(updated)
            // If the current playing song is favorited, update current song reference
            if (_currentSong.value?.id == song.id) {
                _currentSong.value = updated
            }
        }
    }

    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            if (_activePlaylistId.value == playlist.id) {
                _activePlaylistId.value = null
            }
            repository.deletePlaylist(playlist)
        }
    }

    fun addSongToPlaylist(playlistId: Int, songId: String) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Int, songId: String) {
        viewModelScope.launch {
            repository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun customizeSynth(frequency: Float) {
        val song = _currentSong.value ?: return
        viewModelScope.launch {
            val updated = song.copy(baseFrequency = frequency)
            repository.updateSong(updated)
            _currentSong.value = updated
            if (_isPlaying.value) {
                // Instantly update running parameters
                synthEngine.start(updated.synthStyle, updated.baseFrequency, updated.durationMs)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        synthEngine.stop()
    }
}
