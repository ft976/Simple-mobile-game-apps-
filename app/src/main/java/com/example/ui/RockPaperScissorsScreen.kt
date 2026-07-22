package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RPSChoice(val emoji: String, val label: String, val color: Color) {
    ROCK("✊", "ROCK", Color(0xFFFF4081)),
    PAPER("✋", "PAPER", Color(0xFFB388FF)),
    SCISSORS("✌️", "SCISSORS", Color(0xFF1DE9B6))
}

enum class GameOutcome {
    IDLE,
    ROLLING,
    WIN,
    LOSE,
    DRAW
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RockPaperScissorsScreen(
    onBackToHome: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    var playerChoice by rememberSaveable { mutableStateOf<RPSChoice?>(null) }
    var cpuChoice by rememberSaveable { mutableStateOf<RPSChoice?>(null) }
    var gameOutcome by rememberSaveable { mutableStateOf(GameOutcome.IDLE) }
    
    var playerScore by rememberSaveable { mutableStateOf(0) }
    var cpuScore by rememberSaveable { mutableStateOf(0) }
    var draws by rememberSaveable { mutableStateOf(0) }
    var currentStreak by rememberSaveable { mutableStateOf(0) }
    var bestStreak by rememberSaveable { mutableStateOf(0) }
    
    // For rolling animation
    var rollingIndex by remember { mutableStateOf(0) }
    
    // Play sound simulation or trigger shake animation scale
    val scaleAnim = remember { Animatable(1f) }

    fun playRound(choice: RPSChoice) {
        coroutineScope.launch {
            playerChoice = choice
            gameOutcome = GameOutcome.ROLLING
            
            // Fast scale animation
            scaleAnim.animateTo(1.1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            
            // Cycle through options for the rolling effect with highly rapid/smooth transitions (approx 800ms total)
            for (i in 0..15) {
                rollingIndex = i % RPSChoice.values().size
                delay(50)
            }
            
            scaleAnim.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh))
            
            // Finalize CPU Choice
            val finalCpu = RPSChoice.values()[Random.nextInt(RPSChoice.values().size)]
            cpuChoice = finalCpu
            
            // Decide Winner
            gameOutcome = when {
                choice == finalCpu -> GameOutcome.DRAW
                (choice == RPSChoice.ROCK && finalCpu == RPSChoice.SCISSORS) ||
                (choice == RPSChoice.PAPER && finalCpu == RPSChoice.ROCK) ||
                (choice == RPSChoice.SCISSORS && finalCpu == RPSChoice.PAPER) -> {
                    playerScore++
                    currentStreak++
                    if (currentStreak > bestStreak) {
                        bestStreak = currentStreak
                    }
                    GameOutcome.WIN
                }
                else -> {
                    cpuScore++
                    currentStreak = 0
                    GameOutcome.LOSE
                }
            }
            if (gameOutcome == GameOutcome.DRAW) {
                draws++
            }
        }
    }

    fun resetStats() {
        playerScore = 0
        cpuScore = 0
        draws = 0
        currentStreak = 0
        playerChoice = null
        cpuChoice = null
        gameOutcome = GameOutcome.IDLE
    }

    // Main background
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = com.example.R.drawable.arcade_bg_pattern_1783521510215),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.05f }
            )
            Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToHome,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Arcade Lounge",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = com.example.R.drawable.rps_sticker_1783521345811),
                        contentDescription = "RPS Logo",
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PAPER SCISSORS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = { resetStats() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset stats",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats / Scorecard row (Gamer Arcade look)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = BoxBorder(androidx.compose.material3.MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScoreColumn(label = "YOU 👑", score = playerScore, color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                    VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp, color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                    ScoreColumn(label = "DRAWS 🤝", score = draws, color = androidx.compose.material3.MaterialTheme.colorScheme.outline)
                    VerticalDivider(modifier = Modifier.height(40.dp), thickness = 1.dp, color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                    ScoreColumn(label = "ANDROID 🤖", score = cpuScore, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            }

            // Streaks/Achievements Mini Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.tertiary)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Streak: $currentStreak 🔥",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.tertiary)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Best: $bestStreak ⭐",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            // Main Battle Arena Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .scale(scaleAnim.value),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(24.dp),
                border = BoxBorder(androidx.compose.material3.MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    // Battle ground title or Outcome Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                when (gameOutcome) {
                                    GameOutcome.WIN -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                                    GameOutcome.LOSE -> androidx.compose.material3.MaterialTheme.colorScheme.error
                                    GameOutcome.DRAW -> androidx.compose.material3.MaterialTheme.colorScheme.tertiary
                                    GameOutcome.ROLLING -> androidx.compose.material3.MaterialTheme.colorScheme.secondary
                                    else -> androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = when (gameOutcome) {
                                GameOutcome.WIN -> "YOU WIN! 🎉"
                                GameOutcome.LOSE -> "ANDROID WINS! 🤖"
                                GameOutcome.DRAW -> "IT'S A DRAW! 🤝"
                                GameOutcome.ROLLING -> "CHOOSING..."
                                else -> "MAKE YOUR MOVE!"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (gameOutcome == GameOutcome.IDLE) androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Hands Displays Side-by-Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Your side
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "YOU", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedHandCircle(
                                choice = playerChoice,
                                isRolling = gameOutcome == GameOutcome.ROLLING,
                                customChoice = if (gameOutcome == GameOutcome.ROLLING) RPSChoice.values()[rollingIndex] else null
                            )
                        }

                        // VERSUS visual separator
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.material3.MaterialTheme.colorScheme.primary)
                                .border(2.dp, androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("VS", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }

                        // CPU side
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "ANDROID AI", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            AnimatedHandCircle(
                                choice = cpuChoice,
                                isRolling = gameOutcome == GameOutcome.ROLLING,
                                customChoice = if (gameOutcome == GameOutcome.ROLLING) RPSChoice.values()[(rollingIndex + 1) % RPSChoice.values().size] else null,
                                isCpu = true
                            )
                        }
                    }

                    // Professional Tips & Feedback at the bottom of the Card
                    Text(
                        text = when (gameOutcome) {
                            GameOutcome.WIN -> "Excellent strategy! Rock beats Scissors, Scissors beats Paper, Paper beats Rock."
                            GameOutcome.LOSE -> "Hard luck! Select another weapon to challenge the AI."
                            GameOutcome.DRAW -> "Great minds think alike! It is a draw."
                            GameOutcome.ROLLING -> "AI is analyzing your move..."
                            else -> "Choose your weapon below to challenge the Android robot!"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // Lower panel: Hand Selection Buttons (Tappable items)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "CHOOSE YOUR WEAPON",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RPSChoice.values().forEach { choice ->
                        val isEnabled = gameOutcome != GameOutcome.ROLLING
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = isEnabled) { playRound(choice) }
                                .testTag("rps_btn_${choice.label.lowercase()}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isEnabled) androidx.compose.material3.MaterialTheme.colorScheme.surface else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = BoxBorder(choice.color.copy(alpha = if (isEnabled) 1.0f else 0.3f), width = 2.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isEnabled) 4.dp else 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = choice.emoji,
                                    fontSize = 36.sp,
                                    modifier = Modifier.scale(if (isEnabled) 1f else 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = choice.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isEnabled) androidx.compose.material3.MaterialTheme.colorScheme.onSurface else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f)
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

@Composable
fun ScoreColumn(label: String, score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = score.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
fun AnimatedHandCircle(
    choice: RPSChoice?,
    isRolling: Boolean,
    customChoice: RPSChoice? = null,
    isCpu: Boolean = false
) {
    val displayedChoice = if (isRolling) customChoice else choice
    val bgColor = displayedChoice?.color?.copy(alpha = 0.15f) ?: androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
    val borderColor = displayedChoice?.color ?: androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
    
    val infiniteTransition = rememberInfiniteTransition(label = "rolling_anim")
    
    // Smooth intensity decay multiplier
    val rollingIntensity by animateFloatAsState(
        targetValue = if (isRolling) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "rolling_intensity"
    )
    
    // Continuous infinite properties
    val rawBounceOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "raw_bounce"
    )

    val rawRotationAngle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "raw_rotation"
    )

    val bounceOffset = rawBounceOffset * rollingIntensity
    val rotationAngle = rawRotationAngle * rollingIntensity

    // Reveal pop scale - spring bouncy animation when choice is finalized
    val revealScale by animateFloatAsState(
        targetValue = if (isRolling) 0.85f else if (choice != null) 1.0f else 0.9f,
        animationSpec = if (!isRolling && choice != null) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        } else {
            tween(durationMillis = 120)
        },
        label = "reveal_scale"
    )

    Box(
        modifier = Modifier
            .size(90.dp)
            .graphicsLayer {
                scaleX = revealScale
                scaleY = revealScale
                translationY = bounceOffset
                rotationZ = rotationAngle
            }
            .clip(CircleShape)
            .background(bgColor)
            .border(3.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (displayedChoice != null) {
            Text(
                text = displayedChoice.emoji,
                fontSize = 44.sp
            )
        } else {
            Text(
                text = if (isCpu) "🤖" else "❓",
                fontSize = 32.sp
            )
        }
    }
}

private fun BoxBorder(color: Color, width: androidx.compose.ui.unit.Dp = 1.5.dp) = 
    androidx.compose.foundation.BorderStroke(width, color)
