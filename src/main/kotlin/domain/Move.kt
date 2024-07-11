package domain

data class Validity(val valid: Boolean, val reason: String? = null)

class Move(private val board: Board, move: String) {
    val piece: Piece
    val from: Square
    val to: Square

    init {
        // TODO: only destination is required in string
        val moveComplete = if (move.length == 5) move else "P$move"
        try {
            val currentPlayerArmy = board.currPlayerArmy()
            this.piece = moveComplete.elementAt(0).toPiece(currentPlayerArmy)
            this.from = moveComplete.substring(1, 3).toSquare()
            this.to = moveComplete.substring(3, 5).toSquare()

            val p = board.getPiece(this.from)
            if(p != null) this.piece.firstMove = p.firstMove

        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid syntax")
        }
    }

    fun getBoard() = this.board

    override fun toString(): String {
        return "$piece$from$to"
    }

    fun validate(): Validity {
        // Is Piece in specified position
        val p = board.getPiece(this.from) ?: return Validity(false, "There is no piece in ${this.from}")
        if (p != this.piece) {
            return Validity(false, "The specified piece is not in ${this.from}")
        }

        return if (this.to in board.getPossibleTargets(this.from)) {
            Validity(true)
        } else {
            Validity(false, "${piece.army} ${piece.type.name.lowercase()} cannot move to $to")
        }
    }
}


private fun pawnMoves(square: Square, i: Int, i1: Int, list: MutableList<Square>, board: Board, piece: Piece) {
    var tmp: Square
    if(square.col.ordinal in 0..7 && square.row.ordinal + i in 0..7){
        tmp = Square(square.col, (square.row.ordinal + i).toRow())
        if (board.getPiece(tmp) == null) {
            list.add(tmp)
            if (piece.firstMove) {
                tmp = Square(square.col, (square.row.ordinal + i1).toRow())
                if(board.getPiece(tmp) == null)
                    list.add(tmp)
            }
        }
    }
}

private fun pawnCaps(square: Square, i: Int, list: MutableList<Square>, board: Board, piece: Piece) {
    var tmp: Square
    var tmp2: Piece?
    if(square.col.ordinal + 1 in 0..7 && square.row.ordinal + i in 0..7) {
        tmp = Square((square.col.ordinal + 1).toColumn(), (square.row.ordinal + i).toRow())
        tmp2 = board.getPiece(tmp)
        if (tmp2 != null && tmp2.army != piece.army) {
            list.add(tmp)
        }
    }

    if(square.col.ordinal - 1 in 0..7 && square.row.ordinal + i in 0..7) {
        tmp = Square((square.col.ordinal - 1).toColumn(), (square.row.ordinal + i).toRow())
        tmp2 = board.getPiece(tmp)
        if (tmp2 != null && tmp2.army != piece.army) {
            list.add(tmp)
        }
    }
}

fun Board.getPossibleTargets(square: Square): List<Square> {
    val list: MutableList<Square> = mutableListOf()
    var tmp: Square
    val piece = this.getPiece(square)

    when (piece?.type) {
        Type.PAWN -> {
            // Pawn Movements
            if (piece.army == Army.WHITE) {
                pawnMoves(square,-1,-2,list,this,piece)
            } else {
                pawnMoves(square,1,2,list,this,piece)
            }
            // Pawn Captures
            if (piece.army == Army.WHITE) {
                pawnCaps(square, -1, list, this, piece)
            } else {
                pawnCaps(square, 1, list, this, piece)
            }
        }
        Type.KNIGHT , Type.KING -> {
            piece.type.valVectors.forEach {
                if((square.col.ordinal + it.x in 0..7) && (square.row.ordinal + it.y in 0..7)) {
                    tmp = Square((square.col.ordinal + it.x).toColumn(), (square.row.ordinal + it.y).toRow())
                    val tmp2 = this.getPiece(tmp)
                    if (tmp2 == null || tmp2.army != piece.army)
                        list.add(tmp)
                }
            }
        }
        Type.BISHOP , Type.ROOK, Type.QUEEN -> {
            piece.type.valVectors.forEach {
                tmp = Square(square.col, square.row)
                if ((square.col.ordinal + it.x in 0..7) && (square.row.ordinal + it.y in 0..7)) {
                    do {
                        tmp = Square((tmp.col.ordinal + it.x).toColumn(), (tmp.row.ordinal + it.y).toRow())
                        val tmp2 = this.getPiece(tmp)
                        if (tmp2?.army == piece.army)
                            break
                        if (tmp2 == null)
                            list.add(tmp)
                        else if(tmp2.army != piece.army) {
                            list.add(tmp)
                            break
                        }
                    } while ((tmp.col.ordinal + it.x in 0..7) && (tmp.row.ordinal + it.y in 0..7))
                }
            }
        }
        else -> return list
    }
    return list
}