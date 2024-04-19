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

    fun StartPVE() {
        GameData.isHost = true

        GameData.saveGameModel(
            GameModel(
                gameState = GameState.JOINED,
                gameWithAI = false
            )
        )

        startActivity(Intent(this, GameActivity::class.java))
    }

    fun StartPVC() {
        GameData.PlayerID = "X"
        GameData.isHost = true

        GameData.saveGameModel(
            GameModel(
                gameState = GameState.JOINED,
                gameWithAI = true
            )
        )

        startActivity(Intent(this, GameActivity::class.java))
    }

    fun StartPVP() {
        val playerName = binding.nameInput.text.toString()

        GameData.PlayerID = "X"
        GameData.isHost = true

        GameData.saveGameModel(
            GameModel(
                gameID = Random.nextInt( 0 .. 9999).toString(),
                gameState = GameState.CREATED,
                playerXName = playerName
            )
        )

        startActivity(Intent(this, GameActivity::class.java))
    }

    fun checkForIDValid(gameID : String) : Boolean {
        if(gameID.isEmpty()) {
            binding.roomCodeInput.setError("Enter game ID...")
            return false
        }
        return true
    }


    fun JoinPVP() {
        val gameID = binding.roomCodeInput.text.toString()
        val playerName = binding.nameInput.text.toString()

        if(checkForIDValid(gameID) == false) return

        GameData.PlayerID = "O"
        GameData.isHost = false

        Firebase.firestore.collection("Games")
            .document(gameID)
            .get()
            .addOnSuccessListener {
                val model = it?.toObject(GameModel::class.java)

                if(model != null) {
                    model.gameState = GameState.JOINED
                    model.playerOName = playerName

                    GameData.saveGameModel(model)

                    startActivity(Intent(this, GameActivity::class.java))
                }
                else {  binding.roomCodeInput.setError("Enter valid game ID...") }
            }

    }
}
