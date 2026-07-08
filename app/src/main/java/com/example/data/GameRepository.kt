package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow

class GameRepository(private val context: Context) {
    private val database: GameDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            GameDatabase::class.java,
            "game_database"
        )
        .fallbackToDestructiveMigration(dropAllTables = true) // simple migration fallback for prototype updates
        .build()
    }

    private val gameDao = database.gameDao()

    fun getSessionFlow(): Flow<GameSessionEntity?> {
        return gameDao.getSession()
    }

    suspend fun saveSession(session: GameSessionEntity) {
        gameDao.saveSession(session)
    }

    suspend fun clearSession() {
        gameDao.clearSession()
    }
}
