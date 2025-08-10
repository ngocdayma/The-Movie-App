package com.example.movieinfo.models

data class AuthorDetails(
    val name: String,
    val username: String,
    val avatar_path: String?,
    val rating: Float?
)