package com.example.movieinfo.repository

import com.example.movieinfo.`interface`.MovieApi
import com.example.movieinfo.models.CreditResponse
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.models.MovieResponse
import com.example.movieinfo.models.ReviewResponse

class MovieRepository(private val api: MovieApi) {

    suspend fun getPopularMovies(apiKey: String, page: Int = 1): MovieResponse {
        return api.getPopularMovies(apiKey, page = page)
    }

    suspend fun getNowPlayingMovies(apiKey: String, page: Int = 1): MovieResponse {
        return api.getNowPlayingMovies(apiKey, page = page)
    }

    suspend fun getTopRatedMovies(apiKey: String, page: Int = 1): MovieResponse {
        return api.getTopRatedMovies(apiKey, page = page)
    }

    suspend fun getUpcomingMovies(apiKey: String, page: Int = 1): MovieResponse {
        return api.getUpcomingMovies(apiKey, page = page)
    }

    suspend fun getMovieDetail(movieId: Int, apiKey: String): MovieDetail {
        return api.getMovieDetail(movieId, apiKey)
    }

    suspend fun getMovieCredits(apiKey: String, movieId: Int): CreditResponse {
        return api.getCredits(movieId, apiKey)
    }

    suspend fun getMovieReviews(apiKey: String, movieId: Int): ReviewResponse {
        return api.getReviews(movieId, apiKey)
    }

    suspend fun searchMovies(apiKey: String, query: String) =
        api.searchMovies(apiKey, query)

    suspend fun getMovieVideos(movieId: Int, apiKey: String, language: String) =
        api.getMovieVideos(movieId, apiKey, language)
}