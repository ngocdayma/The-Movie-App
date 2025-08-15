package com.example.movieinfo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.movieinfo.models.CreditResponse
import com.example.movieinfo.models.Movie
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.models.ReviewResponse
import com.example.movieinfo.repository.MovieRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _popularMovies = MutableLiveData<List<Movie>>()
    val popularMovies: LiveData<List<Movie>> = _popularMovies

    private val _nowPlayingMovies = MutableLiveData<List<Movie>>()
    val nowPlayingMovies: LiveData<List<Movie>> = _nowPlayingMovies

    private val _topRatedMovies = MutableLiveData<List<Movie>>()
    val topRatedMovies: LiveData<List<Movie>> = _topRatedMovies

    private val _upcomingMovies = MutableLiveData<List<Movie>>()
    val upcomingMovies: LiveData<List<Movie>> = _upcomingMovies

    private val _movieDetail = MutableLiveData<MovieDetail>()
    val movieDetail: LiveData<MovieDetail> = _movieDetail

    private val _credits = MutableLiveData<CreditResponse>()
    val credits: LiveData<CreditResponse> = _credits

    private val _reviews = MutableLiveData<ReviewResponse>()
    val reviews: LiveData<ReviewResponse> = _reviews

    fun fetchPopularMovies(apiKey: String) {
        viewModelScope.launch {
            _popularMovies.value = repository.getPopularMovies(apiKey).results.take(10)
        }
    }

    fun fetchNowPlayingMovies(apiKey: String) {
        viewModelScope.launch {
            _nowPlayingMovies.value = repository.getNowPlayingMovies(apiKey).results.take(10)
        }
    }

    fun fetchTopRatedMovies(apiKey: String) {
        viewModelScope.launch {
            _topRatedMovies.value = repository.getTopRatedMovies(apiKey).results.take(10)
        }
    }

    fun fetchUpcomingMovies(apiKey: String) {
        viewModelScope.launch {
            _upcomingMovies.value = repository.getUpcomingMovies(apiKey).results.take(10)
        }
    }

    fun fetchMovieDetail(apiKey: String, movieId: Int) {
        viewModelScope.launch {
            _movieDetail.value = repository.getMovieDetail(movieId, apiKey)
        }
    }

    fun fetchCredits(apiKey: String, movieId: Int) {
        viewModelScope.launch {
            _credits.value = repository.getMovieCredits(apiKey, movieId)
        }
    }

    fun fetchReviews(apiKey: String, movieId: Int) {
        viewModelScope.launch {
            _reviews.value = repository.getMovieReviews(apiKey, movieId)
        }
    }
}