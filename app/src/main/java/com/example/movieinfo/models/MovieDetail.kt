package com.example.movieinfo.models

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String,
    val runtime: Int?,
    val genres: List<Genre>,
    val vote_average: Float,
    val tagline: String?
) {
    val posterUrl get() = "https://image.tmdb.org/t/p/w500$poster_path"
    val backdropUrl get() = "https://image.tmdb.org/t/p/w780$backdrop_path"
}