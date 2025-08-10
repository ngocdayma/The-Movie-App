package com.example.movieinfo.models

data class Cast(
    val id: Int,
    val name: String,
    val character: String,
    val profile_path: String?
) {
    val profileUrl get() = "https://image.tmdb.org/t/p/w185$profile_path"
}