package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.GameSessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class GameUiState(
    val grid: List<List<Int>> = List(4) { List(4) { 0 } },
    val score: Int = 0,
    val highScore: Int = 0,
    val isGameOver: Boolean = false,
    val hasWon: Boolean = false,
    val canKeepPlaying: Boolean = false,
    val undoHistory: List<GameHistorySnapshot> = emptyList(),
    val isLoading: Boolean = true
)

data class GameHistorySnapshot(
    val grid: List<List<Int>>,
    val score: Int
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadSavedSession()
    }

    private fun loadSavedSession() {
        viewModelScope.launch {
            val savedSession = repository.getSessionFlow().firstOrNull()
            if (savedSession != null) {
                _uiState.value = GameUiState(
                    grid = csvToGrid(savedSession.gridCsv),
                    score = savedSession.score,
                    highScore = savedSession.highScore,
                    isGameOver = savedSession.isGameOver,
                    hasWon = savedSession.hasWon,
                    canKeepPlaying = savedSession.canKeepPlaying,
                    undoHistory = deserializeHistory(savedSession.historyJson),
                    isLoading = false
                )
            } else {
                startNewGame()
            }
        }
    }

    fun startNewGame() {
        var newGrid = List(4) { List(4) { 0 } }
        newGrid = spawnRandomTile(newGrid)
        newGrid = spawnRandomTile(newGrid)

        _uiState.value = GameUiState(
            grid = newGrid,
            score = 0,
            highScore = _uiState.value.highScore, // Retain high score across games
            isGameOver = false,
            hasWon = false,
            canKeepPlaying = false,
            undoHistory = emptyList(),
            isLoading = false
        )
        saveCurrentSession()
    }

    fun move(direction: SwipeDirection) {
        val currentState = _uiState.value
        // If game is over, or user won but hasn't dismissed the win screen (by selecting Keep Playing)
        if (currentState.isGameOver || (currentState.hasWon && !currentState.canKeepPlaying && has2048(currentState.grid))) return

        val originalGrid = currentState.grid
        val (movedGrid, scoreGained) = when (direction) {
            SwipeDirection.LEFT -> moveLeft(originalGrid)
            SwipeDirection.RIGHT -> moveRight(originalGrid)
            SwipeDirection.UP -> moveUp(originalGrid)
            SwipeDirection.DOWN -> moveDown(originalGrid)
        }

        // Only progress game if the swipe actually shifted some tiles
        if (movedGrid != originalGrid) {
            // Check for new highest value in merged
            var maxTile = 0
            for (row in movedGrid) {
                for (cell in row) {
                    if (cell > maxTile) maxTile = cell
                }
            }

            // Save snapshot to history BEFORE spawning a new tile
            val newHistory = currentState.undoHistory.toMutableList().apply {
                add(GameHistorySnapshot(originalGrid, currentState.score))
                if (size > 15) removeAt(0) // Limit memory size to last 15 actions
            }

            // Spawn a new random tile (2 or 4) in an empty spot
            val finalGrid = spawnRandomTile(movedGrid)
            val newScore = currentState.score + scoreGained
            val newHighScore = maxOf(currentState.highScore, newScore)

            // Check if game is over
            val gameOver = isGameOver(finalGrid)

            // Check if user hit 2048 for the first time
            val wonNow = maxTile >= 2048
            val previouslyWon = currentState.hasWon
            val showWinScreen = wonNow && !previouslyWon

            _uiState.value = currentState.copy(
                grid = finalGrid,
                score = newScore,
                highScore = newHighScore,
                isGameOver = gameOver,
                hasWon = previouslyWon || wonNow,
                // If they just hit 2048, prompt them; otherwise keep playing
                canKeepPlaying = currentState.canKeepPlaying || !showWinScreen,
                undoHistory = newHistory
            )

            saveCurrentSession()
        }
    }

    fun undo() {
        val currentState = _uiState.value
        if (currentState.undoHistory.isEmpty()) return

        val history = currentState.undoHistory.toMutableList()
        val lastSnapshot = history.removeAt(history.size - 1)

        _uiState.value = currentState.copy(
            grid = lastSnapshot.grid,
            score = lastSnapshot.score,
            isGameOver = false,
            // Re-evaluate if 2048 exists in the restored state
            hasWon = has2048(lastSnapshot.grid),
            undoHistory = history
        )

        saveCurrentSession()
    }

    fun keepPlaying() {
        _uiState.value = _uiState.value.copy(canKeepPlaying = true)
        saveCurrentSession()
    }

    private fun saveCurrentSession() {
        val state = _uiState.value
        viewModelScope.launch {
            repository.saveSession(
                GameSessionEntity(
                    gridCsv = gridToCsv(state.grid),
                    score = state.score,
                    highScore = state.highScore,
                    isGameOver = state.isGameOver,
                    hasWon = state.hasWon,
                    canKeepPlaying = state.canKeepPlaying,
                    historyJson = serializeHistory(state.undoHistory)
                )
            )
        }
    }

    private fun has2048(grid: List<List<Int>>): Boolean {
        for (row in grid) {
            for (cell in row) {
                if (cell >= 2048) return true
            }
        }
        return false
    }

    // Grid sliding math logic
    private fun slideAndMergeRowLeft(row: List<Int>): Pair<List<Int>, Int> {
        val nonZeros = row.filter { it != 0 }
        val result = mutableListOf<Int>()
        var scoreGained = 0
        var i = 0
        while (i < nonZeros.size) {
            if (i + 1 < nonZeros.size && nonZeros[i] == nonZeros[i+1]) {
                val mergedVal = nonZeros[i] * 2
                result.add(mergedVal)
                scoreGained += mergedVal
                i += 2
            } else {
                result.add(nonZeros[i])
                i += 1
            }
        }
        while (result.size < 4) {
            result.add(0)
        }
        return Pair(result, scoreGained)
    }

    private fun moveLeft(grid: List<List<Int>>): Pair<List<List<Int>>, Int> {
        var scoreGained = 0
        val newGrid = grid.map { row ->
            val (newRow, rowScore) = slideAndMergeRowLeft(row)
            scoreGained += rowScore
            newRow
        }
        return Pair(newGrid, scoreGained)
    }

    private fun moveRight(grid: List<List<Int>>): Pair<List<List<Int>>, Int> {
        var scoreGained = 0
        val newGrid = grid.map { row ->
            val (newRow, rowScore) = slideAndMergeRowLeft(row.reversed())
            scoreGained += rowScore
            newRow.reversed()
        }
        return Pair(newGrid, scoreGained)
    }

    private fun moveUp(grid: List<List<Int>>): Pair<List<List<Int>>, Int> {
        val transposed = transpose(grid)
        val (movedTransposed, scoreGained) = moveLeft(transposed)
        return Pair(transpose(movedTransposed), scoreGained)
    }

    private fun moveDown(grid: List<List<Int>>): Pair<List<List<Int>>, Int> {
        val transposed = transpose(grid)
        val (movedTransposed, scoreGained) = moveRight(transposed)
        return Pair(transpose(movedTransposed), scoreGained)
    }

    private fun transpose(grid: List<List<Int>>): List<List<Int>> {
        return List(4) { col ->
            List(4) { row ->
                grid[row][col]
            }
        }
    }

    private fun spawnRandomTile(grid: List<List<Int>>): List<List<Int>> {
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (r in 0..3) {
            for (c in 0..3) {
                if (grid[r][c] == 0) {
                    emptyPositions.add(Pair(r, c))
                }
            }
        }
        if (emptyPositions.isEmpty()) return grid

        val (row, col) = emptyPositions.random()
        val value = if (Math.random() < 0.9) 2 else 4

        return grid.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cellVal ->
                if (r == row && c == col) value else cellVal
            }
        }
    }

    private fun isGameOver(grid: List<List<Int>>): Boolean {
        for (row in 0..3) {
            for (col in 0..3) {
                if (grid[row][col] == 0) return false
            }
        }
        for (row in 0..3) {
            for (col in 0..2) {
                if (grid[row][col] == grid[row][col + 1]) return false
            }
        }
        for (row in 0..2) {
            for (col in 0..3) {
                if (grid[row][col] == grid[row + 1][col]) return false
            }
        }
        return true
    }

    // Custom text-based serialization
    private fun gridToCsv(grid: List<List<Int>>): String = grid.flatten().joinToString(",")

    private fun csvToGrid(csv: String): List<List<Int>> {
        val flatList = csv.split(",").mapNotNull { it.toIntOrNull() }
        if (flatList.size < 16) return List(4) { List(4) { 0 } }
        return List(4) { r ->
            List(4) { c ->
                flatList[r * 4 + c]
            }
        }
    }

    private fun serializeHistory(history: List<GameHistorySnapshot>): String {
        return history.joinToString(";") { snapshot ->
            "${gridToCsv(snapshot.grid)}|${snapshot.score}"
        }
    }

    private fun deserializeHistory(serialized: String): List<GameHistorySnapshot> {
        if (serialized.isEmpty()) return emptyList()
        return serialized.split(";").mapNotNull { item ->
            val parts = item.split("|")
            if (parts.size == 2) {
                val grid = csvToGrid(parts[0])
                val score = parts[1].toIntOrNull() ?: 0
                GameHistorySnapshot(grid, score)
            } else null
        }
    }
}

enum class SwipeDirection {
    LEFT, RIGHT, UP, DOWN
}
