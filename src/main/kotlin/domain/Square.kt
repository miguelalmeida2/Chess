package domain

class Vector(val x: Int, val y: Int) {
    operator fun plus(v: Vector): Vector {
        return Vector(this.x + v.x, this.y + v.y)
    }

    operator fun minus(v: Vector): Vector {
        return Vector(this.x + v.x, this.y + v.y)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Vector)
            return this.x == other.x && this.y == other.y
        return false
    }

    operator fun div(v: Vector): Boolean {
        return ((this.x % v.x == 0) && (this.y % v.y == 0))
    }
}

class Square(val col: Column, val row: Row) {
    operator fun plus(v: Vector): Square {
        return Square((col.ordinal + v.x).toColumn(), (row.ordinal + v.y).toRow())
    }

    operator fun minus(v: Vector): Square {
        return Square((col.ordinal - v.x).toColumn(), (row.ordinal - v.y).toRow())
    }

    operator fun minus(s: Square): Vector {
        return Vector(this.col.ordinal - (s.col.ordinal), this.row.ordinal - (s.row.ordinal))
    }

    operator fun div(v: Vector): Boolean {
        return ((this.col.ordinal % v.x == 0) && (this.row.ordinal % v.y == 0))
    }

    override fun equals(other: Any?): Boolean {
        if (other is Square)
            return this.col.ordinal == other.col.ordinal && this.row.ordinal == other.row.ordinal
        return false
    }

    override fun toString() = "$col$row"
}

fun String.toSquare(): Square {
    val col = this.elementAt(0).toColumnOrNull() ?: throw IllegalArgumentException("Invalid syntax")
    val row = this.elementAt(1).toRowOrNull() ?: throw IllegalArgumentException("Invalid syntax")
    return Square(col, row)
}