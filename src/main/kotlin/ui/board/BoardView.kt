package ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import domain.Board
import domain.Piece
import domain.Square
import domain.toColumn
import domain.toRow

const val BOARD_SIDE = 8
val SQUARE_SIDE: Dp = 50.dp
val HEADER_THICKNESS: Dp = 20.dp

/**
 * Composable used to display a Tic-Tac-Toe board.
 *
 * @param board          the board
 * @param onSquareSelected the function called when a tile is selected
 */
@Composable
fun BoardView(
    board: Board?,
    onSquareSelected: (Square) -> Unit,
    selectedSquare: Square? = null,
    possibleTargets: List<Square> = listOf()
) {
    Column {
        Row(modifier = Modifier.padding(start = HEADER_THICKNESS)) {
            for (l in 'a'..'h') {
                Box(
                    modifier = Modifier
                        .size(width = SQUARE_SIDE, height = HEADER_THICKNESS),
                    contentAlignment = Alignment.Center
                ) {
                    Text(l.toString())
                }
            }
        }
        repeat(BOARD_SIDE) { rowIndex ->
            Row {
                Box(
                    modifier = Modifier
                        .size(width = HEADER_THICKNESS, height = SQUARE_SIDE),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = (BOARD_SIDE - rowIndex).toString())
                }
                repeat(BOARD_SIDE) { count ->
                    val square = Square(count.toColumn(), rowIndex.toRow())
                    val piece = board?.getPiece(square)
                    val color = if ((rowIndex + count) % 2 == 0) Color.LightGray else Color.DarkGray
                    SquareImageView(
                        piece,
                        color,
                        selected = square == selectedSquare,
                        highlight = square in possibleTargets,
                        onSelected = { onSquareSelected(square) }
                    )
                }
            }
        }
    }
}