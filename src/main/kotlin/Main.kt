// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import domain.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import storage.Storage
import storage.DbMode
import storage.MongoDbStorage
import storage.getDBConnectionInfo
import storage.mongodb.createMongoClient
import ui.MoveHistoryView
import ui.board.BoardView

private typealias StartGameAction = (String) -> Unit

@Composable
@Preview
fun MainWindow(storage: Storage, onCloseRequested: () -> Unit) = Window(
    onCloseRequest = onCloseRequested,
    title = "Chess",
    resizable = false
) {
    val gameState: MutableState<Game> = remember { mutableStateOf(GameNotStarted) }

    val moveHistoryState: MutableState<List<String>> = remember { mutableStateOf(listOf()) }

    val selectedSquareState = remember { mutableStateOf<Square?>(null) }

    val possibleTargetsState = remember { mutableStateOf<List<Square>>(listOf()) }

    val startGameAction = remember { mutableStateOf<StartGameAction?>(null) }

    val coroutineScope = rememberCoroutineScope()

    fun refreshGame(game: GameStarted) {
        coroutineScope.launch {
            // println("Refreshing")
            gameState.value = game.refresh()
            moveHistoryState.value = game.board.getAllMoves()
        }
    }

    fun startGame(gameId: String) {
        coroutineScope.launch {
            gameState.value = (gameState.value as GameNotStarted).start(gameId, Army.WHITE, storage)
        }
    }

    fun joinGame(gameId: String) {
        coroutineScope.launch {
            gameState.value = (gameState.value as GameNotStarted).start(gameId, Army.BLACK, storage)
        }
    }

    // TODO: There's something wrong here
    LaunchedEffect(gameState.value) {
        // println("Launching effect")
        val currGameState = gameState.value
        while (true) {
            if (currGameState is GameStarted && !currGameState.isLocalPlayerTurn()) {
                refreshGame(currGameState)
            }
            delay(5_000)
        }
    }

    fun squareSelected(square: Square) {
        val currSelectedSquare = selectedSquareState.value
        val currPossibleTargets = possibleTargetsState.value
        val currGameState = gameState.value
        if (currGameState is GameStarted && currGameState.isLocalPlayerTurn()) {
            println("game started")
            if (currSelectedSquare != null && square in currPossibleTargets) {
                println("should make move")
                // make move
                coroutineScope.launch {
                    val pieceToMove = currGameState.board.getPiece(currSelectedSquare)
                    println("$pieceToMove$currSelectedSquare$square")
                    currGameState.makeMove("$pieceToMove$currSelectedSquare$square")
                    refreshGame(currGameState)
                }
                selectedSquareState.value = null
                possibleTargetsState.value = listOf()
            } else if (currGameState.board.getPiece(square)?.army == currGameState.localPlayerArmy) {
                println("selecting piece")
                // select moving piece
                selectedSquareState.value = square
                possibleTargetsState.value = currGameState.board.getPossibleTargets(square)
            } else {
                println("other")
                selectedSquareState.value = null
                possibleTargetsState.value = listOf()
            }
        }
    }

    MainWindowMenu(
        gameState.value,
        onStartRequested = { startGameAction.value = ::startGame },
        onJoinRequested = { startGameAction.value = ::joinGame },
        onForfeitRequested = {} // TODO
    )

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFB5651D))
        ) {
            when (val currGameState = gameState.value) {
                is GameNotStarted -> GameNotStartedContent()
                is GameStarted -> GameStartedContent(
                    currGameState,
                    ::squareSelected,
                    selectedSquareState.value,
                    possibleTargetsState.value,
                    moveHistoryState.value
                )
            }
        }
    }

    val currStartGameAction = startGameAction.value
    if (currStartGameAction != null) {
        GetGameId(
            onGameIdEntered = { gameId ->
                currStartGameAction.invoke(gameId)
                startGameAction.value = null
            },
            onCancel = { startGameAction.value = null }
        )
    }
}

/**
 * The [MainWindow]'s menu
 */
@Composable
private fun FrameWindowScope.MainWindowMenu(
    state: Game,
    onStartRequested: () -> Unit,
    onJoinRequested: () -> Unit,
    onForfeitRequested: () -> Unit
) = MenuBar {

    data class MenuState(val start: Boolean, val join: Boolean, val forfeit: Boolean)

    val menuState = MenuState(
        start = state is GameNotStarted,
        join = state is GameNotStarted,
        forfeit = state is GameStarted && state.isLocalPlayerTurn()
    )

    Menu("Game") {
        Item("Start", enabled = menuState.start, onClick = onStartRequested)
        Item("Join", enabled = menuState.join, onClick = onJoinRequested)
    }
    Menu("Options") {
        Item("Forfeit", enabled = menuState.forfeit, onClick = onForfeitRequested)
    }
}


/**
 * Composable used to specify the [MainWindow] content when the application is in the [GameStarted]
 */
@Composable
private fun GameStartedContent(
    state: GameStarted,
    onSquareSelected: (at: Square) -> Unit,
    selectedSquare: Square?,
    possibleTargets: List<Square>,
    moveHistory: List<String>
) {

    Row {
        Column {
            BoardView(
                state.board,
                onSquareSelected = onSquareSelected,
                selectedSquare = selectedSquare,
                possibleTargets = possibleTargets
            )
            Text(
                text = "Game: ${state.board.gameId}" +
                        " | " +
                        "You: ${state.localPlayerArmy.name}" +
                        " | " +
                        if (state.isLocalPlayerTurn()) "Your turn" else "Waiting...",
                modifier = Modifier.padding(10.dp)
            )
            if (state.board.isGameOver()) {
                val winner = state.board.currPlayerArmy().other
                Text(text = "CHECKMATE! $winner Wins", modifier = Modifier.padding(10.dp))
            } else if (state.board.isCheck()) {
                Text(text = "Check", modifier = Modifier.padding(10.dp))
            }
        }
        MoveHistoryView(moveHistory)
    }
}

/**
 * Composable used to specify the [MainWindow] content when the application is in the [GameNotStarted]
 */
@Composable
private fun GameNotStartedContent() {
    Row {
        BoardView(
            null,
            onSquareSelected = { }
        )
    }
}

fun main() = application {
    val dbInfo = getDBConnectionInfo()
    val driver =
        if (dbInfo.mode == DbMode.REMOTE) createMongoClient(dbInfo.connectionString)
        else createMongoClient()
    val mongoDbStorage = MongoDbStorage(driver.getDatabase(dbInfo.dbName))

    MainWindow(mongoDbStorage, ::exitApplication)
}
