package storage

import domain.GameState
import domain.Move

interface Storage {
    suspend fun gameExists(gameId: String): Boolean

    suspend fun createGame(gameId: String): Boolean

    suspend fun saveMove(gameId: String, move: Move): Boolean

    suspend fun getAllMoves(gameId: String): List<String>

    suspend fun getGameState(gameId: String): GameState
}