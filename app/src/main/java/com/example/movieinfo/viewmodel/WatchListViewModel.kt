package com.example.movieinfo.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.util.WatchlistManager
import kotlinx.coroutines.launch

// Resource wrapper để xử lý Loading / Success / Error
sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}

class WatchlistViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _movies = MutableLiveData<Resource<List<MovieDetail>>>()
    val movies: LiveData<Resource<List<MovieDetail>>> = _movies

    fun loadWatchlist(context: Context) {
        viewModelScope.launch {
            _movies.value = Resource.Loading
            try {
                val movieIds = WatchlistManager.getWatchlist(context)
                val movieDetails = mutableListOf<MovieDetail>()
                for (idString in movieIds) {
                    val id = idString.toIntOrNull()
                    if (id != null) {
                        val detail = repository.getMovieDetail(id, com.example.movieinfo.util.Constants.API_KEY)
                        movieDetails.add(detail)
                    }
                }
                _movies.value = Resource.Success(movieDetails)
            } catch (e: Exception) {
                _movies.value = Resource.Error(e.message ?: "Unknown error")
            }
        }
    }
}