package com.example.movierecommendation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movierecommendation.adapter.SectionAdapter
import com.example.movierecommendation.model.Section
import com.example.movierecommendation.network.RetrofitClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class HomeActivity : AppCompatActivity() {

    private lateinit var sectionRecyclerView: RecyclerView
    private lateinit var homeAdapter: SectionAdapter
    private lateinit var searchView: SearchView
    private lateinit var genreSpinner: Spinner

    private var allSections: List<Section> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Initialize UI components
        searchView = findViewById(R.id.searchView)
        genreSpinner = findViewById(R.id.genreSpinner)
        sectionRecyclerView = findViewById(R.id.sectionRecyclerView)
        sectionRecyclerView.layoutManager = LinearLayoutManager(this)
        homeAdapter = SectionAdapter(emptyList())
        sectionRecyclerView.adapter = homeAdapter

        // Coroutine error handler
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
        }

        // Setup Spinner (genre dropdown)
        val genreList = listOf("Select Genre", "Action", "Animation", "Science Fiction", "Drama", "Family", "Comedy")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genreList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genreSpinner.adapter = spinnerAdapter

        genreSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedGenre = parent.getItemAtPosition(position) as String
                if (selectedGenre != "Select Genre") {
                    fetchGenreRecommendations(selectedGenre)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Initial recommendations (genre + random)
        lifecycleScope.launch(errorHandler) {
            val genreSections = mutableListOf<Section>()
            val genresToLoad = listOf("Animation", "Science Fiction", "Drama", "Family", "Comedy")

            for (genre in genresToLoad) {
                try {
                    val genreMovies = RetrofitClient.api.getMoviesByGenre(genre)
                    genreSections.add(Section("Popular in $genre", genreMovies))
                } catch (e: Exception) {
                    Log.e("GenreFetch", "Failed to load $genre", e)
                }
            }

            try {
                val sectionResponses = RetrofitClient.api.getRandomRecommendations()
                val randomSections = sectionResponses.map {
                    Section(it.title, it.movies)
                }

                allSections = genreSections + randomSections
                homeAdapter.updateSections(allSections)
            } catch (e: Exception) {
                Log.e("Home", "Error loading random recs", e)
            }
        }

        // SearchView text query
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    homeAdapter.updateSections(allSections)
                } else {
                    lifecycleScope.launch {
                        try {
                            val searchResults = RetrofitClient.api.searchMovies(newText)
                            val resultSection = Section("Search Results", searchResults)
                            homeAdapter.updateSections(listOf(resultSection))
                        } catch (e: Exception) {
                            Log.e("Search", "Failed", e)
                            Toast.makeText(this@HomeActivity, "Error searching", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                return true
            }
        })
    }

    private fun fetchGenreRecommendations(genre: String) {
        val encoded = URLEncoder.encode(genre, StandardCharsets.UTF_8.toString())
        lifecycleScope.launch {
            try {
                val genreMovies = RetrofitClient.api.getMoviesByGenre(encoded)
                val section = Section("Recommended in $genre", genreMovies)
                homeAdapter.updateSections(listOf(section))
            } catch (e: Exception) {
                Log.e("GenreFetch", "Failed to load $genre", e)
                Toast.makeText(this@HomeActivity, "Error loading $genre movies", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
