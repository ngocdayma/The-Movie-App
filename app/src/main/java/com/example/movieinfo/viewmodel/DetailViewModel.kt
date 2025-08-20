package com.example.movieinfo.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieinfo.models.Cast
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.models.Review
import com.example.movieinfo.models.VideoItem
import com.example.movieinfo.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

// MovieDetailFull dùng List<VideoItem> chứ không phải List<VideoResponse>
data class MovieDetailFull(
    val detail: MovieDetail,
    val cast: List<Cast>,
    val reviews: List<Review>,
    val videos: List<VideoItem>
)

class DetailViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _movieDetailFull = MutableLiveData<MovieDetailFull>()
    val movieDetailFull: LiveData<MovieDetailFull> get() = _movieDetailFull

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun fetchMovieDetailFull(movieId: Int, apiKey: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Chạy song song 4 API
                val detailDeferred = async { repository.getMovieDetail(movieId, apiKey) }
                val creditsDeferred = async { repository.getMovieCredits(apiKey, movieId) }
                val reviewsDeferred = async { repository.getMovieReviews(apiKey, movieId) }
                val videosDeferred = async { repository.getMovieVideos(movieId, apiKey, "en-US") }

                val detail = detailDeferred.await()
                val credits = creditsDeferred.await()
                val reviewsResponse = reviewsDeferred.await()
                val videosResponse = videosDeferred.await()

                _movieDetailFull.value = MovieDetailFull(
                    detail = detail,
                    cast = credits.cast,
                    reviews = reviewsResponse.results,
                    videos = videosResponse.results
                )
            } catch (e: Exception) {
                Log.e("DetailViewModel", "Error: ${e.message}")
                _error.value = e.message ?: "Unknown error"
            } finally {
                _loading.value = false
            }
        }
    }
}
