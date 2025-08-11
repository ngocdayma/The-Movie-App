package com.example.movieinfo.models

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val release_date: String? = null,
    val runtime: Int? = null,
    val genres: List<Genre> = emptyList(),
    val vote_average: Float? = null,
    val tagline: String? = null
) {
    val posterUrl get() = "https://image.tmdb.org/t/p/w500$poster_path"
    val backdropUrl get() = "https://image.tmdb.org/t/p/w780$backdrop_path"
}
