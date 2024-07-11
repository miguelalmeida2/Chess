package storage

import com.mongodb.MongoException
import com.mongodb.client.MongoDatabase
import domain.Board
import domain.Army
import domain.GameState
import domain.Move
import storage.mongodb.createDocument
import storage.mongodb.getCollectionWithId
import storage.mongodb.getDocument
import storage.mongodb.updateDocument
import kotlin.IllegalArgumentException

const val COLLECTION_ID = "games"

class MongoDbStorage(private val db: MongoDatabase) : Storage {
    override suspend fun gameExists(gameId: String): Boolean {
        try {
            return db.getCollectionWithId<GameState>(COLLECTION_ID).getDocument(gameId) != null
        } catch (e: MongoException) {
            // TODO: Create custom exception
            throw e
        }
    }

    override suspend fun createGame(gameId: String): Boolean {
        try {
            if (gameExists(gameId)) throw IllegalArgumentException("Game '$gameId' already exists")
            val newBoard = Board(gameId, Army.WHITE, this)
            return db.createDocument(COLLECTION_ID, GameState(gameId, newBoard.toString(), listOf()))
        } catch (e: MongoException) {
            // TODO: Create custom exception
            throw e
        }
    }

    override suspend fun saveMove(gameId: String, move: Move): Boolean {
        try {
            val game = db.getCollectionWithId<GameState>(COLLECTION_ID).getDocument(gameId)
                ?: throw IllegalArgumentException("Game '$gameId' does not exist")
            return db.getCollectionWithId<GameState>(COLLECTION_ID)
                .updateDocument(GameState(gameId, move.getBoard().toString(), game.moves + move.toString()))
        } catch (e: MongoException) {
            // TODO: Create custom exception
            throw e
        }
    }

    override suspend fun getAllMoves(gameId: String): List<String> {
        try {
            val game = db.getCollectionWithId<GameState>(COLLECTION_ID).getDocument(gameId)
                ?: throw IllegalArgumentException("Game '$gameId' does not exist")
            return game.moves
        } catch (e: MongoException) {
            // TODO: Create custom exception
            throw e
        }
    }

    override suspend fun getGameState(gameId: String): GameState {
        try {
            return db.getCollectionWithId<GameState>(COLLECTION_ID).getDocument(gameId)
                ?: throw IllegalArgumentException("Game '$gameId' does not exist")
        } catch (e: MongoException) {
            // TODO: Create custom exception
            throw e
        }
    }
}