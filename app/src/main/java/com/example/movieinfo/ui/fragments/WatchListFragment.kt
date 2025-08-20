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
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.retrofit.RetrofitClient
import com.example.movieinfo.ui.activities.DetailActivity
import com.example.movieinfo.util.WatchlistManager
import com.example.movieinfo.viewmodel.Resource
import com.example.movieinfo.viewmodel.WatchlistViewModel
import com.example.movieinfo.viewmodel.WatchlistViewModelFactory


class WatchlistFragment : Fragment() {

    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MovieVerticalAdapter
    private lateinit var viewModel: WatchlistViewModel

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

        viewModel.movies.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(state.data)
                    if (state.data.isEmpty()) {
                        Toast.makeText(requireContext(), "Watchlist is empty", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (viewModel.movies.value == null) {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.loadWatchlist(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        if (WatchlistManager.hasChanged) {
            binding.progressBar.visibility = View.VISIBLE
            viewModel.loadWatchlist(requireContext())
            WatchlistManager.hasChanged = false
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
