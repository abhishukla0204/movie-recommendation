package com.example.movierecommendation.model

data class Movie(
    val movie_id: Int,
    val title: String,
    val overview: String,
    val poster: String, // URL to poster image
    val vote_average: Double,
    val genres: String
)
