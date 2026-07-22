package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Database
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "game_session")
data class GameSessionEntity(
    @PrimaryKey val id: Int = 0,
    val gridCsv: String, // Flattened 16 numbers, comma-separated, e.g. "0,2,0,4,..."
    val score: Int,
    val highScore: Int,
    val isGameOver: Boolean,
    val hasWon: Boolean,
    val canKeepPlaying: Boolean,
    val historyJson: String,
    val moveCount: Int = 0
)

@Dao
interface GameDao {
    @Query("SELECT * FROM game_session WHERE id = 0")
    fun getSession(): Flow<GameSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: GameSessionEntity)

    @Query("DELETE FROM game_session WHERE id = 0")
    suspend fun clearSession()
}

@Database(entities = [GameSessionEntity::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
