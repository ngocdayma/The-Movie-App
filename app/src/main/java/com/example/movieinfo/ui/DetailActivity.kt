package com.example.movieinfo.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.movieinfo.R

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val addToWatchlistButton = findViewById<Button>(R.id.btnAddToWatchlist)

        addToWatchlistButton.setOnClickListener {
            Toast.makeText(this, "Added to Watchlist!", Toast.LENGTH_SHORT).show()
            // TODO: Save movie to SharedPreferences or database
        }
    }
}