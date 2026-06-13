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
                    artist = "Zenith Labs; Aether Bloom",
                    album = "Milkyway Odyssey",
                    durationMs = 180000,
                    isFavorite = true,
                    isSynthetic = true,
                    synthStyle = "DEEP_SPACE",
                    baseFrequency = 110f,
                    albumGradientStart = "#0B0B1E",
                    albumGradientEnd = "#4F105F",
                    fileFormat = "FLAC",
                    filePath = "/storage/emulated/0/Music/Ambient/Deep_Space_Drift.flac",
                    fileSizeMb = 31.4,
                    sampleRate = "24-bit / 96 kHz Hi-Res Lossless",
                    genre = "Space Ambient",
                    albumArtist = "Zenith Labs",
                    lyricsLrc = """
                        [00:00.00] 🌌 (Cosmic hum whispering...)
                        [00:04.00] Floating far past the safety of earth
                        [00:10.00] Solar sails expand into the deep
                        [00:16.00] Look back, the pale blue dot is a memory
                        [00:22.00] Across the dust and glowing nebula bands
                        [00:29.00] Suspended inside our tiny cozy cabin
                        [00:36.00] Weightless dreams fill our quiet minds
                        [00:44.00] Synthesis waves pull us further away
                        [00:52.00] 🎹 (Starlight synthesizer solo...)
                        [01:08.00] Silent and free, we drift into tomorrow
                    """.trimIndent()
                ),
                Song(
                    id = "synth_outrun",
                    title = "Retro Drive",
                    artist = "Vector Horizon feat. Neuro Wave",
                    album = "Outrun 1988",
                    durationMs = 150000,
                    isFavorite = false,
                    isSynthetic = true,
                    synthStyle = "RETRO_WAVE",
                    baseFrequency = 147f,
                    albumGradientStart = "#F80759",
                    albumGradientEnd = "#BC4E9C",
                    fileFormat = "MP3",
                    filePath = "/storage/emulated/0/Music/Retro/Retro_Drive.mp3",
                    fileSizeMb = 5.7,
                    sampleRate = "320 kbps MP3 Extra High Quality",
                    genre = "Synthwave",
                    albumArtist = "Vector Horizon",
                    lyricsLrc = """
                        [00:00.00] 🏎️ (Engine startup and retro beats ignite)
                        [00:06.00] Driving under the neon glowing signs
                        [00:12.00] Speedometer hitting one hundred and twenty
                        [00:18.00] Analog systems pumping on our dashboard
                        [00:24.00] Golden reflections guide this quiet escape
                        [00:30.00] Nostalgic memories of an endless summer
                        [00:36.00] Speeding along the mathematical highway
                        [00:42.00] ⚡ (Pure frequency synth-bass solo!)
                        [00:58.00] Keep the drive alive until the sun breaks
                    """.trimIndent()
                ),
                Song(
                    id = "lofi_rain",
                    title = "Rainy Coffee Spot",
                    artist = "Nostalgia Box & Zenith Labs",
                    album = "Suburban Coffee",
                    durationMs = 210000,
                    isFavorite = true,
                    isSynthetic = true,
                    synthStyle = "DOCK_RAIN",
                    baseFrequency = 165f,
                    albumGradientStart = "#3D7EAA",
                    albumGradientEnd = "#FFE47E",
                    fileFormat = "WAV",
                    filePath = "/storage/emulated/0/Downloads/Rainy_Coffee_Spot.wav",
                    fileSizeMb = 21.2,
                    sampleRate = "16-bit / 44.1 kHz CD Audio WAV",
                    genre = "Lo-Fi Beats",
                    albumArtist = "Nostalgia Box",
                    lyricsLrc = """
                        [00:00.00] 🌧️ (Soft raindrops hitting the glass...)
                        [00:05.00] Warm cup of coffee in a crowded corner
                        [00:11.00] Looking at the foggy windows outside
                        [00:17.00] Slow keys playing to the steady rain drop
                        [00:23.00] Wrapping a blanket round lonely shoulders
                        [00:29.00] Sifting through journals of old static words
                        [00:35.00] Time slows down in this rainy workspace
                        [00:42.00] ☕ (Relaxing lo-fi vinyl warmth cracks...)
                        [00:55.00] Just sit and watch the storm pass by
                    """.trimIndent()
                ),
                Song(
                    id = "ambient_sunset",
                    title = "Warm Horizon",
                    artist = "Aether Bloom, Zenith Labs & Neuro Wave",
                    album = "Ethereal Fields",
                    durationMs = 240000,
                    isFavorite = false,
                    isSynthetic = true,
                    synthStyle = "SUNSET_GLOW",
                    baseFrequency = 220f,
                    albumGradientStart = "#FF512F",
                    albumGradientEnd = "#F09819",
                    fileFormat = "OGG",
                    filePath = "/storage/emulated/0/Music/Ambient/Warm_Horizon.ogg",
                    fileSizeMb = 8.1,
                    sampleRate = "192 kbps OGG Vorbis",
                    genre = "Ambient Chill",
                    albumArtist = "Aether Bloom",
                    lyricsLrc = """
                        [00:00.00] 🌅 (Golden aura pad fading in...)
                        [00:07.00] Sunset painting the meadows purple
                        [00:14.00] Day is ending, the fireflies awaken
                        [00:21.00] Follow the light to the edge of the world
                        [00:28.00] Cool breeze whispers through the grass
                        [00:35.00] Floating above like a cloud in the sky
                        [00:42.00] Dissolving into the orange atmosphere
                        [00:49.00] ✨ (Shimmering chime resonance solo!)
                        [01:05.00] Rest your soul on this warm horizon
                    """.trimIndent()
                ),
                Song(
                    id = "chill_pulse",
                    title = "Binaural Focus Pulse",
                    artist = "Neuro Wave; Zenith Labs",
                    album = "Mind Gym",
                    durationMs = 300000,
                    isFavorite = false,
                    isSynthetic = true,
                    synthStyle = "CHILL_AMBIENT",
                    baseFrequency = 130f,
                    albumGradientStart = "#11998E",
                    albumGradientEnd = "#38EF7D",
                    fileFormat = "AAC",
                    filePath = "/storage/emulated/0/Downloads/Binaural_Focus_Pulse.m4a",
                    fileSizeMb = 6.2,
                    sampleRate = "256 kbps AAC iTunes Quality",
                    genre = "Binaural Beats",
                    albumArtist = "Neuro Wave",
                    lyricsLrc = """
                        [00:00.00] 🧠 (Alpha brainwave frequency entrainment)
                        [00:08.00] Synchronize the left and right thoughts
                        [00:16.00] Drifting into deep mental clarity
                        [00:24.00] Sangeet patterns focus the attention
                        [00:32.00] Let the background clutter fall away
                        [00:40.00] Inhale focus, exhale hesitation
                        [00:48.00] Steady flow of productive energy
                        [00:56.00] (🌌 Soft alpha pulse oscillator solo...)
                    """.trimIndent()
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
