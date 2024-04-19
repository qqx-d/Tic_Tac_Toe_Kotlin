package game.tic_tac_toe_kotlin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

object GameData {
    private var _gameModel : MutableLiveData<GameModel> = MutableLiveData()
    var gameModel : LiveData<GameModel> = _gameModel

    var PlayerID = ""
    var isHost : Boolean = false

    fun saveGameModel(model : GameModel) {
        _gameModel.postValue(model)

        if(model.gameID == "-1") return

        Firebase.firestore.collection("Games")
            .document(model.gameID)
            .set(model)


    }

    fun fetchGameModel() {
        gameModel.value?.apply {
            if(gameID == "-1") return

            Firebase.firestore.collection("Games")
                .document(gameID)
                .addSnapshotListener { value, _ ->
                    val model = value?.toObject(GameModel::class.java)

                    _gameModel.postValue(model)
                }

        }
    }
}