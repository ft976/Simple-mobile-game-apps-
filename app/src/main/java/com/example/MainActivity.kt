package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.ui.GameViewModel
import com.example.ui.SwipeDirection
import com.example.ui.theme.MyApplicationTheme
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Flag
import com.example.ui.RockPaperScissorsScreen
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Gesture

enum class Screen {
    Home,
    Game2048,
    RockPaperScissors
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemTheme = androidx.compose.foundation.isSystemInDarkTheme()
            var isDarkTheme by rememberSaveable { mutableStateOf(systemTheme) }
            MyApplicationTheme(darkTheme = isDarkTheme) {
                var currentScreen by rememberSaveable { mutableStateOf(Screen.Home) }
                val viewModel: GameViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            Screen.Home -> HomeScreen(
                                highScore = uiState.highScore,
                                onPlay2048 = { currentScreen = Screen.Game2048 },
                                onPlayRPS = { currentScreen = Screen.RockPaperScissors },
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = { isDarkTheme = it }
                            )
                            Screen.Game2048 -> GameScreen(
                                viewModel = viewModel,
                                isDarkTheme = isDarkTheme,
                                onBackToHome = { currentScreen = Screen.Home }
                            )
                            Screen.RockPaperScissors -> RockPaperScissorsScreen(
                                onBackToHome = { currentScreen = Screen.Home },
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(),
    isDarkTheme: Boolean = false,
    onBackToHome: () -> Unit = {}
) {
    BackHandler {
        onBackToHome()
    }

    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    // Request keyboard focus immediately on screen launch
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Main background
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.arcade_bg_pattern_1783521510215),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.05f }
            )
            Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Content Block
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Nav bar to go back to Home
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackToHome,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "2048 MODE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.width(44.dp)) // horizontal alignment spacer
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.puzzle_2048_sticker_1783521333422),
                            contentDescription = "2048 Graphic",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "2048",
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Black,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                                letterSpacing = (-1.5).sp
                            )
                            Text(
                                text = "Slide to win!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    // Score Badges Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreBadge(
                            label = "Score",
                            value = uiState.score,
                            icon = Icons.Default.Star,
                            iconColor = androidx.compose.material3.MaterialTheme.colorScheme.primary // Dusty rose accent
                        )
                        ScoreBadge(
                            label = "Best",
                            value = uiState.highScore,
                            icon = Icons.Default.EmojiEvents,
                            iconColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiary // Gold accent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Control Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo Button
                    OutlinedButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.undoHistory.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                            disabledContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
                        ),
                        border = borderStrokeForUndo(uiState.undoHistory.isNotEmpty()),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .testTag("undo_button")
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo Move",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Undo (${uiState.undoHistory.size})",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    // Reset / New Game Button
                    Button(
                        onClick = { viewModel.startNewGame() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .testTag("new_game_button")
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "New Game",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New game",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Game Grid Container
            SwipeableBox(
                onSwipeLeft = { viewModel.move(SwipeDirection.LEFT) },
                onSwipeRight = { viewModel.move(SwipeDirection.RIGHT) },
                onSwipeUp = { viewModel.move(SwipeDirection.UP) },
                onSwipeDown = { viewModel.move(SwipeDirection.DOWN) },
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .aspectRatio(1f)
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.DirectionLeft -> { viewModel.move(SwipeDirection.LEFT); true }
                                Key.DirectionRight -> { viewModel.move(SwipeDirection.RIGHT); true }
                                Key.DirectionUp -> { viewModel.move(SwipeDirection.UP); true }
                                Key.DirectionDown -> { viewModel.move(SwipeDirection.DOWN); true }
                                else -> false
                            }
                        } else false
                    }
            ) {
                // Background grid card
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(2.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        // 1. Static empty grid background
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (r in 0..3) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    for (c in 0..3) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant) // empty cell color
                                                .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                .testTag("cell_${r}_${c}")
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Dynamic animated tiles on top
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val gap = 10.dp
                            val cellSize = (maxWidth - gap * 3) / 4

                            uiState.tiles.forEach { tile ->
                                key(tile.id) {
                                    AnimatedTile(
                                        tile = tile,
                                        cellSize = cellSize,
                                        gap = gap
                                    )
                                }
                            }
                        }
                    }
                }

                // In-place Overlays (Win / Game Over)
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.hasWon && !uiState.canKeepPlaying,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        OverlayScreen(
                            title = "You reached 2048!",
                            subtitle = "Congratulations! Keep playing to achieve a higher score.",
                            primaryButtonText = "Keep playing",
                            onPrimaryClick = { viewModel.keepPlaying() },
                            primaryTestTag = "keep_playing_button",
                            secondaryButtonText = "New game",
                            onSecondaryClick = { viewModel.startNewGame() },
                            badgeIcon = Icons.Default.EmojiEvents,
                            badgeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.isGameOver,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        OverlayScreen(
                            title = "No more moves",
                            subtitle = "No legal moves remaining. Try another round!",
                            primaryButtonText = "New game",
                            onPrimaryClick = { viewModel.startNewGame() },
                            primaryTestTag = "try_again_button",
                            secondaryButtonText = null,
                            onSecondaryClick = {},
                            badgeIcon = Icons.Default.Info,
                            badgeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rules / Tips Footer Description
            Text(
                text = "Swipe or use arrow keys to move tiles",
                fontSize = 12.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        }
    }
}

@Composable
fun ScoreBadge(
    label: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    imageRes: Int? = null,
    iconColor: Color
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant // Warm board color
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = value.toString(),
                fontSize = 20.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun AnimatedTile(
    tile: com.example.ui.Tile,
    cellSize: androidx.compose.ui.unit.Dp,
    gap: androidx.compose.ui.unit.Dp
) {
    val xTarget = (cellSize + gap) * tile.col
    val yTarget = (cellSize + gap) * tile.row

    // Animate coordinates smoothly and extremely snappily (high stiffness)
    val animatedX by androidx.compose.animation.core.animateDpAsState(
        targetValue = xTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 3200f // Highly responsive and super fast sliding
        ),
        label = "TileX_${tile.id}"
    )

    val animatedY by androidx.compose.animation.core.animateDpAsState(
        targetValue = yTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 3200f // Highly responsive and super fast sliding
        ),
        label = "TileY_${tile.id}"
    )

    // Using Animatable for precise control over spawn/merge animations
    val scaleAnim = remember { androidx.compose.animation.core.Animatable(if (tile.isNew) 0f else 1f) }
    val alphaAnim = remember { androidx.compose.animation.core.Animatable(if (tile.isNew) 0f else 1f) }

    LaunchedEffect(tile.id, tile.isNew, tile.isMerged, tile.toRemove) {
        if (tile.toRemove) {
            // Smooth fade out and shrink for merged away tiles
            launch {
                scaleAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(stiffness = 3200f)
                )
            }
            launch {
                alphaAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(stiffness = 3200f)
                )
            }
        } else if (tile.isMerged) {
            // Juicy spring pop animation for successfully merged tiles
            scaleAnim.animateTo(
                targetValue = 1.22f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = 3800f
                )
            )
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = 3200f
                )
            )
        } else if (tile.isNew) {
            // Beautiful bounce-in spawn animation for new tiles
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = 3200f
                )
            )
            alphaAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(stiffness = 3200f)
            )
        } else {
            // Keep at base state
            launch { scaleAnim.animateTo(1f) }
            launch { alphaAnim.animateTo(1f) }
        }
    }

    val (backgroundColor, textColor) = getTileColors(tile.value)

    Box(
        modifier = Modifier
            .size(cellSize)
            .graphicsLayer {
                translationX = animatedX.toPx()
                translationY = animatedY.toPx()
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                alpha = alphaAnim.value
            }
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (tile.value >= 1024) 2.dp else if (tile.value == 0) 0.dp else 1.dp,
                color = if (tile.value >= 1024) androidx.compose.material3.MaterialTheme.colorScheme.primary else if (tile.value == 0) Color.Transparent else Color.Black.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("tile_${tile.row}_${tile.col}"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle top reflection for 3D effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
        Text(
            text = tile.value.toString(),
            fontSize = when {
                tile.value >= 10000 -> 18.sp
                tile.value >= 1000 -> 22.sp
                tile.value >= 100 -> 26.sp
                else -> 32.sp
            },
            fontWeight = FontWeight.Black,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GameCell(
    value: Int,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = getTileColors(value)

    // Bounce animation when cell loads with non-zero value
    val cellScale by animateFloatAsState(
        targetValue = if (value > 0) 1.0f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CellScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(cellScale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (value == 2048) 2.dp else 0.dp,
                color = if (value == 2048) androidx.compose.material3.MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (value > 0) {
            Text(
                text = value.toString(),
                fontSize = when {
                    value >= 1000 -> 22.sp
                    value >= 100 -> 26.sp
                    else -> 32.sp
                },
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OverlayScreen(
    title: String,
    subtitle: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    primaryTestTag: String,
    secondaryButtonText: String?,
    onSecondaryClick: () -> Unit,
    badgeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color
) {
    Surface(
        color = androidx.compose.material3.MaterialTheme.colorScheme.background.copy(alpha = 0.9f), // Translucent deep plum-charcoal overlay
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = badgeIcon,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPrimaryClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag(primaryTestTag)
                ) {
                    Text(
                        text = primaryButtonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (secondaryButtonText != null) {
                    OutlinedButton(
                        onClick = onSecondaryClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = borderStrokeForUndo(true),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = secondaryButtonText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableBox(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    var totalDragX by remember { mutableStateOf(0f) }
    var totalDragY by remember { mutableStateOf(0f) }
    var hasSwipedInCurrentGesture by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                        hasSwipedInCurrentGesture = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!hasSwipedInCurrentGesture) {
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y

                            val absX = kotlin.math.abs(totalDragX)
                            val absY = kotlin.math.abs(totalDragY)
                            val threshold = 55f // highly responsive activation threshold

                            if (absX > threshold || absY > threshold) {
                                hasSwipedInCurrentGesture = true // lock swipe until next touch down
                                if (absX > absY) {
                                    if (totalDragX > 0) onSwipeRight() else onSwipeLeft()
                                } else {
                                    if (totalDragY > 0) onSwipeDown() else onSwipeUp()
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        hasSwipedInCurrentGesture = false
                    },
                    onDragCancel = {
                        hasSwipedInCurrentGesture = false
                    }
                )
            },
        content = content
    )
}

@Composable
private fun borderStrokeForUndo(enabled: Boolean): androidx.compose.foundation.BorderStroke {
    return if (enabled) {
        androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary)
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun getTileColors(value: Int): Pair<Color, Color> {
    return when (value) {
        0 -> Pair(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant, Color.Transparent)
        2 -> Pair(Color(0xFF1E88E5), Color.White)
        4 -> Pair(Color(0xFF00ACC1), Color.White)
        8 -> Pair(Color(0xFF00897B), Color.White)
        16 -> Pair(Color(0xFF43A047), Color.White)
        32 -> Pair(Color(0xFF7CB342), Color.White)
        64 -> Pair(Color(0xFFC0CA33), Color.White)
        128 -> Pair(Color(0xFFFDD835), Color(0xFF212529))
        256 -> Pair(Color(0xFFFFB300), Color(0xFF212529))
        512 -> Pair(Color(0xFFFB8C00), Color.White)
        1024 -> Pair(Color(0xFFF4511E), Color.White)
        2048 -> Pair(Color(0xFFE53935), Color.White)
        4096 -> Pair(Color(0xFFD81B60), Color.White)
        8192 -> Pair(Color(0xFF8E24AA), Color.White)
        else -> Pair(Color(0xFF5E35B1), Color.White)
    }
}

@Composable
fun HomeScreen(
    highScore: Int,
    onPlay2048: () -> Unit,
    onPlayRPS: () -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "mascot_float")
    val mascotFloatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "mascot_float"
    )
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Subtle seamless background pattern
            Image(
                painter = painterResource(id = R.drawable.arcade_bg_pattern_1783521510215),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.05f } // Very subtle
            )
        
            Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Arcade Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsEsports,
                            contentDescription = "Arcade Lounge Icon",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "ARCADE LOUNGE 🎮",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Select a game to start playing!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                androidx.compose.material3.Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeToggle
                )
            }

            // Beautiful Hero Arcade Banner Image Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(2.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_arcade_banner_1783502198486),
                        contentDescription = "Arcade Lounge Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Dark ambient overlay with soft radial/linear gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x2216121B),
                                        Color(0xAA16121B)
                                    )
                                )
                            )
                    )
                    
                    // Floating Mascot
                    Image(
                        painter = painterResource(id = R.drawable.arcade_mascot_1783521321124),
                        contentDescription = "Floating Mascot",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 20.dp)
                            .size(90.dp)
                            .offset(y = mascotFloatY.dp)
                            .graphicsLayer {
                                rotationZ = 5f
                            }
                    )
                    
                    // Banner Sticker Badges overlay
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Lounge Arcade Room",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Slide. Match. Control. Win!",
                                fontSize = 11.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        // Sticker-like glowing badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.tertiary)
                                .border(1.5.dp, Color.White, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "⭐ PLAY NOW",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                }
            }

            // Game Selection List Heading
            Text(
                text = "CHOOSE A GAME",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            )

            // Game Card 1: 2048 (Active)
            GameHubCard(
                title = "2048 Puzzle 🧩",
                subtitle = "Slide & merge matching tiles!",
                icon = Icons.Default.GridOn,
                imageRes = R.drawable.puzzle_2048_sticker_1783521333422,
                accentColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
                badgeText = "CLASSIC",
                badgeBgColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                isLocked = false,
                onPlayClick = onPlay2048
            )

            // Game Card 2: Rock Paper Scissors (Active!)
            GameHubCard(
                title = "Rock Paper Scissors ✊✋✌️",
                subtitle = "Challenge the Android Robot AI!",
                icon = Icons.Default.Gesture,
                imageRes = R.drawable.rps_sticker_1783521345811,
                accentColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                badgeText = "POPULAR",
                badgeBgColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                isLocked = false,
                onPlayClick = onPlayRPS
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom "Have Fun" Sticker
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.have_fun_sticker_1783521526099),
                    contentDescription = "Have Fun Sticker",
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer {
                            rotationZ = -8f
                        }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
        }
    }
}

@Composable
fun GameHubCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    imageRes: Int? = null,
    accentColor: Color,
    badgeText: String,
    badgeBgColor: Color,
    isLocked: Boolean,
    onPlayClick: () -> Unit,
    highScore: Int = 0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLocked) { onPlayClick() }
            .testTag("game_card_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isLocked) 1.dp else 2.dp,
            color = if (isLocked) Color.LightGray.copy(alpha = 0.5f) else accentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Left game icon box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isLocked) androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant else accentColor.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "$title Icon",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$title Icon",
                        tint = if (isLocked) androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f) else accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Middle info details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isLocked) androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f) else androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Small mini-badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeBgColor)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.8f)
                )
            }

            // Right Action Button/Lock indicator
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                IconButton(
                    onClick = onPlayClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .testTag("play_button_${title.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow,
                        contentDescription = "Play $title",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

