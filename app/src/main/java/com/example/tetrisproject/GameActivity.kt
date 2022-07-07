package com.example.tetrisproject

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.tetrisproject.storage.AppPreferences

class GameActivity : AppCompatActivity() {

    var tvHighScore: TextView? = null
    var tvCurrentScore: TextView? = null
    var appPreferences: AppPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        appPreferences = AppPreferences(this)

        val btnRestart = findViewById<Button>(R.id.btn_restart)
        tvHighScore = findViewById<TextView>(R.id.tv_high_score)
        tvCurrentScore = findViewById<TextView>(R.id.tv_current_score)

        updateHighScore()
        updateCurrentScore()
    }

    private fun updateCurrentScore(){
        tvCurrentScore?.text = "0"
    }

    private fun updateHighScore() {
        tvHighScore?.text = "${appPreferences?.getHighScore()}"
    }

    private fun onRestartClick(view: View) {}
}