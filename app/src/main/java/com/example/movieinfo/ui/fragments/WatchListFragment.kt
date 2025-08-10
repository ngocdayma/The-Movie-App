package com.example.movieinfo.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieinfo.adapter.MovieVerticalAdapter
import com.example.movieinfo.databinding.FragmentWatchlistBinding
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.retrofit.RetrofitClient
import com.example.movieinfo.ui.DetailActivity
import com.example.movieinfo.util.Constants
import com.example.movieinfo.util.WatchlistManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchlistFragment : Fragment() {

    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    private val movieList = mutableListOf<MovieDetail>()
    private lateinit var adapter: MovieVerticalAdapter

    private val repository = MovieRepository(RetrofitClient.api)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadWatchlistMovies()
    }

    private fun setupRecyclerView() {
        adapter = MovieVerticalAdapter(movieList) { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        binding.rvWatchlist.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWatchlist.adapter = adapter
    }

    private fun loadWatchlistMovies() {
        val savedIds = WatchlistManager.getWatchlist(requireContext())

        movieList.clear()
        adapter.notifyDataSetChanged()

        if (savedIds.isEmpty()) {
            Toast.makeText(requireContext(), "Watchlist is empty", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            for (idStr in savedIds) {
                val id = idStr.toIntOrNull() ?: continue

                val movie = withContext(Dispatchers.IO) {
                    try {
                        repository.getMovieDetail(id, Constants.API_KEY)
                    } catch (e: Exception) {
                        Log.e("Watchlist", "Error fetching movie $id: ${e.message}")
                        null
                    }
                }

                movie?.let {
                    movieList.add(it)
                    adapter.notifyItemInserted(movieList.size - 1)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
