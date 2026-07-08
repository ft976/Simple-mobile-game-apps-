import sys
import re

file_path = "app/src/main/java/com/example/MainActivity.kt"
with open(file_path, "r") as f:
    content = f.read()

# Make the backgrounds use MaterialTheme instead of hardcoding
# Replace HomeScreen Background
old_home_bg = """    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(Color(0xFF1E2A38), Color(0xFF121A23))
                    } else {
                        listOf(Color(0xFFE0F7FA), Color(0xFFFFF9C4))
                    }
                )
            )
    ) {"""
new_home_bg = """    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {"""
content = content.replace(old_home_bg, new_home_bg)

# Replace GameScreen Background
old_game_bg = """    // Main deep-space metallic background gradient
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = if (isDarkTheme) {
                        listOf(Color(0xFF2A2430), Color(0xFF1F1B24))
                    } else {
                        listOf(Color(0xFFEFE6E2), Color(0xFFD6CFCB))
                    }
                )
            )
    ) {"""
new_game_bg = """    // Main background
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {"""
content = content.replace(old_game_bg, new_game_bg)

# Fix the Game Card Background
content = content.replace("""        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )""", """        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )""")
content = content.replace("""border = BorderStroke(1.5.dp, Color(0xFFEEEEEE))""", """border = BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)""")


# For the header in HomeScreen
content = content.replace("""                            color = if(isDarkTheme) Color.White else Color(0xFF1E2A38),""", """                            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,""")
content = content.replace("""                            color = if(isDarkTheme) Color(0xFFB0BEC5) else Color(0xFF5D6D7E)""", """                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant""")


# Adjust 2048 tiles hardcoded colors
old_tiles = """private fun getTileColors(value: Int): Pair<Color, Color> {
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
}"""
new_tiles = """@Composable
private fun getTileColors(value: Int): Pair<Color, Color> {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme() || !androidx.compose.material3.MaterialTheme.colorScheme.background.let { it == Color.White } // approximation
    return when (value) {
        0 -> Pair(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant, Color.Transparent)
        2 -> Pair(androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer, androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer)
        4 -> Pair(androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer, androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer)
        8 -> Pair(androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer, androidx.compose.material3.MaterialTheme.colorScheme.onTertiaryContainer)
        16 -> Pair(Color(0xFFD97F6B), Color.White)
        32 -> Pair(Color(0xFFC9713F), Color.White)
        64 -> Pair(Color(0xFFB8862F), Color.White)
        128 -> Pair(Color(0xFF8FA37A), Color.White)
        256 -> Pair(Color(0xFF6E9583), Color.White)
        512 -> Pair(Color(0xFF4E8792), Color.White)
        1024 -> Pair(Color(0xFF5C6E9E), Color.White)
        2048 -> Pair(androidx.compose.material3.MaterialTheme.colorScheme.primary, androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
        4096 -> Pair(androidx.compose.material3.MaterialTheme.colorScheme.secondary, androidx.compose.material3.MaterialTheme.colorScheme.onSecondary)
        else -> Pair(androidx.compose.material3.MaterialTheme.colorScheme.tertiary, androidx.compose.material3.MaterialTheme.colorScheme.onTertiary)
    }
}"""
content = content.replace(old_tiles, new_tiles)
content = content.replace("val colors = getTileColors(value)", "val colors = getTileColors(value)") # check

# Rock Paper scissors text on home screen
content = content.replace("""Text(
                        text = "Slide. Match. Control. Win!",
                        fontSize = 11.sp,
                        color = Color(0xFFFFD93D),
                        fontWeight = FontWeight.ExtraBold
                    )""", """Text(
                        text = "Play and enjoy!",
                        fontSize = 11.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                        fontWeight = FontWeight.ExtraBold
                    )""")

# Top bar text in GameScreen
content = content.replace("""                    Text(
                        text = "2048 MODE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFBCA69D),
                        letterSpacing = 2.sp
                    )""", """                    Text(
                        text = "2048",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )""")

# Text color in GameHubCard
content = content.replace("""                            Text(
                                text = title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2C3E50)
                            )
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7F8C8D)
                            )""", """                            Text(
                                text = title,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = subtitle,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )""")
content = content.replace("""                                color = Color(0xFF2C3E50)""", """                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant""")

content = content.replace("""            // Game Selection List Heading
            Text(
                text = "CHOOSE A FUN GAME TO PLAY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = if (isDarkTheme) Color(0xFF82B1FF) else Color(0xFF4D96FF),
                letterSpacing = 1.2.sp
            )""", """            // Game Selection List Heading
            Text(
                text = "CHOOSE A GAME",
                style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            )""")


with open(file_path, "w") as f:
    f.write(content)

