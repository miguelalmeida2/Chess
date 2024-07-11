package domain

enum class Type(private val letter: Char, val valVectors: List<Vector>) {
    KING(
        'k', listOf(
            Vector(-1, 1), Vector(-1, 0), Vector(-1, -1),
            // Middle Up-Down Vectors
            Vector(0, 1), Vector(0, -1),
            // Right Side of Piece Vectors
            Vector(1, 1), Vector(1, 0), Vector(1, -1)
        )
    ),
    QUEEN(
        'q', listOf(    // Left Side of Piece Vectors
            Vector(-1, 1), Vector(-1, 0), Vector(-1, -1),
            // Middle Up-Down Vectors
            Vector(0, 1), Vector(0, -1),
            // Right Side of Piece Vectors
            Vector(1, 1), Vector(1, 0), Vector(1, -1)
        )
    ),
    BISHOP(
        'b', listOf(
            Vector(-1, 1),
            Vector(-1, -1),
            Vector(1, 1),
            Vector(1, -1)
        )
    ),
    ROOK(
        'r', listOf(
            Vector(-1, 0),
            Vector(1, 0),
            Vector(0, 1),
            Vector(0, -1)
        )
    ),
    KNIGHT(
        'n',
        listOf(
            // Left Side of Piece
            Vector(-2, 1), Vector(-2, -1), Vector(-1, 2), Vector(-1, -2),
            // Right Side of Piece
            Vector(2, 1), Vector(2, -1), Vector(1, 2), Vector(1, -2),
        )
    ),
    PAWN(
        'p', listOf(
            Vector(0, 1),
            Vector(0, 2),
            Vector(1, 1),
            Vector(-1, 1)
        )
    );

    override fun toString(): String {
        return letter.toString()
    }
}

enum class Army {
    BLACK, WHITE;

    val other: Army
        get() = if (this == BLACK) WHITE else BLACK

    override fun toString(): String {
        return if (this == BLACK) "b" else "w"
    }
}

class Piece(val type: Type, val army: Army) {
    var firstMove: Boolean = true

    override fun equals(other: Any?) = if (other !is Piece) false else ((type == other.type) && (army == other.army))

    override fun toString() = if (this.army == Army.WHITE) this.type.toString().uppercase() else this.type.toString()
}

fun Char.toPiece(army: Army): Piece = this.toPieceOrNull(army) ?: throw IllegalArgumentException()

fun Char.toPieceOrNull(army: Army): Piece? = when (this.uppercaseChar()) {
    'R' -> if (army == Army.WHITE) Piece(Type.ROOK, Army.WHITE) else Piece(Type.ROOK, Army.BLACK)
    'N' -> if (army == Army.WHITE) Piece(Type.KNIGHT, Army.WHITE) else Piece(Type.KNIGHT, Army.BLACK)
    'B' -> if (army == Army.WHITE) Piece(Type.BISHOP, Army.WHITE) else Piece(Type.BISHOP, Army.BLACK)
    'Q' -> if (army == Army.WHITE) Piece(Type.QUEEN, Army.WHITE) else Piece(Type.QUEEN, Army.BLACK)
    'K' -> if (army == Army.WHITE) Piece(Type.KING, Army.WHITE) else Piece(Type.KING, Army.BLACK)
    'P' -> if (army == Army.WHITE) Piece(Type.PAWN, Army.WHITE) else Piece(Type.PAWN, Army.BLACK)
    else -> null
}