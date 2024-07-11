package ui.board

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import domain.Piece
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

/**
 * Composable used to display a Tic-Tac-Toe board tile.
 *
 * @param piece         the player that made a move on that tile, or null if no move has been done yet
 * @param onSelected    the function called when the tile is selected
 */
@Composable
fun SquareImageView(
    piece: Piece?,
    background: Color,
    selected: Boolean = false,
    highlight: Boolean = false,
    onSelected: (Piece?) -> Unit = { }
) = Box(
    modifier = Modifier
        .size(50.dp)
        .then(if (selected) Modifier.border(width = 2.dp, color = Color.Red) else Modifier)
        .background(background)
        .clickable(true) {
            onSelected(piece)
        }
) {
    if (highlight) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0x7700FF00),
                center = Offset(x = size.width / 2, y = size.height / 2),
                radius = (size.minDimension / 2) * 0.7F
            )
        }
    }
    piece?.let {
        val imageResource = "${piece.army}_${piece.type}.png"
        Image(
            painter = painterResource(imageResource),
            contentDescription = "Tile image"
        )
    }
}