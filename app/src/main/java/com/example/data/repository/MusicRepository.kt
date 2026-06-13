package com.example.data.repository

import com.example.data.db.MusicDao
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSong
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MusicRepository(private val musicDao: MusicDao) {

    val allSongs: Flow<List<Song>> = musicDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = musicDao.getFavoriteSongs()
    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists()

    suspend fun updateSong(song: Song) {
        musicDao.updateSong(song)
    }

    suspend fun seedDefaultSongsIfEmpty() {
        val currentSongs = allSongs.first()
        if (currentSongs.isEmpty()) {
            val presets = listOf(
                Song(
                    id = "ambient_space",
                    title = "Deep Space Drift",
                    artist = "Zenith Labs",
                    album = "Milkyway Odyssey",
                    durationMs = 180000,
                    isFavorite = true,
                    isSynthetic = true,
                    synthStyle = "DEEP_SPACE",
                    baseFrequency = 110f,
                    albumGradientStart = "#0B0B1E",
                    albumGradientEnd = "#4F105F"
                ),
                Song(
                    id = "synth_outrun",
                    title = "Retro Drive",
                    artist = "Vector Horizon",
                    album = "Outrun 1988",
                    durationMs = 150000,
                    isFavorite = false,
                    isSynthetic = true,
                    synthStyle = "RETRO_WAVE",
                    baseFrequency = 147f,
                    albumGradientStart = "#F80759",
                    albumGradientEnd = "#BC4E9C"
                ),
                Song(
                    id = "lofi_rain",
                    title = "Rainy Coffee Spot",
                    artist = "Nostalgia Box",
                    album = "Suburban Coffee",
                    durationMs = 210000,
                    isFavorite = true,
                    isSynthetic = true,
                    synthStyle = "DOCK_RAIN",
                    baseFrequency = 165f,
                    albumGradientStart = "#3D7EAA",
                    albumGradientEnd = "#FFE47E"
                ),
                Song(
                    id = "ambient_sunset",
                    title = "Warm Horizon",
                    artist = "Aether Bloom",
                    album = "Ethereal Fields",
                    durationMs = 240000,
                    isFavorite = false,
                    isSynthetic = true,
                    synthStyle = "SUNSET_GLOW",
                    baseFrequency = 220f,
                    albumGradientStart = "#FF512F",
                    albumGradientEnd = "#F09819"
                ),
                Song(
                    id = "chill_pulse",
                    title = "Binaural Focus Pulse",
                    artist = "Neuro Wave",
                    album = "Mind Gym",
                    durationMs = 300000,
                    isFavorite = false,
                    isSynthetic = true,
                    synthStyle = "CHILL_AMBIENT",
                    baseFrequency = 130f,
                    albumGradientStart = "#11998E",
                    albumGradientEnd = "#38EF7D"
                )
            )
            musicDao.insertSongs(presets)
            
            // Seed sample playlist
            val samplePlaylistId = musicDao.insertPlaylist(
                Playlist(name = "Midnight Focus", description = "Relaxing ambient pulses to zone out to.")
            )
            musicDao.addSongToPlaylist(PlaylistSong(samplePlaylistId.toInt(), "ambient_space"))
            musicDao.addSongToPlaylist(PlaylistSong(samplePlaylistId.toInt(), "lofi_rain"))
            musicDao.addSongToPlaylist(PlaylistSong(samplePlaylistId.toInt(), "chill_pulse"))
        }
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return musicDao.insertPlaylist(Playlist(name = name, description = description))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        musicDao.clearPlaylistSongs(playlist.id)
        musicDao.deletePlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Int, songId: String) {
        musicDao.addSongToPlaylist(PlaylistSong(playlistId, songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Int, songId: String) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Int): Flow<List<Song>> {
        return musicDao.getSongsForPlaylist(playlistId)
    }
}
