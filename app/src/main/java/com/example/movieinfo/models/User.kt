package com.example.movieinfo.models

data class User(
    val uid: String = "",
    val email: String = "",
    val movies: List<String> = emptyList()
)
