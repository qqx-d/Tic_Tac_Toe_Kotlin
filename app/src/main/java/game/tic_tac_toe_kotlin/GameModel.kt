package game.tic_tac_toe_kotlin

import kotlin.random.Random

data class GameModel(
    var gameID: String = "-1",
    var gameField: MutableList<String> = mutableListOf("","","","","","","","",""),
    var winnerID: String = "",
    var gameState: GameState = GameState.CREATED,
    var currentPlayer: String = (arrayOf("X", "O"))[Random.nextInt(2)],
    var gameWithAI: Boolean = false,

    var winnerName: String = "",
    var playerXName : String = "",
    var playerOName : String = ""
)

enum class GameState {
    CREATED,
    JOINED,
    IN_PROGRESS,
    FINISHED
}