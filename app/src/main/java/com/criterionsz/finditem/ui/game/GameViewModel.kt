package com.criterionsz.finditem.ui.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.*
import com.criterionsz.finditem.Constants.COUNTDOWN_TIME
import com.criterionsz.finditem.Constants.COUNTDOWN_TIME_BEFORE_GAME
import com.criterionsz.finditem.Constants.DONE
import com.criterionsz.finditem.Constants.ONE_SECOND
import com.criterionsz.finditem.Constants.allWordsList
import com.criterionsz.finditem.Constants.allWordsListTrial
import com.criterionsz.finditem.repository.AmazonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class GameViewModel(
    private val amazonRepository: AmazonRepository
) : ViewModel() {
    private var timer: CountDownTimer? = null
    private var wordList: MutableList<String> = mutableListOf()
    private lateinit var currentWord: String
    private var wrongAnswers = 0

    // The current _word
    private val _word = MutableLiveData<String>()
    val word: LiveData<String>
        get() = _word

    // The current score
    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    // The current score
    private val _isImageClose = MutableLiveData<Boolean?>()
    val isImageClose: LiveData<Boolean?>
        get() = _isImageClose

    // The current score
    private val _isDefaultView = MutableLiveData<Boolean?>()
    val defaultView: LiveData<Boolean?>
        get() = _isDefaultView

    // Countdown time
    private val _currentTime = MutableLiveData<Long>()
    val currentTime: LiveData<Long>
        get() = _currentTime

    // Countdown time
    private val _beforeStartTime = MutableLiveData<Long>()
    val beforeStartTime: LiveData<Long>
        get() = _beforeStartTime

    // Countdown time
    private val _isCorrectAnswer = MutableLiveData<Boolean?>()
    val isCorrectAnswer: LiveData<Boolean?>
        get() = _isCorrectAnswer


    // Event which triggers the end of the game
    private val _eventGameFinish = MutableLiveData<GameState>()
    val eventGameFinish: LiveData<GameState>
        get() = _eventGameFinish

    // The String version of the current time
    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    init {
        startGame()
    }

    fun startGame() {
        _word.value = ""
        _score.value = 0
        wrongAnswers = 0
        resetList()
        nextWord()
        timer = object : CountDownTimer(COUNTDOWN_TIME_BEFORE_GAME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                _beforeStartTime.value = millisUntilFinished / ONE_SECOND
            }

            override fun onFinish() {
                _beforeStartTime.value = DONE
            }
        }

        timer?.start()
    }

    fun resetTimer() {
        timer?.cancel()
        timerOneWord()
    }

    private fun resetList() {
        wordList.clear()
        allWordsList.shuffle()
        allWordsListTrial.shuffle()
        wordList.addAll(allWordsListTrial.take(10))

    }

    private fun nextWord() {
        if (wrongAnswers >= 3) {
            clear()
            onGameFinish(GameState.LOST)
        } else {
            if (wordList.isEmpty()) {
                clear()
                onGameFinish(GameState.WON)
            } else {
                //Select and remove a _word from the list
                currentWord = wordList.removeAt(0)
                _word.value = currentWord
            }
        }
    }

    private fun timerOneWord() {
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {

            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished / ONE_SECOND
            }

            override fun onFinish() {
                _currentTime.value = DONE
            }
        }

        timer?.start()
    }

    fun getResult(tempBuffer: ByteBuffer) = viewModelScope.launch(Dispatchers.IO) {
        try {
            withContext(Dispatchers.Main) {
                timer?.cancel()
            }
            val res = amazonRepository.getResult(tempBuffer)
            withContext(Dispatchers.Main) {
                _isImageClose.value = false
                if (currentWord in res) {
                    onCorrect()
                } else {
                    onWrong()
                }
            }
        } catch (e: Exception) {
            Log.v("SecondViewModel", e.message ?: "")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("GameViewModel", "GameViewModel destroyed!")
        timer?.cancel()
    }

    /** Methods for updating the UI **/
    fun onSkip() {
        wrongAnswers++
        _isCorrectAnswer.value = false
        _isDefaultView.value = true
        nextWord()
    }

    private suspend fun onWrong() {
        wrongAnswers++
        _isCorrectAnswer.value = false
        delay(1000)
        _isDefaultView.value = true
        resetTimer()
        nextWord()
    }

    private suspend fun onCorrect() {
        _score.value = (_score.value)?.plus(1)
        _isCorrectAnswer.value = true
        delay(1000)
        _isDefaultView.value = true
        resetTimer()
        nextWord()
    }


    fun onGameFinish(state: GameState) {
        _eventGameFinish.value = state
    }

    fun clear() {
        _isImageClose.value = null
        timer?.cancel()
        timer = null
        _isCorrectAnswer.value = null
    }

    enum class GameState {
        WON, LOST
    }
}