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

data class Tile(
    val id: Int,
    val value: Int,
    val row: Int,
    val col: Int,
    val isMerged: Boolean = false,
    val isNew: Boolean = false,
    val toRemove: Boolean = false
)

data class GameUiState(
    val grid: List<List<Int>> = List(4) { List(4) { 0 } },
    val tiles: List<Tile> = emptyList(),
    val score: Int = 0,
    val highScore: Int = 0,
    val moveCount: Int = 0,
    val scoreDelta: Int = 0,
    val isGameOver: Boolean = false,
    val hasWon: Boolean = false,
    val canKeepPlaying: Boolean = false,
    val undoHistory: List<GameHistorySnapshot> = emptyList(),
    val isLoading: Boolean = true
)

data class GameHistorySnapshot(
    val grid: List<List<Int>>,
    val score: Int,
    val moveCount: Int
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GameRepository(application)
    private var tileIdCounter = 0

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        loadSavedSession()
    }

    private fun loadSavedSession() {
        viewModelScope.launch {
            val savedSession = repository.getSessionFlow().firstOrNull()
            if (savedSession != null) {
                val grid = csvToGrid(savedSession.gridCsv)
                tileIdCounter = 1000 // Safe start index for loaded sessions
                val tiles = tilesFromGrid(grid)
                _uiState.value = GameUiState(
                    grid = grid,
                    tiles = tiles,
                    score = savedSession.score,
                    highScore = savedSession.highScore,
                    moveCount = savedSession.moveCount,
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
        tileIdCounter = 0
        var finalTiles = emptyList<Tile>()
        finalTiles = spawnRandomTile(finalTiles)
        finalTiles = spawnRandomTile(finalTiles)
        val finalGrid = generateGridFromTiles(finalTiles)

        _uiState.value = GameUiState(
            grid = finalGrid,
            tiles = finalTiles,
            score = 0,
            highScore = _uiState.value.highScore, // Retain high score across games
            moveCount = 0,
            scoreDelta = 0,
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
        if (currentState.isGameOver || (currentState.hasWon && !currentState.canKeepPlaying && has2048(currentState.grid))) return

        val currentTiles = currentState.tiles.filter { !it.toRemove }
        val (movedTiles, scoreGained, hasMoved) = moveTiles(currentTiles, direction)

        if (hasMoved) {
            val newHistory = currentState.undoHistory.toMutableList().apply {
                add(GameHistorySnapshot(currentState.grid, currentState.score, currentState.moveCount))
                if (size > 20) removeAt(0) // Keep last 20 undo snapshots
            }

            val finalTiles = spawnRandomTile(movedTiles)
            val finalGrid = generateGridFromTiles(finalTiles)

            var maxTile = 0
            for (tile in finalTiles) {
                if (!tile.toRemove && tile.value > maxTile) {
                    maxTile = tile.value
                }
            }

            val newScore = currentState.score + scoreGained
            val newHighScore = maxOf(currentState.highScore, newScore)
            val newMoveCount = currentState.moveCount + 1

            val gameOver = isGameOver(finalGrid)
            val wonNow = maxTile >= 2048
            val previouslyWon = currentState.hasWon
            val showWinScreen = wonNow && !previouslyWon

            _uiState.value = currentState.copy(
                grid = finalGrid,
                tiles = finalTiles,
                score = newScore,
                highScore = newHighScore,
                moveCount = newMoveCount,
                scoreDelta = scoreGained,
                isGameOver = gameOver,
                hasWon = previouslyWon || wonNow,
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

        val restoredTiles = tilesFromGrid(lastSnapshot.grid)

        _uiState.value = currentState.copy(
            grid = lastSnapshot.grid,
            tiles = restoredTiles,
            score = lastSnapshot.score,
            moveCount = lastSnapshot.moveCount,
            scoreDelta = 0,
            isGameOver = false,
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
                    historyJson = serializeHistory(state.undoHistory),
                    moveCount = state.moveCount
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
            "${gridToCsv(snapshot.grid)}|${snapshot.score}|${snapshot.moveCount}"
        }
    }

    private fun deserializeHistory(serialized: String): List<GameHistorySnapshot> {
        if (serialized.isEmpty()) return emptyList()
        return serialized.split(";").mapNotNull { item ->
            val parts = item.split("|")
            if (parts.size >= 2) {
                val grid = csvToGrid(parts[0])
                val score = parts[1].toIntOrNull() ?: 0
                val moveCount = if (parts.size >= 3) parts[2].toIntOrNull() ?: 0 else 0
                GameHistorySnapshot(grid, score, moveCount)
            } else null
        }
    }

    private fun tilesFromGrid(grid: List<List<Int>>): List<Tile> {
        val list = mutableListOf<Tile>()
        for (r in 0..3) {
            for (c in 0..3) {
                val value = grid[r][c]
                if (value > 0) {
                    list.add(Tile(id = tileIdCounter++, value = value, row = r, col = c))
                }
            }
        }
        return list
    }

    private fun generateGridFromTiles(tiles: List<Tile>): List<List<Int>> {
        val newGrid = MutableList(4) { MutableList(4) { 0 } }
        for (tile in tiles) {
            if (!tile.toRemove) {
                newGrid[tile.row][tile.col] = tile.value
            }
        }
        return newGrid
    }

    private fun spawnRandomTile(tiles: List<Tile>): List<Tile> {
        val active = tiles.filter { !it.toRemove }
        val emptyPositions = mutableListOf<Pair<Int, Int>>()
        for (r in 0..3) {
            for (c in 0..3) {
                if (active.none { it.row == r && it.col == c }) {
                    emptyPositions.add(Pair(r, c))
                }
            }
        }
        if (emptyPositions.isEmpty()) return tiles

        val (row, col) = emptyPositions.random()
        val value = if (Math.random() < 0.9) 2 else 4
        val newTile = Tile(id = tileIdCounter++, value = value, row = row, col = col, isNew = true)

        return tiles + newTile
    }

    private fun moveTiles(currentTiles: List<Tile>, direction: SwipeDirection): Triple<List<Tile>, Int, Boolean> {
        val activeTiles = currentTiles.filter { !it.toRemove }
        val movedTiles = mutableListOf<Tile>()
        var totalScoreGained = 0

        for (lineIdx in 0..3) {
            val lineTiles = when (direction) {
                SwipeDirection.LEFT -> activeTiles.filter { it.row == lineIdx }.sortedBy { it.col }
                SwipeDirection.RIGHT -> activeTiles.filter { it.row == lineIdx }.sortedByDescending { it.col }
                SwipeDirection.UP -> activeTiles.filter { it.col == lineIdx }.sortedBy { it.row }
                SwipeDirection.DOWN -> activeTiles.filter { it.col == lineIdx }.sortedByDescending { it.row }
            }

            var targetPos = 0
            var i = 0
            while (i < lineTiles.size) {
                val current = lineTiles[i]
                if (i + 1 < lineTiles.size && current.value == lineTiles[i + 1].value) {
                    val next = lineTiles[i + 1]
                    val mergedValue = current.value * 2
                    totalScoreGained += mergedValue

                    val (targetRow, targetCol) = getPosition(lineIdx, targetPos, direction)

                    val movedCurrent = current.copy(
                        row = targetRow,
                        col = targetCol,
                        value = mergedValue,
                        isMerged = true,
                        isNew = false,
                        toRemove = false
                    )
                    val movedNext = next.copy(
                        row = targetRow,
                        col = targetCol,
                        value = next.value,
                        isMerged = false,
                        isNew = false,
                        toRemove = true
                    )
                    movedTiles.add(movedCurrent)
                    movedTiles.add(movedNext)

                    i += 2
                    targetPos += 1
                } else {
                    val (targetRow, targetCol) = getPosition(lineIdx, targetPos, direction)

                    val movedCurrent = current.copy(
                        row = targetRow,
                        col = targetCol,
                        isMerged = false,
                        isNew = false,
                        toRemove = false
                    )
                    movedTiles.add(movedCurrent)

                    i += 1
                    targetPos += 1
                }
            }
        }

        var hasMovedAny = false
        for (tile in movedTiles) {
            val original = activeTiles.find { it.id == tile.id }
            if (original != null) {
                if (original.row != tile.row || original.col != tile.col || tile.isMerged || tile.toRemove) {
                    hasMovedAny = true
                    break
                }
            }
        }

        return Triple(movedTiles, totalScoreGained, hasMovedAny)
    }

    private fun getPosition(lineIndex: Int, pos: Int, direction: SwipeDirection): Pair<Int, Int> {
        return when (direction) {
            SwipeDirection.LEFT -> Pair(lineIndex, pos)
            SwipeDirection.RIGHT -> Pair(lineIndex, 3 - pos)
            SwipeDirection.UP -> Pair(pos, lineIndex)
            SwipeDirection.DOWN -> Pair(3 - pos, lineIndex)
        }
    }
}

enum class SwipeDirection {
    LEFT, RIGHT, UP, DOWN
}
