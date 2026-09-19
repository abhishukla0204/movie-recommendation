package com.example.movierecommendation.model

data class MovieDetailResponse(
    val id: Int,
    val title: String,
    val overview: String,
    val genres: String,
    val tagline: String,
    val release_date: String,
    val vote_average: Double,
    val runtime: Int,
    val poster: String
)
