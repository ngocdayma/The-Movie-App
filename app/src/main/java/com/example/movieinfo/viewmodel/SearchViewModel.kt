package com.example.movieinfo.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.util.Constants
import com.example.movieinfo.util.SearchHistoryManager
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _history = MutableLiveData<List<String>>()
    val history: LiveData<List<String>> get() = _history

    private val _movies = MutableLiveData<List<MovieDetail>>()
    val movies: LiveData<List<MovieDetail>> get() = _movies

    fun loadSearchHistory(context: Context) {
        _history.value = SearchHistoryManager.getSearchHistory(context)
    }

    fun performSearch(context: Context, query: String) {
        SearchHistoryManager.saveSearch(context, query)
        loadSearchHistory(context)

        viewModelScope.launch {
            try {
                val response = repository.searchMovies(Constants.API_KEY, query)

                val mappedList = response.results.map {
                    MovieDetail(
                        id = it.id,
                        title = it.title ?: "",
                        poster_path = it.poster_path,
                        backdrop_path = it.backdrop_path,
                        release_date = it.release_date ?: "",
                        vote_average = (it.vote_average ?: 0.0).toFloat(),
                        tagline = null
                    )
                }

                _movies.value = mappedList
            } catch (e: Exception) {
                _movies.value = emptyList()
            }
        }
    }

}
