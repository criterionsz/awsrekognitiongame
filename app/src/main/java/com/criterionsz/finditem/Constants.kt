package com.criterionsz.finditem

import android.Manifest

object Constants {
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    const val SCORE_INCREASE = 1

    // Time when the game is over
    const val DONE = 0L

    // Countdown time interval
    const val ONE_SECOND = 1000L

    // Total time for the game
    const val COUNTDOWN_TIME = 15000L

    // Total time before the game
    const val COUNTDOWN_TIME_BEFORE_GAME = 4000L

    val allWordsList = mutableListOf(
        "Pen",
        "Hand",
        "Paper",
        "Spoon",
        "Knife",
        "Fork",
        "Glass",
        "Scissors",
        "Mirror",
        "Jar",
        "Headphones",
        "Gloves",
        "Pillow",
        "Blanket",
        "Monitor",
        "Fridge",
        "Computer mouse",
        "Candle",
        "Lemon",
        "Magazine",
        "Shirt",
        "Cookie"
    )

    val allWordsListTrial = mutableListOf(
        "Pen",
        "Knife",
        "Scissors",
    )

    const val WRONG_HEART_IMAGE = R.drawable.broken_heart
}