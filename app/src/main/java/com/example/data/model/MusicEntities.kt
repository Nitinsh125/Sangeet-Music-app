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
    val albumGradientStart: String = "#8A2387",
    val albumGradientEnd: String = "#E94057"
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
