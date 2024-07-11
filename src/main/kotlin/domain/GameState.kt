package domain

import storage.Storage

data class GameState(val _id: String, val board: String, val moves: List<String>)

/**
 * Sum type used to define the game's state.
 * @see [GameNotStarted]
 * @see [GameStarted]
 */
sealed class Game

/**
 * Singleton that represents games that have not yet been started.
 */
object GameNotStarted : Game() {

    /**
     * Starts a game. The game's plays are published to the given repository.
     *
     * @param localPlayerArmy   the local player army
     * @param storage           the storage to where the game's plays are to be saved
     */
    suspend fun start(gameId: String, localPlayerArmy: Army, storage: Storage): GameStarted {
        val initialBoard = Board(gameId, localPlayerArmy, storage)
        val gameState = GameStarted(initialBoard, localPlayerArmy, storage)
        if (gameState.isLocalPlayerTurn() && !storage.gameExists(gameId)) {
            storage.createGame(gameId)
        }
        return gameState.refresh()
    }
}

/**
 * Represents started games, whose state is published to the given repository.
 *
 * @param board             the game board
 * @param localPlayerArmy   the army of the local player
 * @param storage           the storage to where the game's plays are to be saved
 */
data class GameStarted(
    val board: Board,
    val localPlayerArmy: Army,
    val storage: Storage
) : Game() {
    /**
     * Checks whether it's the local player turn to play
     */
    fun isLocalPlayerTurn() = localPlayerArmy == board.currPlayerArmy()

    /**
     * Makes a move, if it's the local player turn.
     * @param move    the coordinates of the play to be made
     * @return the new [GameStarted] instance
     * @throws IllegalStateException if it's not the local player turn to play
     */
    suspend fun makeMove(move: String): GameStarted = copy(board = board.makeMove(move))

    /**
     * Creates a new instance from the data in storage
     */
    suspend fun refresh(): GameStarted = copy(board = board.refresh())
}