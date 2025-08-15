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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieinfo.R
import com.example.movieinfo.adapter.MovieHorizontalAdapter
import com.example.movieinfo.databinding.FragmentHomeBinding
import com.example.movieinfo.models.Movie
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.retrofit.RetrofitClient
import com.example.movieinfo.ui.DetailActivity
import com.example.movieinfo.util.Constants
import com.example.movieinfo.viewmodel.MovieViewModel
import com.example.movieinfo.viewmodel.MovieViewModelFactory

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MovieViewModel

    private lateinit var popularAdapter: MovieHorizontalAdapter
    private lateinit var nowPlayingAdapter: MovieHorizontalAdapter
    private lateinit var topRatedAdapter: MovieHorizontalAdapter
    private lateinit var upcomingAdapter: MovieHorizontalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = MovieRepository(RetrofitClient.api)
        val factory = MovieViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[MovieViewModel::class.java]

        setupRecyclerViews()
        observeData()

        // Chuyển sang SearchFragment khi nhấn Enter
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                navigateToSearch(query)
            }
            true
        }

        // Nếu chưa có dữ liệu thì mới load
        if (viewModel.popularMovies.value.isNullOrEmpty()) {
            if (!isNetworkAvailable(requireContext())) {
                Toast.makeText(requireContext(), "No internet connection. Please check your network.", Toast.LENGTH_SHORT).show()
                showLoading(false)
            } else {
                showLoading(true)
                viewModel.fetchPopularMovies(Constants.API_KEY)
                viewModel.fetchNowPlayingMovies(Constants.API_KEY)
                viewModel.fetchTopRatedMovies(Constants.API_KEY)
                viewModel.fetchUpcomingMovies(Constants.API_KEY)
            }
        }
    }

    private fun setupRecyclerViews() {
        val onMovieClick: (Movie) -> Unit = { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        // Popular
        popularAdapter = MovieHorizontalAdapter(onMovieClick)
        binding.rvPopular.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPopular.adapter = popularAdapter
        binding.btnSeeMorePopular.setOnClickListener {
            openSeeMoreFragment("popular")
        }

        // Now Playing
        nowPlayingAdapter = MovieHorizontalAdapter(onMovieClick)
        binding.rvNowPlaying.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvNowPlaying.adapter = nowPlayingAdapter
        binding.btnSeeMoreNowPlaying.setOnClickListener {
            openSeeMoreFragment("now_playing")
        }

        // Top Rated
        topRatedAdapter = MovieHorizontalAdapter(onMovieClick)
        binding.rvTopRated.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTopRated.adapter = topRatedAdapter
        binding.btnSeeMoreTopRate.setOnClickListener {
            openSeeMoreFragment("top_rated")
        }

        // Upcoming
        upcomingAdapter = MovieHorizontalAdapter(onMovieClick)
        binding.rvUpcoming.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvUpcoming.adapter = upcomingAdapter
        binding.btnSeeMoreUpComing.setOnClickListener {
            openSeeMoreFragment("upcoming")
        }
    }

    private fun openSeeMoreFragment(category: String) {
        val bundle = Bundle().apply {
            putString("category", category)
        }
        val seeMoreFragment = SeeMoreFragment().apply {
            arguments = bundle
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, seeMoreFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun observeData() {
        viewModel.popularMovies.observe(viewLifecycleOwner) {
            popularAdapter.submitList(it)
            hideLoadingIfDataLoaded()
        }
        viewModel.nowPlayingMovies.observe(viewLifecycleOwner) {
            nowPlayingAdapter.submitList(it)
            hideLoadingIfDataLoaded()
        }
        viewModel.topRatedMovies.observe(viewLifecycleOwner) {
            topRatedAdapter.submitList(it)
            hideLoadingIfDataLoaded()
        }
        viewModel.upcomingMovies.observe(viewLifecycleOwner) {
            upcomingAdapter.submitList(it)
            hideLoadingIfDataLoaded()
        }
    }

    private fun navigateToSearch(query: String) {
        val bundle = Bundle().apply {
            putString("search_query", query)
        }
        val searchFragment = SearchFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, searchFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            binding.loadingLayout.animate().alpha(1f).setDuration(300).withStartAction {
                binding.loadingLayout.visibility = View.VISIBLE
            }
        } else {
            binding.loadingLayout.animate().alpha(0f).setDuration(300).withEndAction {
                binding.loadingLayout.visibility = View.GONE
            }
        }
    }

    private fun hideLoadingIfDataLoaded() {
        if (!viewModel.popularMovies.value.isNullOrEmpty()
            && !viewModel.nowPlayingMovies.value.isNullOrEmpty()
            && !viewModel.topRatedMovies.value.isNullOrEmpty()
            && !viewModel.upcomingMovies.value.isNullOrEmpty()
        ) {
            showLoading(false)
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
