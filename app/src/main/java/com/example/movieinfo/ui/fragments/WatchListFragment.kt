package com.example.movieinfo.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieinfo.adapter.MovieVerticalAdapter
import com.example.movieinfo.databinding.FragmentWatchlistBinding
import com.example.movieinfo.repository.AuthRepository
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.retrofit.RetrofitClient
import com.example.movieinfo.ui.activities.DetailActivity
import com.example.movieinfo.ui.activities.LoginActivity
import com.example.movieinfo.util.WatchlistManager
import com.example.movieinfo.viewmodel.Resource
import com.example.movieinfo.viewmodel.WatchlistViewModel
import com.example.movieinfo.viewmodel.WatchlistViewModelFactory

class WatchlistFragment : Fragment() {

    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MovieVerticalAdapter
    private lateinit var viewModel: WatchlistViewModel
    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWatchlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = MovieRepository(RetrofitClient.api)
        viewModel = ViewModelProvider(
            requireActivity(),
            WatchlistViewModelFactory(repository)
        )[WatchlistViewModel::class.java]

        setupRecyclerView()
        checkLoginStatus()
    }

    private fun setupRecyclerView() {
        adapter = MovieVerticalAdapter { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }
        binding.rvWatchlist.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWatchlist.adapter = adapter
    }

    private fun checkLoginStatus() {
        val currentUserId = authRepository.getCurrentUserId()

        if (currentUserId == null) {
            // Chưa đăng nhập
            binding.rvWatchlist.visibility = View.GONE
            binding.progressBar.visibility = View.GONE

            binding.tvLoginNotification.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE

            binding.tvLoginNotification.setOnClickListener {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }

        } else {
            // Đã đăng nhập, load watchlist
            binding.tvLoginNotification.visibility = View.GONE
            binding.rvWatchlist.visibility = View.VISIBLE
            observeWatchlist()
            loadWatchlist()
        }
    }

    private fun observeWatchlist() {
        viewModel.movies.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvWatchlist.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.data.isNullOrEmpty()) {
                        binding.rvWatchlist.visibility = View.GONE
                        binding.tvEmpty.text = "You haven't saved any movies yet. Explore more!"
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvWatchlist.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        adapter.submitList(state.data)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvWatchlist.visibility = View.GONE
                    binding.tvEmpty.text = "Failed to load watchlist."
                    binding.tvEmpty.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadWatchlist() {
        if (WatchlistManager.hasChanged || viewModel.movies.value == null) {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.loadWatchlist(requireContext())
            WatchlistManager.hasChanged = false
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUserId = authRepository.getCurrentUserId()
        if (currentUserId != null) {
            loadWatchlist()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
