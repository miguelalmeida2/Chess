package domain

import storage.Storage

/**
 * Contract to be supported by all commands. Notice that commands are mere functions =)
 */
typealias Command = suspend (String?) -> Result

/**
 * Gets the container bearing the associations between user entered strings and the corresponding command implementation.
 * @param board the [Board] to be used by all commands
 * @return the container with the command mappings
 */
fun buildCommands(board: Board): Map<String, Command> {
    return mapOf(
        "OPEN" to { open(it, board.storage) },
        "JOIN" to { join(it, board.storage) },
        "PLAY" to { play(board, it) },
        "REFRESH" to { refresh(board) },
        "MOVES" to { moves(board) },
        "EXIT" to { exit() },
    )
}

suspend fun open(gameId: String?, storage: Storage): Result {
    if (gameId == null)
        throw IllegalStateException("Missing game name.")

    return if (storage.gameExists(gameId)) {
        ValueResult(Board(gameId, Army.WHITE, storage).refresh())
    } else {
        storage.createGame(gameId)
        ValueResult(Board(gameId, Army.WHITE, storage).refresh())
    }
}

suspend fun join(gameId: String?, storage: Storage): Result {
    if (gameId == null)
        throw IllegalStateException("Missing game name.")

    return if (storage.gameExists(gameId)) {
        ValueResult(Board(gameId, Army.BLACK, storage).refresh())
    } else {
        throw IllegalArgumentException("Game '$gameId' does not exist")
    }
}

suspend fun play(board: Board, move: String?): Result {
    if (move == null)
        throw IllegalStateException("Missing move.")
    return ValueResult(board.makeMove(move))
}

suspend fun refresh(board: Board) = ValueResult(board.refresh())

suspend fun moves(board: Board) = ValueResult(board.getAllMoves())

fun exit(): Result = ExitResult