package domain

import storage.Storage

class Board(val gameId: String?, private val playerArmy: Army?, val storage: Storage) {
    private val board: Array<Array<Piece?>> = newBoard()
    private var currPlayerArmy: Army = Army.WHITE
    private var isGameCheck = false
    private var lastMove = ""
    private var endGame = false

    private fun newBoard(): Array<Array<Piece?>> {
        return arrayOf(
            arrayOf(
                Piece(Type.ROOK, Army.BLACK),
                Piece(Type.KNIGHT, Army.BLACK),
                Piece(Type.BISHOP, Army.BLACK),
                Piece(Type.QUEEN, Army.BLACK),
                Piece(Type.KING, Army.BLACK),
                Piece(Type.BISHOP, Army.BLACK),
                Piece(Type.KNIGHT, Army.BLACK),
                Piece(Type.ROOK, Army.BLACK)
            ),
            Array(8) { Piece(Type.PAWN, Army.BLACK) },
            arrayOfNulls(8),
            arrayOfNulls(8),
            arrayOfNulls(8),
            arrayOfNulls(8),
            Array(8) { Piece(Type.PAWN, Army.WHITE) },
            arrayOf(
                Piece(Type.ROOK, Army.WHITE),
                Piece(Type.KNIGHT, Army.WHITE),
                Piece(Type.BISHOP, Army.WHITE),
                Piece(Type.QUEEN, Army.WHITE),
                Piece(Type.KING, Army.WHITE),
                Piece(Type.BISHOP, Army.WHITE),
                Piece(Type.KNIGHT, Army.WHITE),
                Piece(Type.ROOK, Army.WHITE)
            )
        )
    }

    override fun toString() = board.joinToString("") { row ->
        row.joinToString("") {
            it?.toString() ?: " "
        }
    }

    fun currPlayerArmy() = currPlayerArmy
    fun isCheck() = isGameCheck
    fun isGameOver() = endGame

    private fun didLastMoveResultInCheck(move: String): Boolean {
        if (lastMove.isEmpty())
            return false
        val tmpMove = Move(this, move)
        val nextMoves = getPossibleTargets(tmpMove.to)
        nextMoves.forEach {
            val tmp = board[it.row.ordinal][it.col.ordinal]
            if (tmp != null && tmp.type == Type.KING && tmp.army == currPlayerArmy) {
                println("Check")
                isGameCheck = true
                return true
            }
        }
        return false
    }

    private fun canKingMove(): Boolean {
        var listOfPossibleKingMoves: MutableList<Square> = mutableListOf()
        for (row in board.indices) {
            for (col in board[row].indices) {
                val tmpPiece = getPiece(Square(col.toColumn(), row.toRow())) ?: continue
                if (tmpPiece.type == Type.KING && tmpPiece.army == currPlayerArmy()) {
                    val kingSquare = Square(col.toColumn(), row.toRow())
                    listOfPossibleKingMoves = getPossibleTargets(kingSquare) as MutableList<Square>
                }
            }
        }
        for (row in board.indices) {
            for (col in board[row].indices) {
                val tmpPiece = getPiece(Square(col.toColumn(), row.toRow())) ?: continue
                if (tmpPiece.army != currPlayerArmy()) {
                    val enemyPieceMoves = getPossibleTargets(Square(col.toColumn(), row.toRow()))
                    enemyPieceMoves.forEach {
                        if (it in listOfPossibleKingMoves) listOfPossibleKingMoves.remove(it)
                    }
                }
            }
        }
        return listOfPossibleKingMoves.isEmpty()
    }

    private fun promotePawn(pawnAt: Square): Board {
        board[pawnAt.row.ordinal][pawnAt.col.ordinal] = Piece(Type.QUEEN, currPlayerArmy())
        return this@Board
    }

    suspend fun makeMove(move: String): Board {
        if (didLastMoveResultInCheck(lastMove) && canKingMove()) {
            println("Checkmate! you won")
            endGame = true
            return this@Board
        }

        if (gameId == null || this.playerArmy == null) {
            throw IllegalStateException("Can't play without a game (try open or join commands).")
        }
        if (currPlayerArmy != playerArmy) {
            throw IllegalStateException("Wait for your turn (try refresh command).")
        }
        val tmpMove = Move(this, move)
        val validity = tmpMove.validate()
        if (validity.valid) {
            board[tmpMove.from.row.ordinal][tmpMove.from.col.ordinal] = null
            board[tmpMove.to.row.ordinal][tmpMove.to.col.ordinal] = tmpMove.piece
            val finalRow = if (currPlayerArmy() == Army.WHITE) 0 else 7
            if (tmpMove.to.row.ordinal == finalRow && tmpMove.piece.type == Type.PAWN) promotePawn(
                Square(
                    tmpMove.to.col,
                    tmpMove.to.row
                )
            )

            currPlayerArmy = currPlayerArmy.other
            tmpMove.piece.firstMove = false

            storage.saveMove(gameId, tmpMove)
            if (didLastMoveResultInCheck(move)) {
                isGameCheck = true
                println("Check")
            }

        } else {
            throw IllegalStateException("Illegal move: ${validity.reason}")
        }
        return this
    }

    private fun GameState.toBoard(): Board {
        val board = Board(gameId, playerArmy, storage)
        Row.values().forEach { row ->
            Column.values().forEach { col ->
                val c = this.board.elementAt(row.ordinal * 8 + col.ordinal)
                board.board[row.ordinal][col.ordinal] = c.toPieceOrNull(if (c.isUpperCase()) Army.WHITE else Army.BLACK)
            }
        }
        if (this.moves.isNotEmpty()) {
            board.lastMove = this.moves.last()
            if (didLastMoveResultInCheck(this.moves.last()) && canKingMove()) {
                println("Checkmate! You Won")
                endGame = true
            }
        }
        if (this.moves.size % 2 != 0) {
            board.currPlayerArmy = Army.BLACK
        }

        return board
    }

    suspend fun refresh(): Board {
        if (gameId == null || this.playerArmy == null) {
            throw IllegalStateException("Can't refresh without a game (try open or join commands).")
        }
        val gameState = storage.getGameState(gameId)
        return gameState.toBoard()
    }

    suspend fun getAllMoves(): List<String> {
        if (gameId == null || this.playerArmy == null) {
            throw IllegalStateException("No game, no moves (try open or join commands).")
        }
        return this.storage.getAllMoves(this.gameId)
    }

    fun getPiece(square: Square): Piece? = board[square.row.ordinal][square.col.ordinal]
}