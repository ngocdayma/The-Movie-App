package com.example.movieinfo.models

data class CreditResponse(
    val id: Int,
    val cast: List<Cast>,
    val crew: List<Crew>
)