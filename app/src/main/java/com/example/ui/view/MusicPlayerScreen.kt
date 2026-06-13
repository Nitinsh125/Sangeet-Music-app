package com.example.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicViewModel
import java.util.Locale

// Custom Sangeet Theme Design Tokens
val SangeetBg = Color(0xFFFFF7F7)           // Cute, warm cream background
val SangeetPrimary = Color(0xFF813D53)      // Elegant deep rose/plum color
val SangeetAccent = Color(0xFFFAEBEF)       // soft rose card backdrop tint
val SangeetTextDark = Color(0xFF352226)     // deep charcoal/espresso font
val SangeetTextMuted = Color(0xFF8C797C)    // cozy grayish/plum subtext
val SangeetCardBg = Color(0xE6FFFFFF)       // frosted clear white card

// Extension mapper to map Song ID to beautiful High-Quality Artwork photos
fun Song.getCoverUrl(): String {
    return when (this.id) {
        "ambient_space" -> "https://images.unsplash.com/photo-1614850523011-8f49fc9ee67a?q=80&w=400&auto=format&fit=crop" // Purple/celestial gradient wave
        "synth_outrun" -> "https://images.unsplash.com/photo-1542838132-92c53300491e?q=80&w=400&auto=format&fit=crop"  // Neon synth sunrise
        "lofi_rain" -> "https://images.unsplash.com/photo-1518609878373-06d740f60d8b?q=80&w=400&auto=format&fit=crop" // Cozy warm lofi coffee art
        "ambient_sunset" -> "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=400&auto=format&fit=crop" // Sunset warm fields
        "chill_pulse" -> "https://images.unsplash.com/photo-1558591710-4b4a1ae0f04d?q=80&w=400&auto=format&fit=crop" // Waves organic pattern lines
        else -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=400&auto=format&fit=crop" // Default music microphone art
    }
}

// Extension mapper to map Artist name to beautiful illustration portraits
fun String.getArtistUrl(): String {
    return when (this) {
        "Zenith Labs" -> "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=300&auto=format&fit=crop" // Beautiful studio portrait
        "Vector Horizon" -> "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?q=80&w=300&auto=format&fit=crop" // Unknown smiling boy profile from screenshot
        "Nostalgia Box" -> "https://images.unsplash.com/photo-1494790108377-be9c29b29330?q=80&w=300&auto=format&fit=crop" // Cute anime-vibe girl photo
        "Aether Bloom" -> "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?q=80&w=300&auto=format&fit=crop" // Warm smiling boy under sun
        "Neuro Wave" -> "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?q=80&w=300&auto=format&fit=crop" // Cool model collage portrait
        else -> "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=300&auto=format&fit=crop"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = viewModel()
) {
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
    val visibleSongs by viewModel.visibleSongs.collectAsStateWithLifecycle()
    val ignoredFolders by viewModel.ignoredFolders.collectAsStateWithLifecycle()
    val activeDelimiters by viewModel.activeDelimiters.collectAsStateWithLifecycle()
    val isFetchingLyrics by viewModel.isFetchingLyrics.collectAsStateWithLifecycle()
    val favoriteSongs by viewModel.favoriteSongs.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playSpeed by viewModel.playSpeed.collectAsStateWithLifecycle()
    val loopActive by viewModel.loopActive.collectAsStateWithLifecycle()
    val shuffleActive by viewModel.shuffleActive.collectAsStateWithLifecycle()

    val visualizerData by viewModel.visualizerData.collectAsStateWithLifecycle()
    val progressMs by viewModel.playbackProgressMs.collectAsStateWithLifecycle()
    val activePlaylistId by viewModel.activePlaylistId.collectAsStateWithLifecycle()
    val activePlaylistSongs by viewModel.activePlaylistSongs.collectAsStateWithLifecycle()

    // Screen navigation layout: 0 = Home ("Your Mix"), 1 = Search, 2 = Library
    var selectedBottomTab by remember { mutableStateOf(0) }
    // Library lists option chip filters: 0 = SONGS, 1 = ALBUMS, 2 = ARTIST, 3 = PLAYLISTS
    var selectedLibraryFilter by remember { mutableStateOf(1) } // Default to ALBUMS as in Image 2!

    var searchQuery by remember { mutableStateOf("") }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf<Song?>(null) }
    var showNowPlayingSheet by remember { mutableStateOf(false) }

    // Vinyl Rotation Transition
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl_rotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_angle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SangeetBg)
    ) {
        // Soft peach and lavender glowing spots
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius1 = (size.minDimension * 0.95f).coerceAtLeast(1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFDADE), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.18f),
                    radius = radius1
                ),
                center = Offset(size.width * 0.15f, size.height * 0.18f),
                radius = radius1
            )
            val radius2 = (size.minDimension * 0.95f).coerceAtLeast(1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE4E6FF), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.82f),
                    radius = radius2
                ),
                center = Offset(size.width * 0.85f, size.height * 0.82f),
                radius = radius2
            )
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            bottomBar = {
                SangeetBottomNavigation(
                    selectedTab = selectedBottomTab,
                    onTabSelected = { selectedBottomTab = it }
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // main scrollable area depending on selected navigation tab
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    when (selectedBottomTab) {
                        0 -> {
                            // --- HOME / YOUR MIX VIEW (Image 3) ---
                            HomeYourMixSection(
                                allSongs = allSongs,
                                onSongClick = { viewModel.playSong(it) },
                                onShuffleClick = {
                                    viewModel.toggleShuffle()
                                    viewModel.playNext()
                                }
                            )
                        }
                        1 -> {
                            // --- SEARCH SECTION ---
                            SearchSection(
                                allSongs = visibleSongs,
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                onSongClick = { viewModel.playSong(it) },
                                currentSong = currentSong,
                                onAddPlaylistClick = { showAddToPlaylistDialog = it }
                            )
                        }
                        2 -> {
                            // --- LIBRARY SECTION (Image 2) ---
                            LibrarySection(
                                allSongs = visibleSongs,
                                playlists = playlists,
                                currentSong = currentSong,
                                activePlaylistId = activePlaylistId,
                                selectPlaylist = { viewModel.selectPlaylist(it) },
                                deletePlaylist = { viewModel.deletePlaylist(it) },
                                onCreatePlaylistClick = { showCreatePlaylistDialog = true },
                                onSongClick = { viewModel.playSong(it) },
                                selectedFilter = selectedLibraryFilter,
                                onFilterSelected = { selectedLibraryFilter = it },
                                onAddPlaylistClick = { showAddToPlaylistDialog = it },
                                onShuffleClick = {
                                    viewModel.toggleShuffle()
                                    viewModel.playNext()
                                },
                                ignoredFolders = ignoredFolders,
                                onToggleFolderFilter = { viewModel.toggleFolderFilter(it) },
                                activeDelimiters = activeDelimiters,
                                onToggleDelimiter = { viewModel.toggleDelimiter(it) }
                            )
                        }
                    }
                    
                    // Spacer for mini-player overlap
                    Spacer(modifier = Modifier.height(84.dp))
                }

                // Floating Mini-Player Pill above navigation bar (Image 2 style)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    MiniPlayer(
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onNextClick = { viewModel.playNext() },
                        onPrevClick = { viewModel.playPrevious() },
                        onExpandClick = { showNowPlayingSheet = true }
                    )
                }
            }
        }

        // --- FULL-EXPANDED NOW PLAYING SHEET ---
        if (showNowPlayingSheet) {
            NowPlayingSheet(
                currentSong = currentSong,
                isPlaying = isPlaying,
                progressMs = progressMs,
                playSpeed = playSpeed,
                loopActive = loopActive,
                shuffleActive = shuffleActive,
                visualizerData = visualizerData,
                rotationAngle = rotationAngle,
                onDismiss = { showNowPlayingSheet = false },
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onNextClick = { viewModel.playNext() },
                onPrevClick = { viewModel.playPrevious() },
                onShuffleToggle = { viewModel.toggleShuffle() },
                onLoopToggle = { viewModel.toggleLoop() },
                onFavoriteToggle = { currentSong?.let { viewModel.toggleFavorite(it) } },
                onPitchChange = { viewModel.customizeSynth(it) },
                onSpeedChange = { viewModel.setSpeed(it) },
                onSeekClick = { viewModel.seekTo(it) },
                onSaveLyrics = { id, text -> viewModel.saveLyrics(id, text) },
                onSyncLyrics = { viewModel.syncLyricsFromLrclib(it) },
                isFetchingLyrics = isFetchingLyrics
            )
        }

        // --- SECTOR DIALOGS ---
        if (showCreatePlaylistDialog) {
            var playlistName by remember { mutableStateOf("") }
            var playlistDesc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreatePlaylistDialog = false },
                containerColor = SangeetCardBg,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Create Sangeet Playlist", color = SangeetTextDark, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = playlistName,
                            onValueChange = { playlistName = it },
                            label = { Text("Playlist Name", color = SangeetTextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SangeetTextDark,
                                unfocusedTextColor = SangeetTextDark,
                                focusedBorderColor = SangeetPrimary,
                                unfocusedBorderColor = SangeetTextMuted
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = playlistDesc,
                            onValueChange = { playlistDesc = it },
                            label = { Text("Short Notes (optional)", color = SangeetTextMuted) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SangeetTextDark,
                                unfocusedTextColor = SangeetTextDark,
                                focusedBorderColor = SangeetPrimary,
                                unfocusedBorderColor = SangeetTextMuted
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (playlistName.isNotBlank()) {
                                viewModel.createPlaylist(playlistName, playlistDesc)
                                showCreatePlaylistDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SangeetPrimary, contentColor = Color.White)
                    ) {
                        Text("Confirm", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreatePlaylistDialog = false }) {
                        Text("Cancel", color = SangeetTextMuted)
                    }
                }
            )
        }

        showAddToPlaylistDialog?.let { targetSong ->
            AlertDialog(
                onDismissRequest = { showAddToPlaylistDialog = null },
                containerColor = SangeetCardBg,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Add to Sangeet Playlist", color = SangeetTextDark, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    if (playlists.isEmpty()) {
                        Text("You don't have Sangeet playlists yet. Create one in the Library tab!", color = SangeetTextMuted)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(playlists) { p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(SangeetBg)
                                        .clickable {
                                            viewModel.addSongToPlaylist(p.id, targetSong.id)
                                            showAddToPlaylistDialog = null
                                        }
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = p.name, color = SangeetTextDark, fontWeight = FontWeight.SemiBold)
                                    Icon(Icons.Default.Add, contentDescription = "Add song", tint = SangeetPrimary)
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showAddToPlaylistDialog = null }) {
                        Text("Close", color = SangeetTextMuted)
                    }
                }
            )
        }
    }
}

// ================= COMPOSABLE PAGES & SECTIONS =================

@Composable
fun SangeetBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xF2FFF4F6), // soft, cozy, blurred peach white
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color(0xFFFDE4E9), shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
    ) {
        val items = listOf(
            Triple(0, "Home", Icons.Default.Home),
            Triple(1, "Search", Icons.Default.Search),
            Triple(2, "Library", Icons.Default.LibraryMusic)
        )
        items.forEach { (index, title, icon) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) SangeetPrimary else SangeetTextMuted
                    )
                },
                label = {
                    Text(
                        text = title,
                        color = if (isSelected) SangeetTextDark else SangeetTextMuted,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFFF8DFE4)
                )
            )
        }
    }
}

@Composable
fun HomeYourMixSection(
    allSongs: List<Song>,
    onSongClick: (Song) -> Unit,
    onShuffleClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Beta Pill on Top-Left
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFAEBEF))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "β Beta",
                        color = SangeetPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action Bar Top-Right Icons
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.CloudQueue, contentDescription = "Sync", tint = SangeetTextDark)
                    }
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = SangeetTextDark)
                    }
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SangeetTextDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main "Your Mix" typography
            Text(
                text = "Your\nMix",
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = SangeetTextDark,
                lineHeight = 54.sp,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Today's Mix for you",
                fontSize = 14.sp,
                color = SangeetTextMuted,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Organic visual collage (Blob-like bubble cluster coordinates) (Image 3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Outer glowing blurred halos under central blob
                Canvas(modifier = Modifier.size(240.dp)) {
                    val rHome = (size.minDimension * 0.48f).coerceAtLeast(1f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFDFE3), Color.Transparent),
                            radius = rHome
                        ),
                        radius = rHome
                    )
                }

                // 1. Center Main Hexagonal Cover
                val centerSong = allSongs.getOrNull(0)
                centerSong?.let { song ->
                    Box(
                        modifier = Modifier
                            .size(165.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White)
                            .border(6.dp, Color(0xFFFAEBEF), RoundedCornerShape(32.dp))
                            .clickable { onSongClick(song) },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = song.getCoverUrl(),
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 2. High Right bubble
                val rSong = allSongs.getOrNull(1)
                rSong?.let { song ->
                    Box(
                        modifier = Modifier
                            .offset(x = 100.dp, y = (-95).dp)
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(3.dp, Color(0xFFFFF1F3), CircleShape)
                            .clickable { onSongClick(song) }
                    ) {
                        AsyncImage(
                            model = song.getCoverUrl(),
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 3. Mid Left bubble
                val lSong = allSongs.getOrNull(2)
                lSong?.let { song ->
                    Box(
                        modifier = Modifier
                            .offset(x = (-110).dp, y = 45.dp)
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.5.dp, Color(0xFFFFF1F3), CircleShape)
                            .clickable { onSongClick(song) }
                    ) {
                        AsyncImage(
                            model = song.getCoverUrl(),
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 4. Low Right bubble
                val bSong = allSongs.getOrNull(3)
                bSong?.let { song ->
                    Box(
                        modifier = Modifier
                            .offset(x = 85.dp, y = 105.dp)
                            .size(105.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color(0xFFFFF1F3), CircleShape)
                            .clickable { onSongClick(song) }
                    ) {
                        AsyncImage(
                            model = song.getCoverUrl(),
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 5. High Left small bubble
                val hlSong = allSongs.getOrNull(4)
                hlSong?.let { song ->
                    Box(
                        modifier = Modifier
                            .offset(x = (-95).dp, y = (-80).dp)
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(2.dp, Color(0xFFFFF1F3), CircleShape)
                            .clickable { onSongClick(song) }
                    ) {
                        AsyncImage(
                            model = song.getCoverUrl(),
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Floating collage Quick Shuffle FAB
        FloatingActionButton(
            onClick = onShuffleClick,
            containerColor = SangeetPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 4.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle Collage",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SearchSection(
    allSongs: List<Song>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSongClick: (Song) -> Unit,
    currentSong: Song?,
    onAddPlaylistClick: (Song) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Search",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = SangeetTextDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Glassmorphic peach-toned search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search songs, artists, and sound styles...", color = SangeetTextMuted, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = SangeetPrimary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                focusedBorderColor = SangeetPrimary,
                unfocusedBorderColor = Color(0xFFFDE4E9),
                focusedTextColor = SangeetTextDark,
                unfocusedTextColor = SangeetTextDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        val filteredSongs = allSongs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.artist.contains(searchQuery, ignoreCase = true) ||
            it.synthStyle.contains(searchQuery, ignoreCase = true)
        }

        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No tracks found in the Sangeet database.",
                    color = SangeetTextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSongs) { song ->
                    val isCurrent = currentSong?.id == song.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isCurrent) Color(0xFFFFF1F3) else Color.White.copy(alpha = 0.4f))
                            .border(1.dp, Color(0xFFFFF1F3), RoundedCornerShape(16.dp))
                            .clickable { onSongClick(song) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.getCoverUrl(),
                            contentDescription = "artwork",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                color = SangeetTextDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${song.artist} • ${song.album}",
                                color = SangeetTextMuted,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onAddPlaylistClick(song) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = SangeetPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibrarySection(
    allSongs: List<Song>,
    playlists: List<Playlist>,
    currentSong: Song?,
    activePlaylistId: Int?,
    selectPlaylist: (Int?) -> Unit,
    deletePlaylist: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    selectedFilter: Int,
    onFilterSelected: (Int) -> Unit,
    onAddPlaylistClick: (Song) -> Unit,
    onShuffleClick: () -> Unit,
    // Dynamic features states
    ignoredFolders: Set<String>,
    onToggleFolderFilter: (String) -> Unit,
    activeDelimiters: List<String>,
    onToggleDelimiter: (String) -> Unit
) {
    var showScanFilterDialog by remember { mutableStateOf(false) }
    var showDelimDialog by remember { mutableStateOf(false) }
    
    // Sub-view dialog state when selecting a specific group
    var selectedGenreName by remember { mutableStateOf<String?>(null) }
    var selectedFolderName by remember { mutableStateOf<String?>(null) }
    var selectedArtistName by remember { mutableStateOf<String?>(null) }
    var selectedAlbumName by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Upper Title block (Library + Configuration Cogs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = SangeetTextDark
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Toggler for smart delimiters
                IconButton(
                    onClick = { showDelimDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFAEBEF), CircleShape)
                ) {
                    Icon(Icons.Default.ManageAccounts, contentDescription = "Artist Delim Config", tint = SangeetPrimary, modifier = Modifier.size(18.dp))
                }
                
                // Toggler for foldered scan filtering
                IconButton(
                    onClick = { showScanFilterDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFFAEBEF), CircleShape)
                ) {
                    Icon(Icons.Default.FolderSpecial, contentDescription = "Scanned Folders Config", tint = SangeetPrimary, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Horizontal scrollable filter chips selection (SONGS | ALBUMS | ARTIST | GENRES | FOLDERS | PLAYLISTS)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val filters = listOf("SONGS", "ALBUMS", "ARTIST", "GENRES", "FOLDERS", "PLAYLISTS")
            filters.forEachIndexed { idx, label ->
                val isActive = selectedFilter == idx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isActive) SangeetPrimary else Color(0xFFFAEBEF))
                        .clickable { onFilterSelected(idx) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isActive) Color.White else SangeetPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Secondary control Row (Shuffle button + Status details)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFAEBEF))
                    .clickable { onShuffleClick() }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = SangeetPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Shuffle All",
                    color = SangeetPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${allSongs.size} Tracks Loaded",
                fontSize = 12.sp,
                color = SangeetTextMuted,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Grid/List Contents box based on Chip choice
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            when (selectedFilter) {
                0 -> {
                    // SONGS LIST VIEW WITH AUDIO SPEC BADGES
                    if (allSongs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tracks available inside active scanned folders.", color = SangeetTextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(allSongs) { song ->
                                val isCurrent = currentSong?.id == song.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isCurrent) Color(0xFFFFF1F3) else Color.White.copy(alpha = 0.4f))
                                        .border(1.dp, Color(0xFFFFF1F3), RoundedCornerShape(14.dp))
                                        .clickable { onSongClick(song) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = song.getCoverUrl(),
                                        contentDescription = "cover",
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = song.title,
                                                color = if (isCurrent) SangeetPrimary else SangeetTextDark,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            // Glowing audio format key badge (MP3, FLAC, WAV, AAC, OGG)
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (song.fileFormat == "FLAC" || song.fileFormat == "WAV") Color(0xFF432A3E)
                                                        else SangeetPrimary.copy(alpha = 0.1f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = song.fileFormat,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (song.fileFormat == "FLAC" || song.fileFormat == "WAV") Color(0xFFFFDADE) else SangeetPrimary
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${song.artist} • ${song.sampleRate}",
                                            color = SangeetTextMuted,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = formatDuration(song.durationMs),
                                        color = SangeetTextMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(onClick = { onAddPlaylistClick(song) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add track", tint = SangeetPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // ALBUMS COHESIVE GROUPING (Album Artist integration)
                    val albumsMap = remember(allSongs) {
                        allSongs.groupBy { it.album }
                    }
                    if (albumsMap.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No albums available.", color = SangeetTextMuted)
                        }
                    } else {
                        val albumsList = albumsMap.toList()
                        val chunkedAlbums = albumsList.chunked(2)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(chunkedAlbums) { pair ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // First Album
                                    val (albumTitle1, songsInAlbum1) = pair[0]
                                    Box(modifier = Modifier.weight(1f)) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(6.dp)
                                                .clip(RoundedCornerShape(24.dp))
                                                .background(Color(0xFFFAEBEF))
                                                .clickable { selectedAlbumName = albumTitle1 }
                                                .padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            AsyncImage(
                                                model = songsInAlbum1.first().getCoverUrl(),
                                                contentDescription = albumTitle1,
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = albumTitle1,
                                                fontWeight = FontWeight.Bold,
                                                color = SangeetTextDark,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            // Show smart album creator grouping
                                            val creator = songsInAlbum1.first().albumArtist ?: songsInAlbum1.first().artist.split(";").first().trim()
                                            Text(
                                                text = "$creator\n${songsInAlbum1.size} Trails",
                                                color = SangeetTextMuted,
                                                fontSize = 11.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 14.sp,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                    
                                    // Second Album
                                    if (pair.size > 1) {
                                        val (albumTitle2, songsInAlbum2) = pair[1]
                                        Box(modifier = Modifier.weight(1f)) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(6.dp)
                                                    .clip(RoundedCornerShape(24.dp))
                                                    .background(Color(0xFFFAEBEF))
                                                    .clickable { selectedAlbumName = albumTitle2 }
                                                    .padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                AsyncImage(
                                                    model = songsInAlbum2.first().getCoverUrl(),
                                                    contentDescription = albumTitle2,
                                                    modifier = Modifier
                                                        .size(120.dp)
                                                        .clip(RoundedCornerShape(16.dp))
                                                )
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = albumTitle2,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SangeetTextDark,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                val creator = songsInAlbum2.first().albumArtist ?: songsInAlbum2.first().artist.split(";").first().trim()
                                                Text(
                                                    text = "$creator\n${songsInAlbum2.size} Trails",
                                                    color = SangeetTextMuted,
                                                    fontSize = 11.sp,
                                                    textAlign = TextAlign.Center,
                                                    lineHeight = 14.sp,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // ARTIST MULTI-ARTIST SMART PARSING VIEW
                    val parsedArtistsMap = remember(allSongs, activeDelimiters) {
                        val map = mutableMapOf<String, MutableList<Song>>()
                        for (song in allSongs) {
                            var splitList = listOf(song.artist)
                            for (delim in activeDelimiters) {
                                splitList = splitList.flatMap { textSegment ->
                                    if (textSegment.contains(delim)) {
                                        textSegment.split(delim)
                                    } else {
                                        listOf(textSegment)
                                    }
                                }
                            }
                            for (art in splitList) {
                                val cleanArt = art.trim()
                                if (cleanArt.isNotEmpty()) {
                                    map.getOrPut(cleanArt) { mutableListOf() }.add(song)
                                }
                            }
                        }
                        map.toList().sortedBy { it.first }
                    }

                    if (parsedArtistsMap.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No artists parsed.", color = SangeetTextMuted)
                        }
                    } else {
                        val chunkedArtists = parsedArtistsMap.chunked(2)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(chunkedArtists) { pair ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    // First Artist
                                    val (artistName1, songsIn1) = pair[0]
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(6.dp)
                                            .clickable { selectedArtistName = artistName1 },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            // Avatar Circle with Initials
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        Brush.linearGradient(
                                                            colors = listOf(SangeetPrimary, Color(0xFFFFDADE))
                                                        )
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = artistName1.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = artistName1,
                                                color = SangeetTextDark,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${songsIn1.size} Collaborations",
                                                color = SangeetTextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Second Artist
                                    if (pair.size > 1) {
                                        val (artistName2, songsIn2) = pair[1]
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(6.dp)
                                                .clickable { selectedArtistName = artistName2 },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(100.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(SangeetPrimary, Color(0xFFFFDADE))
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = artistName2.take(1).uppercase(),
                                                        color = Color.White,
                                                        fontSize = 32.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = artistName2,
                                                    color = SangeetTextDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${songsIn2.size} Collaborations",
                                                    color = SangeetTextMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // GENRES Filter
                    val genreGroups = remember(allSongs) {
                        allSongs.groupBy { it.genre }
                    }
                    if (genreGroups.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No genres available.", color = SangeetTextMuted)
                        }
                    } else {
                        val genreList = genreGroups.toList()
                        val chunkedGenres = genreList.chunked(2)
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(chunkedGenres) { pair ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    val (genre1, songs1) = pair[0]
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFFFAEBEF), Color(0xFFFFF5F7))
                                                )
                                            )
                                            .clickable { selectedGenreName = genre1 }
                                            .padding(16.dp)
                                    ) {
                                        Column {
                                            Icon(Icons.Default.MusicNote, contentDescription = "Genre", tint = SangeetPrimary, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Text(text = genre1, fontWeight = FontWeight.Bold, color = SangeetTextDark, fontSize = 15.sp)
                                            Text(text = "${songs1.size} Songs", color = SangeetTextMuted, fontSize = 12.sp)
                                        }
                                    }

                                    if (pair.size > 1) {
                                        val (genre2, songs2) = pair[1]
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(6.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(
                                                    Brush.linearGradient(
                                                        colors = listOf(Color(0xFFFAEBEF), Color(0xFFFFF5F7))
                                                    )
                                                )
                                                .clickable { selectedGenreName = genre2 }
                                                .padding(16.dp)
                                        ) {
                                            Column {
                                                Icon(Icons.Default.MusicNote, contentDescription = "Genre", tint = SangeetPrimary, modifier = Modifier.size(24.dp))
                                                Spacer(modifier = Modifier.height(14.dp))
                                                Text(text = genre2, fontWeight = FontWeight.Bold, color = SangeetTextDark, fontSize = 15.sp)
                                                Text(text = "${songs2.size} Songs", color = SangeetTextMuted, fontSize = 12.sp)
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // FOLDERS Filter (Physical directory grouping)
                    val folderGroups = remember(allSongs) {
                        allSongs.groupBy { song ->
                            song.filePath.substringBeforeLast("/")
                        }
                    }
                    if (folderGroups.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No active directories. Enable directories in the gear icon above.", color = SangeetTextMuted, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            folderGroups.forEach { (dirPath, songsInDir) ->
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFFAEBEF))
                                            .clickable { selectedFolderName = dirPath }
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Default.Folder, contentDescription = "folder", tint = SangeetPrimary, modifier = Modifier.size(32.dp))
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    text = dirPath.substringAfterLast("/"),
                                                    fontWeight = FontWeight.Bold,
                                                    color = SangeetTextDark,
                                                    fontSize = 15.sp
                                                )
                                                Text(
                                                    text = dirPath,
                                                    color = SangeetTextMuted,
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(SangeetPrimary.copy(alpha = 0.15f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${songsInDir.size} loaded",
                                                color = SangeetPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                5 -> {
                    // PLAYLISTS VIEW WITH CUSTOM ADD PORTAL
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Sangeet Playlists",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SangeetTextDark
                            )
                            Button(
                                onClick = onCreatePlaylistClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SangeetPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("+ New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (playlists.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No playlists. Tap '+ New' to start Sangeet journeys!",
                                    color = SangeetTextMuted,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 16.dp)
                            ) {
                                items(playlists) { playlist ->
                                    val isSelected = activePlaylistId == playlist.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) Color(0xFFFFF1F3) else Color.White.copy(alpha = 0.4f))
                                            .border(1.dp, Color(0xFFFFF1F3), RoundedCornerShape(16.dp))
                                            .clickable { selectPlaylist(if (isSelected) null else playlist.id) }
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = "folder",
                                                tint = SangeetPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = playlist.name,
                                                    color = SangeetTextDark,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                if (playlist.description.isNotEmpty()) {
                                                    Text(
                                                        text = playlist.description,
                                                        color = SangeetTextMuted,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isSelected) "Active" else "Queue",
                                                fontSize = 11.sp,
                                                color = SangeetPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                            IconButton(
                                                onClick = { deletePlaylist(playlist) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "delete",
                                                    tint = Color.Red.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // LISTINGS POPUP FOR GENRES SELECTOR
    selectedGenreName?.let { genreTitle ->
        val genreSongs = allSongs.filter { it.genre == genreTitle }
        AlertDialog(
            onDismissRequest = { selectedGenreName = null },
            title = { Text(text = "$genreTitle Genre Tracks", fontWeight = FontWeight.Bold, color = SangeetTextDark) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    items(genreSongs) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSongClick(song)
                                    selectedGenreName = null
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = SangeetPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = song.title, fontWeight = FontWeight.Bold, color = SangeetTextDark, fontSize = 14.sp)
                                Text(text = song.artist, color = SangeetTextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedGenreName = null }, colors = ButtonDefaults.buttonColors(containerColor = SangeetPrimary)) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // LISTINGS POPUP FOR FOLDERS SELECTOR
    selectedFolderName?.let { folderPath ->
        val folderSongs = allSongs.filter { it.filePath.substringBeforeLast("/") == folderPath }
        AlertDialog(
            onDismissRequest = { selectedFolderName = null },
            title = { Text(text = folderPath.substringAfterLast("/"), fontWeight = FontWeight.Bold, color = SangeetTextDark) },
            text = {
                Column {
                    Text(text = "Directory: $folderPath", fontSize = 11.sp, color = SangeetTextMuted)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 350.dp)
                    ) {
                        items(folderSongs) { song ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSongClick(song)
                                        selectedFolderName = null
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = SangeetPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = song.title, fontWeight = FontWeight.Bold, color = SangeetTextDark, fontSize = 14.sp)
                                    Text(text = song.filePath.substringAfterLast("/"), color = SangeetTextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedFolderName = null }, colors = ButtonDefaults.buttonColors(containerColor = SangeetPrimary)) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // LISTINGS POPUP FOR MULTI-ARTIST COLLABORATORS
    selectedArtistName?.let { artName ->
        val artistSongs = allSongs.filter { song ->
            var list = listOf(song.artist)
            for (delim in activeDelimiters) {
                list = list.flatMap { part -> if (part.contains(delim)) part.split(delim) else listOf(part) }
            }
            list.map { it.trim().lowercase() }.contains(artName.lowercase())
        }
        AlertDialog(
            onDismissRequest = { selectedArtistName = null },
            title = { Text(text = "Tracks for $artName", fontWeight = FontWeight.Bold, color = SangeetTextDark) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    items(artistSongs) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSongClick(song)
                                    selectedArtistName = null
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayCircleFilled, contentDescription = "Play", tint = SangeetPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = song.title, fontWeight = FontWeight.Bold, color = SangeetTextDark, fontSize = 14.sp)
                                Text(text = "${song.artist} • ${song.album}", color = SangeetTextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedArtistName = null }, colors = ButtonDefaults.buttonColors(containerColor = SangeetPrimary)) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // LISTINGS POPUP FOR ALBUMS
    selectedAlbumName?.let { albName ->
        val albumSongs = allSongs.filter { it.album == albName }
        AlertDialog(
            onDismissRequest = { selectedAlbumName = null },
            title = { Text(text = albName, fontWeight = FontWeight.Bold, color = SangeetTextDark) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 350.dp)
                ) {
                    items(albumSongs) { song ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSongClick(song)
                                    selectedAlbumName = null
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = "Play", tint = SangeetPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = song.title, fontWeight = FontWeight.Bold, color = SangeetTextDark, fontSize = 14.sp)
                                Text(text = if (song.albumArtist != null) "Album Artist: ${song.albumArtist}" else "Artist: ${song.artist}", color = SangeetTextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedAlbumName = null }, colors = ButtonDefaults.buttonColors(containerColor = SangeetPrimary)) {
                    Text("Close", color = Color.White)
                }
            }
        )
    }

    // SWITCH DIRECTORIES SCAN CONFIGURATION DIALOG
    if (showScanFilterDialog) {
        val staticFolders = listOf(
            "/storage/emulated/0/Music/Ambient",
            "/storage/emulated/0/Music/Retro",
            "/storage/emulated/0/Downloads"
        )
        AlertDialog(
            onDismissRequest = { showScanFilterDialog = false },
            title = { Text(text = "Choose Scan Directories", fontWeight = FontWeight.Bold, color = SangeetTextDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Sangeet only imports audio tracks located in enabled directories.", fontSize = 12.sp, color = SangeetTextMuted)
                    staticFolders.forEach { folder ->
                        val isScanned = folder !in ignoredFolders
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleFolderFilter(folder) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = "folder", tint = SangeetPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = folder.substringAfterLast("/"), fontWeight = FontWeight.Bold, color = SangeetTextDark, fontSize = 14.sp)
                                    Text(text = folder, color = SangeetTextMuted, fontSize = 11.sp)
                                }
                            }
                            Switch(
                                checked = isScanned,
                                onCheckedChange = { onToggleFolderFilter(folder) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SangeetPrimary)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showScanFilterDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SangeetPrimary)) {
                    Text("Apply & Scan", color = Color.White)
                }
            }
        )
    }

    // DELIMITERS CONFIGURATION DIALOG
    if (showDelimDialog) {
        val delimiters = listOf(";", "&", "feat.", ",")
        AlertDialog(
            onDismissRequest = { showDelimDialog = false },
            title = { Text(text = "Smart Artist Splitters", fontWeight = FontWeight.Bold, color = SangeetTextDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Configure which tokens split multi-artist tracks into isolated artist cards dynamically.", fontSize = 12.sp, color = SangeetTextMuted)
                    delimiters.forEach { delim ->
                        val isEnabled = delim in activeDelimiters
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleDelimiter(delim) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "' $delim '", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SangeetTextDark)
                            Checkbox(
                                checked = isEnabled,
                                onCheckedChange = { onToggleDelimiter(delim) },
                                colors = CheckboxDefaults.colors(checkedColor = SangeetPrimary)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDelimDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = SangeetPrimary)) {
                    Text("Save", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun AlbumGridCell(song: Song, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFFAEBEF)) // light pastel card backdrop matching Image 2
            .clickable { onClick() }
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = song.getCoverUrl(),
                contentDescription = song.album,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = song.title,
                color = SangeetTextDark,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            Text(
                text = "${song.artist}\n1 Song",
                color = SangeetTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun ArtistGridCell(song: Song, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 6.dp)
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = song.artist.getArtistUrl(),
            contentDescription = song.artist,
            modifier = Modifier
                .size(126.dp)
                .clip(CircleShape)
                .border(4.dp, Color(0xFFFAEBEF), CircleShape) // Light pastel circle border matching Image 1
                .background(Color.White)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = song.artist,
            color = SangeetTextDark,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "1 album • 1 song",
            color = SangeetTextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MiniPlayer(
    currentSong: Song?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onExpandClick: () -> Unit
) {
    if (currentSong == null) return
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(Color(0xFFFAEBEF)) // Custom Pink/Cream cozy backdrop
            .border(width = 1.dp, color = Color(0x33FFFFFF), shape = RoundedCornerShape(36.dp))
            .clickable { onExpandClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = currentSong.getCoverUrl(),
                    contentDescription = "Cover Image",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = currentSong.title,
                        color = SangeetTextDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentSong.artist,
                        color = SangeetTextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Player control action buttons (Previous, Play/Pause, Next)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onPrevClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFFFF1F3), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Song",
                        tint = SangeetPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(SangeetPrimary, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFFFF1F3), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Song",
                        tint = SangeetPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ================= NOW PLAYING EXPANDED VIEW SHEET =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSheet(
    currentSong: Song?,
    isPlaying: Boolean,
    progressMs: Long,
    playSpeed: Float,
    loopActive: Boolean,
    shuffleActive: Boolean,
    visualizerData: FloatArray,
    rotationAngle: Float,
    onDismiss: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onShuffleToggle: () -> Unit,
    onLoopToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onPitchChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSeekClick: (Long) -> Unit,
    onSaveLyrics: (String, String) -> Unit,
    onSyncLyrics: (Song) -> Unit,
    isFetchingLyrics: Boolean
) {
    if (currentSong == null) return

    var showLyrics by remember { mutableStateOf(false) }
    var showLyricsEditor by remember { mutableStateOf(false) }
    var lyricsEditValue by remember(currentSong.lyricsLrc) { mutableStateOf(currentSong.lyricsLrc ?: "") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1518).copy(alpha = 0.96f)) // Ambient dark player screen
            .clickable(enabled = false) {}
    ) {
        val sheetScrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(sheetScrollState, enabled = !showLyrics)
                .padding(horizontal = 24.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Action controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close player", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                
                // Double Pill Tab Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "PLAYER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!showLyrics) Color(0xFFFFDADE) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!showLyrics) Color.White.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { showLyrics = false }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                    Text(
                        text = "LYRICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (showLyrics) Color(0xFFFFDADE) else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (showLyrics) Color.White.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { showLyrics = true }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (currentSong.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (currentSong.isFavorite) Color(0xFFE890A6) else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (showLyrics) {
                // HIGH ACCURACY SCROLLING LYRICS AND NETWORK SYNC COLUMN
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .padding(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Synchronized Lyrics",
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (isFetchingLyrics) {
                                Spacer(modifier = Modifier.width(8.dp))
                                CircularProgressIndicator(color = Color(0xFFFFDADE), strokeWidth = 1.5.dp, modifier = Modifier.size(12.dp))
                            }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconButton(
                                onClick = { onSyncLyrics(currentSong) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = "Query LRCLIB", tint = Color(0xFFFFDADE), modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { showLyricsEditor = true },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Modify timed lyrics", tint = Color(0xFFFFDADE), modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    val rawLrc = currentSong.lyricsLrc ?: ""
                    val lrcLines = remember(rawLrc) { parseLrc(rawLrc) }

                    if (lrcLines.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No Timed Lyrics Added",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { onSyncLyrics(currentSong) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("✨ Click here to fetch from LRCLIB", color = Color(0xFFFFDADE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showLyricsEditor = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("✍️ Paste or edit LRC", color = Color(0xFFFFDADE), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        val activeLineIdx = lrcLines.indexOfLast { it.first <= progressMs }.coerceAtLeast(0)
                        val listState = rememberLazyListState()

                        LaunchedEffect(activeLineIdx) {
                            if (lrcLines.isNotEmpty()) {
                                listState.animateScrollToItem(activeLineIdx, scrollOffset = -80)
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 40.dp)
                        ) {
                            items(lrcLines.size) { idx ->
                                val line = lrcLines[idx]
                                val isActive = idx == activeLineIdx
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSeekClick(line.first) }
                                ) {
                                    Text(
                                        text = line.second,
                                        color = if (isActive) Color(0xFFFFDADE) else Color.White.copy(alpha = 0.35f),
                                        fontSize = if (isActive) 19.sp else 15.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // 1. Rotating Vinyl container
                Box(
                    modifier = Modifier
                        .size(230.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF813D53), Color(0xFFFFDADE), Color(0xFFD0BCFF))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(32.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.Black.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // rotating vinyl
                        val currentRotation = if (isPlaying) rotationAngle else 0f
                        Box(
                            modifier = Modifier
                                .size(175.dp)
                                .rotate(currentRotation),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val center = Offset(size.width / 2, size.height / 2)
                                val radius = size.minDimension / 2
                                // vinyl black records
                                drawCircle(color = Color(0xFF141416), radius = radius)
                                // grooves
                                for (r in 3..14) {
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.04f),
                                        radius = radius * (r / 15f),
                                        style = Stroke(width = 1f)
                                    )
                                }
                                // label
                                drawCircle(color = Color(0xFF813D53), radius = radius * 0.4f)
                            }

                            // central gradient label cover
                            val gradColors = try {
                                listOf(
                                    Color(android.graphics.Color.parseColor(currentSong.albumGradientStart)),
                                    Color(android.graphics.Color.parseColor(currentSong.albumGradientEnd))
                                )
                            } catch (e: Exception) {
                                listOf(Color(0xFF813D53), Color(0xFFFFDADE))
                            }
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(colors = gradColors))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF141416))
                                        .border(1.dp, Color.LightGray, CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Real-time Neon Equalizer/Visualizer
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .padding(horizontal = 24.dp)
                ) {
                    val barWidth = size.width / 16f
                    val gap = 6f
                    for (i in 0 until 16) {
                        val coef = if (i < visualizerData.size) visualizerData[i] else 0.15f
                        val barHeight = (size.height * coef).coerceAtLeast(4f)
                        val startX = i * barWidth + gap / 2
                        val startY = size.height - barHeight
                        val w = barWidth - gap
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFDADE), Color(0xFF813D53)),
                                startY = startY,
                                endY = size.height
                            ),
                            topLeft = Offset(startX, startY),
                            size = Size(w, barHeight),
                            cornerRadius = CornerRadius(w / 2, w / 2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Metadata Title + Subtitle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentSong.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${currentSong.artist} • ${currentSong.album}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Progress Seekbar with duration stamps
            val progressRatio = if (currentSong.durationMs > 0) progressMs.toFloat() / currentSong.durationMs else 0f
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = progressRatio,
                    onValueChange = {}, // Read-only synthesizer time slider
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.fillMaxWidth().height(16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = formatDuration(progressMs), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    Text(text = formatDuration(currentSong.durationMs), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Media player primary actions deck
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShuffleToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (shuffleActive) Color.White.copy(alpha = 0.15f) else Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = if (shuffleActive) Color(0xFFFFDADE) else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = onPrevClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(22.dp))
                }

                // Core Round Play/Pause Deck
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onPlayPauseClick() }
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle",
                        tint = Color(0xFF1E1518),
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(22.dp))
                }

                IconButton(
                    onClick = onLoopToggle,
                    modifier = Modifier
                        .size(40.dp)
                        .background(if (loopActive) Color.White.copy(alpha = 0.15f) else Color.Transparent, CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Loop", tint = if (loopActive) Color(0xFFFFDADE) else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. Sound Lab Synthesizer Console
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Acoustic Waves Synthesis Console",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Synthesizer generates wave frequencies in real-time. Sliders manipulate carrier pitches and BPM oscillators.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Carrier Pitch Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Carrier Pitch Base", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("${currentSong.baseFrequency.toInt()} Hz", fontSize = 11.sp, color = Color(0xFFFFDADE), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = currentSong.baseFrequency,
                        onValueChange = onPitchChange,
                        valueRange = 50f..600f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFFFFDADE),
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BPM Oscillator Speed Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("BPM / Tempo Oscillator", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text(String.format(Locale.US, "%.1fx", playSpeed), fontSize = 11.sp, color = Color(0xFFFFDADE), fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = playSpeed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFFFFDADE),
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- LYRICS EDITOR DIALOG ---
        if (showLyricsEditor) {
            AlertDialog(
                onDismissRequest = { showLyricsEditor = false },
                containerColor = Color(0xFF2C2224),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "Edit Sangeet LRC Lyrics",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Paste standard timed lyrics format [mm:ss.xx] or raw lines.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        OutlinedTextField(
                            value = lyricsEditValue,
                            onValueChange = { lyricsEditValue = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            placeholder = { Text("[00:15.00] Let the Sangeet trail begin...", color = Color.White.copy(alpha = 0.3f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFDADE),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onSaveLyrics(currentSong.id, lyricsEditValue)
                            showLyricsEditor = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF813D53))
                    ) {
                        Text("Save Trails", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLyricsEditor = false }) {
                        Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            )
        }
    }
}

// Robust LRC parser that splits strings into List of timestamp (Ms) and lyric text
fun parseLrc(raw: String?): List<Pair<Long, String>> {
    if (raw.isNullOrBlank()) return emptyList()
    val list = mutableListOf<Pair<Long, String>>()
    val lines = raw.split("\n")
    // Regex matching [mm:ss] or [mm:ss.xx] or [mm:ss:xx] or [mm:ss.xxx]
    val regex = Regex("\\[(\\d+):(\\d+)(?:[.:](\\d+))?]\\s*(.*)")
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue
        val match = regex.matchEntire(trimmed)
        if (match != null) {
            try {
                val min = match.groupValues[1].toLong()
                val sec = match.groupValues[2].toLong()
                val fractionStr = match.groupValues[3]
                val text = match.groupValues[4]
                
                var millis = 0L
                if (fractionStr.isNotEmpty()) {
                    val pad = fractionStr.padEnd(3, '0').take(3)
                    millis = pad.toLong()
                }
                val totalMs = (min * 60 + sec) * 1000 + millis
                list.add(Pair(totalMs, text))
            } catch (e: Exception) {
                // Ignore parsing slip-ups gracefully
            }
        }
    }
    return list.sortedBy { it.first }
}

// Format duration from millis to MM:SS
private fun formatDuration(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
