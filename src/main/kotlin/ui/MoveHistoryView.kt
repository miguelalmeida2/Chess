package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MoveHistoryView(moves: List<String>) {
    Box(modifier = Modifier.padding(20.dp)) {
        var text = ""
        moves.forEachIndexed { index, s ->
            text += if (index % 2 == 0) {
                "${index + 1}. $s"
            } else {
                " - $s\n"
            }
        }
        Text(text = text, modifier = Modifier.background(Color.White).fillMaxSize())
    }
}