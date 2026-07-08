package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import com.example.ui.GameViewModel
import com.example.ui.SwipeDirection
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GameScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    // Request keyboard focus immediately on screen launch
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Main deep-space metallic background gradient
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2A2430), // Dusk warm plum-charcoal start
                        Color(0xFF1F1B24)  // Dusk plum-charcoal deep end
                    )
                )
            )
    ) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "2048",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            color = Color(0xFFEFE6E2), // Warm off-white
                            letterSpacing = (-1.5).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Merge tiles to reach 2048",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB7ABA0) // taupe accent
                        )
                    }

                    // Score Badges Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreBadge(
                            label = "Score",
                            value = uiState.score,
                            icon = Icons.Default.Star,
                            iconColor = Color(0xFFE8B4B8) // Dusty rose accent
                        )
                        ScoreBadge(
                            label = "Best",
                            value = uiState.highScore,
                            icon = Icons.Default.EmojiEvents,
                            iconColor = Color(0xFFB49A3D) // Gold accent
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
                            contentColor = Color(0xFFEFE6E2),
                            disabledContentColor = Color(0xFF8A7E82)
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
                            containerColor = Color(0xFFE8B4B8),
                            contentColor = Color(0xFF3A3238)
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
                        containerColor = Color(0xFF3A3238) // Warm board color (#3A3238)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
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
                                    val value = uiState.grid[r][c]
                                    GameCell(
                                        value = value,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("cell_${r}_${c}")
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
                            badgeColor = Color(0xFFE8B4B8)
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
                            badgeColor = Color(0xFFE8B4B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rules / Tips Footer Description
            Text(
                text = "Swipe or use arrow keys to move tiles",
                fontSize = 12.sp,
                color = Color(0xFF8A7E82),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun ScoreBadge(
    label: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3A3238) // Warm board color
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
                    color = Color(0xFFB7ABA0),
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = value.toString(),
                fontSize = 20.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEFE6E2)
            )
        }
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
                color = if (value == 2048) Color(0xFFE8B4B8) else Color.Transparent,
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
        color = Color(0xEB1F1B24), // Translucent deep plum-charcoal overlay
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
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = Color(0xFFB7ABA0),
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
                        containerColor = Color(0xFFE8B4B8),
                        contentColor = Color(0xFF3A3238)
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

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y
                    },
                    onDragEnd = {
                        val absX = kotlin.math.abs(totalDragX)
                        val absY = kotlin.math.abs(totalDragY)
                        val threshold = 70f // highly sensitive swipe activation
                        if (absX > threshold || absY > threshold) {
                            if (absX > absY) {
                                if (totalDragX > 0) onSwipeRight() else onSwipeLeft()
                            } else {
                                if (totalDragY > 0) onSwipeDown() else onSwipeUp()
                            }
                        }
                    }
                )
            },
        content = content
    )
}

private fun borderStrokeForUndo(enabled: Boolean): androidx.compose.foundation.BorderStroke {
    return if (enabled) {
        androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE8B4B8))
    } else {
        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF463E45))
    }
}

private fun getTileColors(value: Int): Pair<Color, Color> {
    return when (value) {
        0 -> Pair(Color(0xFF463E45), Color.Transparent) // Empty Cell Warm Slate-Grey
        2 -> Pair(Color(0xFFEFE6E2), Color(0xFF3A3238))
        4 -> Pair(Color(0xFFE8B4B8), Color(0xFF3A3238))
        8 -> Pair(Color(0xFFE0949A), Color(0xFFFFFFFF))
        16 -> Pair(Color(0xFFD97F6B), Color(0xFFFFFFFF))
        32 -> Pair(Color(0xFFC9713F), Color(0xFFFFFFFF))
        64 -> Pair(Color(0xFFB8862F), Color(0xFFFFFFFF))
        128 -> Pair(Color(0xFF8FA37A), Color(0xFFFFFFFF))
        256 -> Pair(Color(0xFF6E9583), Color(0xFFFFFFFF))
        512 -> Pair(Color(0xFF4E8792), Color(0xFFFFFFFF))
        1024 -> Pair(Color(0xFF5C6E9E), Color(0xFFFFFFFF))
        2048 -> Pair(Color(0xFF7C5CA3), Color(0xFFFFFFFF))
        4096 -> Pair(Color(0xFFB49A3D), Color(0xFFFFFFFF))
        else -> Pair(Color(0xFF7C5CA3), Color(0xFFFFFFFF))
    }
}
