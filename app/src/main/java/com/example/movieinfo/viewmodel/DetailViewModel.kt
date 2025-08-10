package com.example.movieinfo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.repository.MovieRepository
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _movieDetail = MutableLiveData<MovieDetail>()
    val movieDetail: LiveData<MovieDetail> get() = _movieDetail

    fun fetchMovieDetail(movieId: Int, apiKey: String) {
        viewModelScope.launch {
            try {
                val detail = repository.getMovieDetail(movieId, apiKey)
                _movieDetail.value = detail
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error: ${e.message}")
            }
        }
    }
}
