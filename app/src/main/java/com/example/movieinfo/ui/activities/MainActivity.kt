package com.example.movieinfo.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.movieinfo.R
import com.example.movieinfo.databinding.ActivityMainBinding
import com.example.movieinfo.ui.fragments.HomeFragment
import com.example.movieinfo.ui.fragments.SearchFragment
import com.example.movieinfo.ui.fragments.WatchlistFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val searchFragment = SearchFragment()
    private val watchlistFragment = WatchlistFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load mặc định là Home
        loadFragment(homeFragment)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(homeFragment)
                R.id.nav_search -> loadFragment(searchFragment)
                R.id.nav_watchlist -> loadFragment(watchlistFragment)
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}