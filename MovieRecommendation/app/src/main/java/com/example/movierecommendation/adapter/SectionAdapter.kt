package com.example.movierecommendation.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movierecommendation.MovieDetailActivity
import com.example.movierecommendation.R
import com.example.movierecommendation.model.Section

class SectionAdapter(private var sections: List<Section>) :
    RecyclerView.Adapter<SectionAdapter.SectionViewHolder>() {

    inner class SectionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sectionTitle: TextView = itemView.findViewById(R.id.sectionTitle)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section, parent, false)
        return SectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SectionViewHolder, position: Int) {
        val section = sections[position]
        holder.sectionTitle.text = section.title

        val movieAdapter = MovieAdapter(section.movies) { movie ->
            val intent = Intent(holder.itemView.context, MovieDetailActivity::class.java)
            intent.putExtra("movie_id", movie.movie_id)
            holder.itemView.context.startActivity(intent)
        }

        holder.recyclerView.layoutManager =
            LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.recyclerView.adapter = movieAdapter
    }


    override fun getItemCount(): Int = sections.size

    fun updateSections(newSections: List<Section>) {
        sections = newSections
        notifyDataSetChanged()
    }
}
