package game.tic_tac_toe_kotlin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object GameData {
    // LiveData to hold the current game model
    private var _gameModel : MutableLiveData<GameModel> = MutableLiveData()
    var gameModel : LiveData<GameModel> = _gameModel

    // Player ID and host status
    var PlayerID = ""
    var isHost : Boolean = false

    // Function to save the game model to Firebase
    fun saveGameModel(model : GameModel) {
        // Update the local LiveData with the new game model
        _gameModel.postValue(model)

        // If the game is offline, do not save to Firebase
        if(model.gameID == "-1") return

        // Save the game model to the Firebase Firestore database
        Firebase.firestore.collection("Games")
            .document(model.gameID)
            .set(model)
    }

    // Function to fetch the game model from Firebase
    fun fetchGameModel() {
        // Retrieve the current game model
        gameModel.value?.apply {
            // If the game is offline, do not save to Firebase
            if(gameID == "-1") return

            // Listen for changes in the game model in the Firestore database
            Firebase.firestore.collection("Games")
                .document(gameID)
                .addSnapshotListener { value, _ ->
                    // Convert the Firestore data to a GameModel object
                    val model = value?.toObject(GameModel::class.java)

                    // Update the local LiveData with the new game model
                    _gameModel.postValue(model)
                }
        }
    }
}
