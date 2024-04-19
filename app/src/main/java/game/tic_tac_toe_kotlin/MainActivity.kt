package game.tic_tac_toe_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import game.tic_tac_toe_kotlin.databinding.ActivityMainBinding
import java.util.zip.CheckedInputStream
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Set onClick listeners for different game modes
        binding.startPve.setOnClickListener {
            StartPVE()
        }

        binding.startPvc.setOnClickListener {
            StartPVC()
        }

        binding.startPvp.setOnClickListener {
            StartPVP()
        }

        binding.joinPvp.setOnClickListener {
            JoinPVP()
        }
    }

    // Function to start Player vs Environment game mode
    fun StartPVE() {
        // Set game state and initiate game model for local data manipulation
        GameData.isHost = true
        GameData.saveGameModel(
            GameModel(
                gameState = GameState.JOINED,
                gameWithAI = false
            )
        )
        // Start GameActivity
        startActivity(Intent(this, GameActivity::class.java))
    }

    // Function to start Player vs Computer game mode
    fun StartPVC() {
        // Set game state and initiate game model for local data manipulation
        GameData.PlayerID = "X"
        GameData.isHost = true
        GameData.saveGameModel(
            GameModel(
                gameState = GameState.JOINED,
                gameWithAI = true
            )
        )
        // Start GameActivity
        startActivity(Intent(this, GameActivity::class.java))
    }

    // Function to start Player vs Player game mode
    fun StartPVP() {
        // Get player name input
        val playerName = binding.nameInput.text.toString()

        // Set game state and initiate game model for local data manipulation
        GameData.PlayerID = "X"
        GameData.isHost = true
        GameData.saveGameModel(
            GameModel(
                gameID = Random.nextInt(0 .. 9999).toString(),
                gameState = GameState.CREATED,
                playerXName = playerName
            )
        )
        // Start GameActivity
        startActivity(Intent(this, GameActivity::class.java))
    }

    // Function to join a Player vs Player game
    fun JoinPVP() {
        // Get game ID and player name input
        val gameID = binding.roomCodeInput.text.toString()
        val playerName = binding.nameInput.text.toString()

        // Check if game ID is valid
        if(!checkForIDValid(gameID)) return

        // Set player ID, identify as non-host, and load game data from Firebase
        GameData.PlayerID = "O"
        GameData.isHost = false

        Firebase.firestore.collection("Games")
            .document(gameID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val model = documentSnapshot.toObject(GameModel::class.java)

                // If game data is retrieved successfully, update game state and player name, then start GameActivity
                if(model != null) {
                    model.gameState = GameState.JOINED
                    model.playerOName = playerName

                    GameData.saveGameModel(model)
                    startActivity(Intent(this, GameActivity::class.java))
                }
                // Handle error when game session input field is empty
                else {  
                    binding.roomCodeInput.setError("Enter valid game ID...")
                } 
            }
    }

    // Function to check if game ID input is valid
    fun checkForIDValid(gameID : String) : Boolean {
        if(gameID.isEmpty()) {
            binding.roomCodeInput.setError("Enter game ID...")
            return false
        }
        return true
    }
}
