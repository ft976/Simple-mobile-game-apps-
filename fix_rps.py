import sys
import re

file_path = "app/src/main/java/com/example/ui/RockPaperScissorsScreen.kt"
with open(file_path, "r") as f:
    content = f.read()

old_bg = """    // Dynamic Theme background
    Box(
        modifier = modifier
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
new_bg = """    // Main background
    androidx.compose.material3.Surface(
        modifier = modifier.fillMaxSize(),
        color = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) {"""
content = content.replace(old_bg, new_bg)


content = content.replace("""                                text = "ROCK PAPER SCISSORS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2C3E50),
                                letterSpacing = 1.sp""", """                                text = "ROCK PAPER SCISSORS",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground""")

content = content.replace("""                                text = "vs Android Robot",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF7F8C8D)""", """                                text = "vs AI",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant""")


content = content.replace("""                                Text(
                                    text = "YOU",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2C3E50),
                                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                )""", """                                Text(
                                    text = "YOU",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF2C3E50),
                                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                                )""")
                                
content = content.replace("""                                Text(
                                    text = "CPU",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 6.dp, end = 12.dp)
                                )""", """                                Text(
                                    text = "CPU",
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 6.dp, end = 12.dp)
                                )""")
                                
with open(file_path, "w") as f:
    f.write(content)

