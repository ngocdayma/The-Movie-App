package com.example.movieinfo.models

data class VideoResponse(
    val id: Int,
    val results: List<VideoItem>
)