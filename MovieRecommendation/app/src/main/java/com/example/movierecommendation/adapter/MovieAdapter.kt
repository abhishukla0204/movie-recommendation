package com.example.movierecommendation.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movierecommendation.MovieDetailActivity
import com.example.movierecommendation.R
import com.example.movierecommendation.model.Movie

class MovieAdapter(
    private val movies: List<Movie>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val posterImage: ImageView = itemView.findViewById(R.id.posterImageView)
        val titleText: TextView = itemView.findViewById(R.id.titleTextView)
        // Add more views if needed: genre, vote_average etc.
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        Glide.with(holder.itemView.context)
            .load(movie.poster)
            .into(holder.posterImage)  // ✅ correct
        holder.titleText.text = movie.title  // ✅ correct

        holder.itemView.setOnClickListener {
            onMovieClick(movie)
        }
    }


    override fun getItemCount(): Int = movies.size
}
