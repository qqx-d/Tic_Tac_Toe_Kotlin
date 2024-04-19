package game.tic_tac_toe_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import game.tic_tac_toe_kotlin.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding : ActivityGameBinding

    private var gameModel : GameModel? = null

    // Array to store winning positions
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

        // Fetch the game model from Firebase
        GameData.fetchGameModel()

        bindCellButtons()

        // Observe changes in the game model
        GameData.gameModel.observe(this) {
            gameModel = it
            UploadUI()
        }

        // Set onClick listener for the start game button
        binding.startGame.setOnClickListener {
            startGame()
        }

        // Start with the start button visible
        turnStartButton(true)
    }

    // Function to make the AI's move
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

    // Function to check for a winner or a draw
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

    // Function to start the game
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

    // Function to update the game model in Firebase
    fun updateGameData(model : GameModel) {
        GameData.saveGameModel(model)
    }

    // Function to update the UI based on the game model
    fun UploadUI() {
        setCellValuesUI()
        setGameStatusUI()
    }

    // Function to set the game status UI text
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

    // Function to show or hide the start game button
    fun turnStartButton(state : Boolean) {
        binding.startGame.visibility = if(state) View.VISIBLE else View.INVISIBLE
    }

    // Function to set the cell values in the UI
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

    // Function to bind onClick listeners to cell buttons
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

    // Function to handle clicks on cell buttons
    override fun onClick(v: View?) {
        gameModel?.apply {
            // Inform the player that the game has not started
            if(gameState != GameState.IN_PROGRESS){
                Toast.makeText(applicationContext, "Game not started!", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Prevent and inform the player that it's not his code now
            if(!gameID.equals("-1") && currentPlayer != GameData.PlayerID && gameWithAI == false) {
                Toast.makeText(applicationContext, "Not your turn!", Toast.LENGTH_SHORT).show()
                return
            }

            // Change of the current player and the bot's move, in case the game is in PVC mode
            var clickedCellID = (v?.tag as String).toInt()
            if(gameField[clickedCellID].isEmpty()) {
                gameField[clickedCellID] = currentPlayer

                currentPlayer = if(currentPlayer.equals("X")) "O" else "X"

                checkWinner()
                updateGameData(this)
                
                // The bot is making a move
                if (gameState == GameState.IN_PROGRESS && currentPlayer != GameData.PlayerID) {
                    makeBotMove()
                }
            }
        }
    }
}
