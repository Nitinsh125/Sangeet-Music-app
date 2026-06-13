package com.example.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    modifier: Modifier = Modifier,
    viewModel: MusicViewModel = viewModel()
) {
    val allSongs by viewModel.allSongs.collectAsStateWithLifecycle()
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Library, 1: Playlists, 2: Sound Lab

    // Dialog trigger states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf<Song?>(null) }

    // Floating Vinyl Rotation Angle
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
            .background(CosmosDark)
    ) {
        // Glowing Ambient Blurs to mimic "Frosted Glass" top[-10%] left[-10%] bg-purple and bottom-right bg-blue
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Upper-left fuzzy purple spot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x388B5CF6), Color.Transparent), // purple/22 blur
                    center = Offset(size.width * 0.1f, size.height * 0.12f),
                    radius = size.minDimension * 0.8f
                ),
                center = Offset(size.width * 0.1f, size.height * 0.12f),
                radius = size.minDimension * 0.8f
            )
            // Lower-right fuzzy blue spot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x223B82F6), Color.Transparent), // blue/14 blur
                    center = Offset(size.width * 0.9f, size.height * 0.88f),
                    radius = size.minDimension * 0.9f
                ),
                center = Offset(size.width * 0.9f, size.height * 0.88f),
                radius = size.minDimension * 0.9f
            )
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AURA",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CosmosPrimary,
                                letterSpacing = 4.sp
                            )
                            Text(
                                text = "Synth Audio Laboratory",
                                fontSize = 11.sp,
                                color = CosmosAccent,
                                letterSpacing = 1.sp
                            )
                        }
                        
                        // Playlist queue descriptor
                        IconButton(
                            onClick = { viewModel.selectPlaylist(null) },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                .size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Reset Queue to Library",
                                tint = if (activePlaylistId == null) CosmosPrimary else CosmosTextLight
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Glassmorphic Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search tracks or artists...", color = CosmosTextMuted.copy(alpha = 0.8f), fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CosmosPrimary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.08f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                            focusedBorderColor = Color.White.copy(alpha = 0.25f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            focusedTextColor = CosmosTextLight,
                            unfocusedTextColor = CosmosTextLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

            // SECTION 1: NOW PLAYING SCREEN CORE CARD (Vinyl & Neon Visualizer)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Frosted Glass Album art container
                Box(
                    modifier = Modifier
                        .sizeIn(maxWidth = 200.dp, maxHeight = 200.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFEC4899)),
                                start = Offset(0f, 0f),
                                end = Offset.Infinite
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Vinyl container
                        Box(
                            modifier = Modifier
                                .size(145.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Draw Cosmic orbits behind record
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = CosmosSecondary.copy(alpha = 0.2f),
                                    radius = size.minDimension / 2,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                drawCircle(
                                    color = CosmosAccent.copy(alpha = 0.1f),
                                    radius = size.minDimension * 0.38f / 2,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }

                            // Rotating Vinyl canvas
                            val currentRotation = if (isPlaying) rotationAngle else 0f
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(currentRotation),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val center = Offset(size.width / 2, size.height / 2)
                                    val radius = size.minDimension / 2
                                    
                                    // 1. Vinyl Base (Ebony record)
                                    drawCircle(
                                        color = Color(0xFF141416),
                                        radius = radius
                                    )
                                    
                                    // 2. Sound tracks lines (Grooves)
                                    for (r in 4..14) {
                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.04f),
                                            radius = radius * (r / 15f),
                                            style = Stroke(width = 1f)
                                        )
                                    }
                                    
                                    // 3. Center Label Ring
                                    drawCircle(
                                        color = CosmosSecondary,
                                        radius = radius * 0.4f
                                    )
                                }
                                
                                // Custom procedurally styled gradient center cover
                                val sColors = remember(currentSong) {
                                    listOf(
                                        Color(android.graphics.Color.parseColor(currentSong?.albumGradientStart ?: "#8A2387")),
                                        Color(android.graphics.Color.parseColor(currentSong?.albumGradientEnd ?: "#E94057"))
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(Brush.radialGradient(colors = sColors))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Metal Spindle Center hole
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(CosmosDark)
                                            .padding(3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(Color.LightGray)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Real-time Glowing 16-Band Sine/Wave Visualizer
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .padding(horizontal = 40.dp)
                ) {
                    val barWidth = size.width / 16f
                    val gap = 6f
                    
                    for (i in 0 until 16) {
                        val coef = if (i < visualizerData.size) visualizerData[i] else 0.1f
                        val finalHeight = (size.height * coef).coerceAtLeast(4f)
                        
                        val startX = i * barWidth + gap / 2
                        val startY = size.height - finalHeight
                        val w = barWidth - gap
                        
                        // Draw glowing neon cylinder bars
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(CosmosAccent, CosmosPrimary),
                                startY = startY,
                                endY = size.height
                            ),
                            topLeft = Offset(startX, startY),
                            size = Size(w, finalHeight),
                            cornerRadius = CornerRadius(w / 2, w / 2)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Song Meta Text Information (Title + Artist + Like button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentSong?.title ?: "Ambient Cosmos Engine",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmosTextLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentSong?.artist ?: "Unknown Space Signal",
                            fontSize = 13.sp,
                            color = CosmosTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    currentSong?.let { song ->
                        IconButton(
                            onClick = { viewModel.toggleFavorite(song) },
                            modifier = Modifier.testTag("favorite_toggle_btn")
                        ) {
                            Icon(
                                imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite Song",
                                tint = if (song.isFavorite) CosmosPrimary else CosmosTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Progressive Duration Seekbar
                currentSong?.let { song ->
                    val progressRatio = if (song.durationMs > 0) progressMs.toFloat() / song.durationMs else 0f
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    ) {
                        Slider(
                            value = progressRatio,
                            onValueChange = {}, // read-only live generator progression 
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth().height(16.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatDuration(progressMs),
                                fontSize = 11.sp,
                                color = CosmosPrimary
                            )
                            Text(
                                text = formatDuration(song.durationMs),
                                fontSize = 11.sp,
                                color = CosmosTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Control Center Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleShuffle() },
                        modifier = Modifier
                            .background(if (shuffleActive) Color.White.copy(alpha = 0.15f) else Color.Transparent, CircleShape)
                            .border(1.dp, if (shuffleActive) Color.White.copy(alpha = 0.25f) else Color.Transparent, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (shuffleActive) CosmosPrimary else CosmosTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.playPrevious() },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = CosmosTextLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Main Glassmorphic Solid High-Contrast Play Button
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .clickable { viewModel.togglePlayPause() }
                            .testTag("play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = CosmosDark,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.playNext() },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = CosmosTextLight,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleLoop() },
                        modifier = Modifier
                            .background(if (loopActive) Color.White.copy(alpha = 0.15f) else Color.Transparent, CircleShape)
                            .border(1.dp, if (loopActive) Color.White.copy(alpha = 0.25f) else Color.Transparent, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Loop Song",
                            tint = if (loopActive) CosmosPrimary else CosmosTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // SECTION 2: NEON CLUSTER TABS (Library, Playlists, Sound Lab)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                ) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = CosmosPrimary,
                                height = 2.dp
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Library", fontWeight = FontWeight.Bold, color = if (selectedTab == 0) Color.White else CosmosTextMuted) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Playlists", fontWeight = FontWeight.Bold, color = if (selectedTab == 1) Color.White else CosmosTextMuted) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Sound Lab", fontWeight = FontWeight.Bold, color = if (selectedTab == 2) Color.White else CosmosTextMuted) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Transparent)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // --- TAB 1: LIBRARY LIST ---
                            val filteredSongs = allSongs.filter {
                                it.title.contains(searchQuery, ignoreCase = true) ||
                                        it.artist.contains(searchQuery, ignoreCase = true)
                            }

                            if (filteredSongs.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No interstellar signals found.",
                                        color = CosmosTextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    items(filteredSongs) { song ->
                                        val isCurrent = currentSong?.id == song.id
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (isCurrent) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f))
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isCurrent) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                                    shape = RoundedCornerShape(14.dp)
                                                )
                                                .clickable { viewModel.playSong(song) }
                                                .padding(horizontal = 14.dp, vertical = 11.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Floating gradient dot indicator
                                            val gradColors = listOf(
                                                Color(android.graphics.Color.parseColor(song.albumGradientStart)),
                                                Color(android.graphics.Color.parseColor(song.albumGradientEnd))
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Brush.linearGradient(gradColors)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Play Track",
                                                    tint = Color.White.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = song.title,
                                                    color = if (isCurrent) CosmosAccent else CosmosTextLight,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = song.artist,
                                                    color = CosmosTextMuted,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Text(
                                                text = formatDuration(song.durationMs),
                                                color = CosmosTextMuted,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )

                                            IconButton(onClick = { showAddToPlaylistDialog = song }) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add to playlist",
                                                    tint = CosmosAccent
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // --- TAB 2: PLAYLISTS PANEL ---
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Playlists",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CosmosTextLight
                                    )
                                    Button(
                                        onClick = { showCreatePlaylistDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmosPrimary, contentColor = CosmosTextLight),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("+ Create", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                            text = "No custom playlists. Create one now!",
                                            color = CosmosTextMuted,
                                            fontSize = 13.sp
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
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(if (isSelected) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.03f))
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                                        shape = RoundedCornerShape(14.dp)
                                                    )
                                                    .clickable { 
                                                        viewModel.selectPlaylist(if (isSelected) null else playlist.id) 
                                                    }
                                                    .padding(14.dp)
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
                                                        Icon(
                                                            imageVector = Icons.Default.Share,
                                                            contentDescription = "Folder",
                                                            tint = if (isSelected) CosmosAccent else CosmosTextMuted,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(
                                                                text = playlist.name,
                                                                color = CosmosTextLight,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 14.sp
                                                            )
                                                            if (playlist.description.isNotEmpty()) {
                                                                Text(
                                                                    text = playlist.description,
                                                                    color = CosmosTextMuted,
                                                                    fontSize = 11.sp,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = if (isSelected) "Active Queue" else "Tap to Play",
                                                            fontSize = 11.sp,
                                                            color = if (isSelected) CosmosAccent else CosmosTextMuted,
                                                            fontWeight = FontWeight.SemiBold,
                                                            modifier = Modifier.padding(end = 4.dp)
                                                        )
                                                        IconButton(
                                                            onClick = { viewModel.deletePlaylist(playlist) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete Playlist",
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
                        2 -> {
                            // --- TAB 3: SOUND LAB (Pitch Bend Synth Settings) ---
                            currentSong?.let { song ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Text(
                                        text = "Acoustic Waves Synthesis Console",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CosmosTextLight
                                    )
                                    Text(
                                        text = "Aura synthesizes relaxing organic sounds in real-time. Drag sliders below to manipulate carriers, modulate cerebral rhythms, or speed up oscillators.",
                                        fontSize = 11.sp,
                                        color = CosmosTextMuted,
                                        lineHeight = 16.sp
                                    )

                                    // Carrier Base Pitch Bend
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Settings, contentDescription = "Pitch", tint = CosmosPrimary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Synthesizer Pitch Base", fontSize = 12.sp, color = CosmosTextLight, fontWeight = FontWeight.Bold)
                                            }
                                            Text("${song.baseFrequency.toInt()} Hz", fontSize = 12.sp, color = CosmosPrimary, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        Slider(
                                            value = song.baseFrequency,
                                            onValueChange = { viewModel.customizeSynth(it) },
                                            valueRange = 50f..600f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = CosmosPrimary,
                                                activeTrackColor = CosmosPrimary,
                                                inactiveTrackColor = CosmosDark
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("50Hz (Deep Bass)", fontSize = 9.sp, color = CosmosTextMuted)
                                            Text("600Hz (High Squeak)", fontSize = 9.sp, color = CosmosTextMuted)
                                        }
                                    }

                                    // Tempo speed scale multiplier
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.Settings, contentDescription = "Tempo", tint = CosmosAccent, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("BPM / Oscillator Speed", fontSize = 12.sp, color = CosmosTextLight, fontWeight = FontWeight.Bold)
                                            }
                                            Text(String.format(Locale.US, "%.1fx", playSpeed), fontSize = 12.sp, color = CosmosAccent, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        Slider(
                                            value = playSpeed,
                                            onValueChange = { viewModel.setSpeed(it) },
                                            valueRange = 0.5f..2.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = CosmosAccent,
                                                activeTrackColor = CosmosAccent,
                                                inactiveTrackColor = CosmosDark
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("0.5x (Slow Chill)", fontSize = 9.sp, color = CosmosTextMuted)
                                            Text("2.0x (Hyperdrive)", fontSize = 9.sp, color = CosmosTextMuted)
                                        }
                                    }

                                    // Ambient presets legend info
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(CosmosDark.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Synth Mode Status",
                                            tint = CosmosAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Running: ${song.synthStyle.replace("_", " ")} logic engine.",
                                            color = CosmosTextLight,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            } ?: Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Select a song from the library to bend its waves.",
                                    color = CosmosTextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // --- MODAL DIALOGS ---

    // 1. Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = CosmosCard,
            title = { Text("Create New Playlist", color = CosmosTextLight, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Playlist Name", color = CosmosTextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmosTextLight,
                            unfocusedTextColor = CosmosTextLight,
                            focusedBorderColor = CosmosAccent,
                            unfocusedBorderColor = CosmosTextMuted
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text("Short Description (optional)", color = CosmosTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmosTextLight,
                            unfocusedTextColor = CosmosTextLight,
                            focusedBorderColor = CosmosAccent,
                            unfocusedBorderColor = CosmosTextMuted
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
                    colors = ButtonDefaults.buttonColors(containerColor = CosmosAccent, contentColor = CosmosDark)
                ) {
                    Text("Confirm", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel", color = CosmosTextMuted)
                }
            }
        )
    }

    // 2. Add Song To Playlist Selector Dialog
    showAddToPlaylistDialog?.let { targetSong ->
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = null },
            containerColor = CosmosCard,
            title = {
                Text(
                    text = "Add to Playlist",
                    color = CosmosTextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                if (playlists.isEmpty()) {
                    Text("You have no playlists yet. Create one in the Playlists tab!", color = CosmosTextMuted)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlists) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CosmosDark)
                                    .clickable {
                                        viewModel.addSongToPlaylist(p.id, targetSong.id)
                                        showAddToPlaylistDialog = null
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = p.name, color = CosmosTextLight, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Default.Add, contentDescription = "Add here", tint = CosmosAccent)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToPlaylistDialog = null }) {
                    Text("Dismiss", color = CosmosTextMuted)
                }
            }
        )
    }
}

// Format duration from millis to MM:SS
private fun formatDuration(ms: Long): String {
    val totalSecs = ms / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
