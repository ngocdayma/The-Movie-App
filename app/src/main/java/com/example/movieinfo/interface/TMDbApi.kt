package com.example.movieinfo.`interface`

import com.example.movieinfo.models.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDbApi {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse
}