package com.example.movieinfo.models

data class Review(
    val author: String,
    val content: String,
    val created_at: String,
    val author_details: AuthorDetails
)