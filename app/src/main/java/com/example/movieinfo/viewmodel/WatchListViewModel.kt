package com.example.movieinfo.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.util.Constants
import com.example.movieinfo.util.WatchlistManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WatchlistViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _movies = MutableLiveData<List<MovieDetail>>()
    val movies: LiveData<List<MovieDetail>> = _movies

    fun loadWatchlist(context: Context) {
        val savedIds = WatchlistManager.getWatchlist(context)
        if (savedIds.isEmpty()) {
            _movies.value = emptyList()
            return
        }

        viewModelScope.launch {
            val list = mutableListOf<MovieDetail>()
            for (idStr in savedIds) {
                val id = idStr.toIntOrNull() ?: continue
                try {
                    val detail = repository.getMovieDetail(id, Constants.API_KEY)
                    list.add(detail)
                } catch (e: Exception) {
                    Log.e("WatchlistViewModel", "Error loading movie $id: ${e.message}")
                }
            }
            _movies.value = list
        }
    }
}
