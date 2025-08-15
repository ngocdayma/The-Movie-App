package com.example.movieinfo.ui.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.movieinfo.adapter.SeeMoreAdapter
import com.example.movieinfo.databinding.FragmentSeemoreBinding
import com.example.movieinfo.models.Movie
import com.example.movieinfo.retrofit.RetrofitClient
import com.example.movieinfo.ui.DetailActivity
import com.example.movieinfo.util.Constants
import kotlinx.coroutines.launch

class SeeMoreFragment : Fragment() {

    private var _binding: FragmentSeemoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var movieAdapter: SeeMoreAdapter
    private var category: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getString("category")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeemoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movieAdapter = SeeMoreAdapter(emptyList()) { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }
        binding.recyclerSeeMore.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerSeeMore.adapter = movieAdapter

        // Set title
        binding.tvHeaderTitle.text = when (category) {
            "popular" -> "Popular Movies"
            "now_playing" -> "Now Playing"
            "top_rated" -> "Top Rated"
            "upcoming" -> "Upcoming"
            else -> "Movies"
        }

        // Back
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        loadMovies()
    }

    private fun loadMovies() {

        if (!isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }
        showLoading(true)
        lifecycleScope.launch {
            try {
                val allMovies = mutableListOf<Movie>()
                for (page in 1..12) {
                    val response = when (category) {
                        "popular" -> RetrofitClient.api.getPopularMovies(
                            Constants.API_KEY,
                            page = page
                        )

                        "now_playing" -> RetrofitClient.api.getNowPlayingMovies(
                            Constants.API_KEY,
                            page = page
                        )

                        "top_rated" -> RetrofitClient.api.getTopRatedMovies(
                            Constants.API_KEY,
                            page = page
                        )

                        "upcoming" -> RetrofitClient.api.getUpcomingMovies(
                            Constants.API_KEY,
                            page = page
                        )

                        else -> null
                    }
                    response?.results?.let { allMovies.addAll(it) }
                }
                movieAdapter.updateData(allMovies)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to load movies. Please check your network.", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.recyclerSeeMore.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnected
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
