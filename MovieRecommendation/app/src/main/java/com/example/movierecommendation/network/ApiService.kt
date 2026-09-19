package com.example.movierecommendation.network

import com.example.movierecommendation.model.Movie
import com.example.movierecommendation.model.MovieDetailResponse
import com.example.movierecommendation.model.RecommendationResponse
import com.example.movierecommendation.model.SectionResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("/genre/{genre_name}")
    suspend fun getMoviesByGenre(@Path("genre_name") genre: String): List<Movie>


    @GET("/search/{query}")
    suspend fun searchMovies(@Path("query") query: String): List<Movie>

    @GET("/random_recommendations")
    suspend fun getRandomRecommendations(): List<SectionResponse>

    @GET("/recommend/{title}")
    suspend fun getRecommendations(
        @Path("title") title: String
    ): RecommendationResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(@Path("movie_id") movieId: Int): MovieDetailResponse
}
