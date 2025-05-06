package com.example.a2dgameexample

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewModel : ViewModel() {
    private val _gameState = MutableStateFlow(GameStates.NOT_PLAYING)
    val gameState: StateFlow<GameStates> = _gameState

    private val _finalScore = MutableStateFlow(0)
    val finalScore: StateFlow<Int> = _finalScore


    fun startGame() {
        _gameState.value = GameStates.RUNNING
    }

    fun pauseGame() {
        _gameState.value = GameStates.PAUSED
    }

    fun restartGame() {
        _gameState.value = GameStates.NOT_PLAYING

        _gameState.value = GameStates.RUNNING
    }

    fun gameOver(score: Int) {
        _finalScore.value = score
        _gameState.value = GameStates.GAME_OVER
    }

    fun setGameState(state: GameStates) {  //added setGameState
        _gameState.value = state
    }
}