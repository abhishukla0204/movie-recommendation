package com.example.movierecommendation

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.movierecommendation.model.MovieDetailResponse
import com.example.movierecommendation.network.RetrofitClient
import kotlinx.coroutines.launch

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var posterImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var taglineTextView: TextView
    private lateinit var overviewTextView: TextView
    private lateinit var genresTextView: TextView
    private lateinit var runtimeTextView: TextView
    private lateinit var releaseDateTextView: TextView
    private lateinit var voteAvgTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_detail)

        // Initialize views
        posterImageView = findViewById(R.id.posterImageView)
        titleTextView = findViewById(R.id.titleTextView)
        taglineTextView = findViewById(R.id.taglineTextView)
        overviewTextView = findViewById(R.id.overviewTextView)
        genresTextView = findViewById(R.id.genresTextView)
        runtimeTextView = findViewById(R.id.runtimeTextView)
        releaseDateTextView = findViewById(R.id.releaseDateTextView)
        voteAvgTextView = findViewById(R.id.voteAvgTextView)

        // Get movieId from intent
        val movieId = intent.getIntExtra("movie_id", -1)
        if (movieId == -1) {
            Toast.makeText(this, "Invalid movie ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Fetch movie details from API
        lifecycleScope.launch {
            try {
                val response: MovieDetailResponse = RetrofitClient.api.getMovieDetail(movieId)
                showMovieDetails(response)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MovieDetailActivity, "Error fetching movie", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showMovieDetails(movie: MovieDetailResponse) {
        Glide.with(this).load(movie.poster).into(posterImageView)
        titleTextView.text = movie.title
        taglineTextView.text = movie.tagline
        overviewTextView.text = movie.overview
        genresTextView.text = movie.genres
        runtimeTextView.text = "Runtime: ${movie.runtime} mins"
        releaseDateTextView.text = "Released: ${movie.release_date}"
        voteAvgTextView.text = "Rating: ${movie.vote_average}/10"
    }
}
