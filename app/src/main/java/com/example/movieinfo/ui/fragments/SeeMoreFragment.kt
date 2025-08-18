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
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var gridLayoutManager: GridLayoutManager
    private var category: String? = null

    // Pagination variables
    private var currentPage = 1
    private var totalPages = 1
    private var isLoading = false
    private var isLastPage = false

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

        setupRecyclerView()
        setupUI()
        loadMovies(1, false) // Load first page
    }

    private fun setupRecyclerView() {
        movieAdapter = SeeMoreAdapter(mutableListOf()) { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        gridLayoutManager = GridLayoutManager(requireContext(), 3)

        // Set span size lookup để loading item chiếm full width
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (movieAdapter.getViewType(position) == SeeMoreAdapter.VIEW_TYPE_LOADING) {
                    3 // Chiếm full width (3 columns)
                } else {
                    1 // Movie item chiếm 1 column
                }
            }
        }

        binding.recyclerSeeMore.layoutManager = gridLayoutManager
        binding.recyclerSeeMore.adapter = movieAdapter

        // Add scroll listener for infinite scroll
        binding.recyclerSeeMore.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = gridLayoutManager.childCount
                val totalItemCount = gridLayoutManager.itemCount
                val firstVisibleItemPosition = gridLayoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= 3 // Minimum items to trigger
                    ) {
                        loadMoreMovies()
                    }
                }
            }
        })
    }

    private fun setupUI() {
        // Set title
        binding.tvHeaderTitle.text = when (category) {
            "popular" -> "Popular Movies"
            "now_playing" -> "Now Playing"
            "top_rated" -> "Top Rated"
            "upcoming" -> "Upcoming"
            else -> "Movies"
        }

        // Back button
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadMovies(page: Int, isLoadMore: Boolean = false) {
        if (!isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }

        isLoading = true
        showLoading(true, isLoadMore)

        lifecycleScope.launch {
            try {
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

                response?.let { movieResponse ->
                    totalPages = movieResponse.total_pages
                    currentPage = movieResponse.page

                    val movies = movieResponse.results

                    if (isLoadMore) {
                        movieAdapter.addData(movies)
                    } else {
                        movieAdapter.updateData(movies)
                    }

                    isLastPage = currentPage >= totalPages

                } ?: run {
                    Toast.makeText(requireContext(), "Unknown category", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    requireContext(),
                    "Failed to load movies. Please check your network.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
                showLoading(false, isLoadMore)
            }
        }
    }

    private fun loadMoreMovies() {
        if (!isLastPage && !isLoading) {
            loadMovies(currentPage + 1, true)
        }
    }

    private fun showLoading(isLoading: Boolean, isLoadMore: Boolean = false) {
        if (isLoadMore) {
            movieAdapter.setLoadingMore(isLoading)
        } else {
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.recyclerSeeMore.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
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