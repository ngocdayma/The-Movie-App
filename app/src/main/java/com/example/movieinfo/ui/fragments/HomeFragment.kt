package com.example.movieinfo.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.movieinfo.adapter.MovieHorizontalAdapter
import com.example.movieinfo.databinding.FragmentHomeBinding
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.retrofit.RetrofitClient
import com.example.movieinfo.viewmodel.MovieViewModel
import com.example.movieinfo.viewmodel.MovieViewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieinfo.R
import com.example.movieinfo.models.Movie
import com.example.movieinfo.ui.DetailActivity
import com.example.movieinfo.util.Constants

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
        // Gắn với Activity thay vì Fragment → giữ dữ liệu khi đổi tab
        viewModel = ViewModelProvider(requireActivity(), factory)[MovieViewModel::class.java]

        setupRecyclerViews()
        observeData()
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                navigateToSearch(query)
            }
            true
        }
        // Chỉ fetch lần đầu
        if (viewModel.popularMovies.value.isNullOrEmpty()) {
            viewModel.fetchPopularMovies(Constants.API_KEY)
            viewModel.fetchNowPlayingMovies(Constants.API_KEY)
            viewModel.fetchTopRatedMovies(Constants.API_KEY)
            viewModel.fetchUpcomingMovies(Constants.API_KEY)
        }
    }

    private fun setupRecyclerViews() {
        val onMovieClick: (Movie) -> Unit = { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }

        popularAdapter = MovieHorizontalAdapter(onMovieClick)
        binding.rvPopular.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPopular.adapter = popularAdapter

        nowPlayingAdapter = MovieHorizontalAdapter(onMovieClick)
        binding.rvNowPlaying.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvNowPlaying.adapter = nowPlayingAdapter

        topRatedAdapter = MovieHorizontalAdapter(onMovieClick)
        binding.rvTopRated.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTopRated.adapter = topRatedAdapter

        upcomingAdapter = MovieHorizontalAdapter(onMovieClick)
        binding.rvUpcoming.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvUpcoming.adapter = upcomingAdapter
    }

    private fun observeData() {
        viewModel.popularMovies.observe(viewLifecycleOwner) {
            popularAdapter.submitList(it)
        }
        viewModel.nowPlayingMovies.observe(viewLifecycleOwner) {
            nowPlayingAdapter.submitList(it)
        }
        viewModel.topRatedMovies.observe(viewLifecycleOwner) {
            topRatedAdapter.submitList(it)
        }
        viewModel.upcomingMovies.observe(viewLifecycleOwner) {
            upcomingAdapter.submitList(it)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
