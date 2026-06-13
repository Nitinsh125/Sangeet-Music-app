package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val isFavorite: Boolean = false,
    val isSynthetic: Boolean = true,
    val synthStyle: String = "CHILL_AMBIENT", // CHILL_AMBIENT, RETRO_WAVE, DEEP_SPACE, DOCK_RAIN, SUNSET_GLOW
    val baseFrequency: Float = 220f,
    val streamUrl: String? = null,
    val albumGradientStart: String = "#802240",
    val albumGradientEnd: String = "#FFA0B0",
    // New Advanced Features fields
    val fileFormat: String = "MP3",
    val filePath: String = "/storage/emulated/0/Music",
    val fileSizeMb: Double = 4.5,
    val sampleRate: String = "44.1 kHz, 320 kbps",
    val genre: String = "Ambient",
    val albumArtist: String? = null,
    val lyricsLrc: String? = null
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSong(
    val playlistId: Int,
    val songId: String
)
