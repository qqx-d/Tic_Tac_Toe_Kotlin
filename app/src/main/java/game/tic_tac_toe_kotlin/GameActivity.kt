package game.tic_tac_toe_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import game.tic_tac_toe_kotlin.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding : ActivityGameBinding

    private var gameModel : GameModel? = null

    private val winningPositions = arrayOf(
        intArrayOf(0, 1, 2),
        intArrayOf(3, 4, 5),
        intArrayOf(6, 7, 8),
        intArrayOf(0, 3, 6),
        intArrayOf(1, 4, 7),
        intArrayOf(2, 5, 8),
        intArrayOf(0, 4, 8),
        intArrayOf(2, 4, 6)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)

        setContentView(binding.root)

        GameData.fetchGameModel()

        bindCellButtons()

        GameData.gameModel.observe(this) {
            gameModel = it
            UploadUI()
        }

        binding.startGame.setOnClickListener {
            startGame()

        }

        turnStartButton(true)
    }

    fun makeBotMove() {
        gameModel?.apply {
            if(gameWithAI == false) return

            val emptyCells = gameField.indices.filter { gameField[it].isEmpty() }
            if (emptyCells.isNotEmpty()) {
                val botMove = emptyCells.random()
                gameField[botMove] = currentPlayer
                currentPlayer = if (currentPlayer == "X") "O" else "X"

                checkWinner()
                updateGameData(this)
            }
        }
    }

    fun checkWinner() {
        gameModel?.apply {
            for (i in winningPositions) {
                if(gameField[i[0]] == gameField[i[1]] && gameField[i[1]] == gameField[i[2]] && gameField[i[0]].isNotEmpty()) {
                    gameState = GameState.FINISHED
                    winnerID = gameField[i[0]]
                    winnerName = if (gameField[i[0]] == "X") playerXName else playerOName
                }
            }

            if(gameField.none {it.isEmpty()}) {
                gameState = GameState.FINISHED
            }

            updateGameData(this)
        }
    }

    fun startGame() {
        gameModel?.apply {
            val updatedPlayerTurn = "X"
            updateGameData(
                GameModel(
                    gameID = gameID,
                    gameWithAI = gameWithAI,
                    playerXName = playerXName,
                    playerOName = playerOName,
                    currentPlayer = if(gameWithAI) updatedPlayerTurn else currentPlayer,
                    gameState = GameState.IN_PROGRESS
                )
            )
        }
    }

    fun updateGameData(model : GameModel) {
        GameData.saveGameModel(model)
    }

    fun UploadUI() {
        setCellValuesUI()
        setGameStatusUI()
    }

    fun setGameStatusUI() {
        gameModel?.apply {
            binding.gameStatus.text =
                when(gameState) {
                    GameState.CREATED -> {
                        if(GameData.isHost)
                            turnStartButton(true)
                        "Game ID : $gameID"
                    }

                    GameState.JOINED -> {
                        if(GameData.isHost == false)
                            turnStartButton(false)

                        if(GameData.isHost) "Start Game!" else "Waiting for start"
                    }

                    GameState.IN_PROGRESS -> {
                        turnStartButton(false)

                        val currentPlayerName = if (currentPlayer == "X") playerXName else playerOName

                        if(gameID == "-1")
                            "$currentPlayer turn"
                        else
                            if(GameData.PlayerID == currentPlayer) "Your turn" else "$currentPlayerName turn"
                    }

                    GameState.FINISHED -> {
                        if(GameData.isHost)
                            turnStartButton(true)

                        if(gameID == "-1")
                            if(!winnerID.isEmpty()) "$winnerID won!" else "Draw!"
                        else
                            "$winnerName won!"
                    }
                }
        }
    }


    fun turnStartButton(state : Boolean) {
        binding.startGame.visibility = if(state) View.VISIBLE else View.INVISIBLE
    }

    fun setCellValuesUI() {
        gameModel?.apply {
            binding.cell1.text = gameField[0]
            binding.cell2.text = gameField[1]
            binding.cell3.text = gameField[2]
            binding.cell4.text = gameField[3]
            binding.cell5.text = gameField[4]
            binding.cell6.text = gameField[5]
            binding.cell7.text = gameField[6]
            binding.cell8.text = gameField[7]
            binding.cell9.text = gameField[8]
        }
    }

    fun bindCellButtons() {
        binding.cell1.setOnClickListener(this)
        binding.cell2.setOnClickListener(this)
        binding.cell3.setOnClickListener(this)
        binding.cell4.setOnClickListener(this)
        binding.cell5.setOnClickListener(this)
        binding.cell6.setOnClickListener(this)
        binding.cell7.setOnClickListener(this)
        binding.cell8.setOnClickListener(this)
        binding.cell9.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        gameModel?.apply {
            if(gameState != GameState.IN_PROGRESS){
                Toast.makeText(applicationContext, "Game not started!", Toast.LENGTH_SHORT).show()
                return
            }

            if(!gameID.equals("-1") && currentPlayer != GameData.PlayerID && gameWithAI == false) {
                Toast.makeText(applicationContext, "Not your turn!", Toast.LENGTH_SHORT).show()
                return
            }

            var clickedCellID = (v?.tag as String).toInt()

            if(gameField[clickedCellID].isEmpty()) {
                gameField[clickedCellID] = currentPlayer

                currentPlayer = if(currentPlayer.equals("X")) "O" else "X"

                checkWinner()
                updateGameData(this)

                if (gameState == GameState.IN_PROGRESS && currentPlayer != GameData.PlayerID) {
                    makeBotMove()
                }
            }
        }
    }

}
