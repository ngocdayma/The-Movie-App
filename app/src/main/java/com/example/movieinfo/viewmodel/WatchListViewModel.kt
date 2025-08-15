package com.example.movieinfo.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.util.Constants
import com.example.movieinfo.util.WatchlistManager
import kotlinx.coroutines.launch
import java.io.IOException

// Lớp Resource để bọc trạng thái dữ liệu
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

class WatchlistViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _movies = MutableLiveData<Resource<List<MovieDetail>>>()
    val movies: LiveData<Resource<List<MovieDetail>>> = _movies

    fun loadWatchlist(context: Context) {
        val savedIds = WatchlistManager.getWatchlist(context)
        if (savedIds.isEmpty()) {
            _movies.value = Resource.Success(emptyList())
            return
        }

        _movies.value = Resource.Loading

        viewModelScope.launch {
            try {
                val list = mutableListOf<MovieDetail>()
                for (idStr in savedIds) {
                    val id = idStr.toIntOrNull() ?: continue
                    try {
                        val detail = repository.getMovieDetail(id, Constants.API_KEY)
                        list.add(detail)
                    } catch (e: IOException) { // lỗi mạng
                        Log.e("WatchlistViewModel", "Network error for movie $id: ${e.message}")
                        _movies.value = Resource.Error("No internet connection.")
                        return@launch
                    } catch (e: Exception) {
                        Log.e("WatchlistViewModel", "Error loading movie $id: ${e.message}")
                    }
                }
                _movies.value = Resource.Success(list)
            } catch (e: IOException) { // lỗi mạng
                _movies.value = Resource.Error("No internet connection")
            } catch (e: Exception) {
                _movies.value = Resource.Error("Load watchlist error")
            }
        }
    }
}
