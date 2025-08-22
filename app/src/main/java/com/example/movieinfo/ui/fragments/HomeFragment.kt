package com.example.movieinfo.ui.fragments

import android.annotation.SuppressLint
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
import com.example.movieinfo.repository.AuthRepository
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.retrofit.RetrofitClient
import com.example.movieinfo.ui.activities.DetailActivity
import com.example.movieinfo.ui.activities.LoginActivity
import com.example.movieinfo.ui.activities.ProfileActivity
import com.example.movieinfo.util.Constants
import com.example.movieinfo.viewmodel.MovieViewModel
import com.example.movieinfo.viewmodel.MovieViewModelFactory

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val authRepository = AuthRepository()

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
        setupListeners()

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

    private fun setupListeners() {
        binding.imgUser.setOnClickListener {
            val intent = if (isUserLoggedIn()) {
                Intent(requireContext(), ProfileActivity::class.java)
            } else {
                Intent(requireContext(), LoginActivity::class.java)
            }
            startActivity(intent)
        }

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) navigateToSearch(query)
            true
        }
    }

    private fun setupRecyclerViews() {
        val onMovieClick: (Movie) -> Unit = { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        popularAdapter = MovieHorizontalAdapter(onMovieClick)
        nowPlayingAdapter = MovieHorizontalAdapter(onMovieClick)
        topRatedAdapter = MovieHorizontalAdapter(onMovieClick)
        upcomingAdapter = MovieHorizontalAdapter(onMovieClick)

        binding.rvPopular.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = popularAdapter
        }
        binding.rvNowPlaying.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = nowPlayingAdapter
        }
        binding.rvTopRated.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = topRatedAdapter
        }
        binding.rvUpcoming.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = upcomingAdapter
        }

        binding.btnSeeMorePopular.setOnClickListener { openSeeMoreFragment("popular") }
        binding.btnSeeMoreNowPlaying.setOnClickListener { openSeeMoreFragment("now_playing") }
        binding.btnSeeMoreTopRate.setOnClickListener { openSeeMoreFragment("top_rated") }
        binding.btnSeeMoreUpComing.setOnClickListener { openSeeMoreFragment("upcoming") }
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

    private fun isUserLoggedIn(): Boolean = authRepository.getCurrentUserId() != null

    private fun navigateToSearch(query: String) {
        val bundle = Bundle().apply { putString("search_query", query) }
        val searchFragment = SearchFragment().apply { arguments = bundle }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, searchFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openSeeMoreFragment(category: String) {
        val bundle = Bundle().apply { putString("category", category) }
        val seeMoreFragment = SeeMoreFragment().apply { arguments = bundle }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, seeMoreFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showLoading(show: Boolean) {
        _binding?.let { b ->
            if (show) {
                b.loadingLayout.animate().alpha(1f).setDuration(300).withStartAction { b.loadingLayout.visibility = View.VISIBLE }
            } else {
                b.loadingLayout.animate().alpha(0f).setDuration(300).withEndAction { b.loadingLayout.visibility = View.GONE }
            }
        }
    }

    private fun hideLoadingIfDataLoaded() {
        if (!viewModel.popularMovies.value.isNullOrEmpty()
            && !viewModel.nowPlayingMovies.value.isNullOrEmpty()
            && !viewModel.topRatedMovies.value.isNullOrEmpty()
            && !viewModel.upcomingMovies.value.isNullOrEmpty()
        ) showLoading(false)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            @Suppress("DEPRECATION")
            cm.activeNetworkInfo?.isConnected ?: false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
