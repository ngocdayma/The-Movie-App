package com.example.movieinfo.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.movieinfo.adapter.MovieSearchAdapter
import com.example.movieinfo.adapter.SearchHistoryAdapter
import com.example.movieinfo.databinding.FragmentSearchBinding
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.retrofit.RetrofitClient
import com.example.movieinfo.ui.DetailActivity
import com.example.movieinfo.util.SearchHistoryManager
import com.example.movieinfo.viewmodel.SearchViewModel
import com.example.movieinfo.viewmodel.SearchViewModelFactory

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SearchViewModel
    private lateinit var historyAdapter: SearchHistoryAdapter
    private lateinit var resultsAdapter: MovieSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = MovieRepository(RetrofitClient.api)
        viewModel = ViewModelProvider(
            requireActivity(),
            SearchViewModelFactory(repository)
        )[SearchViewModel::class.java]

        setupAdapters()

        viewModel.history.observe(viewLifecycleOwner) { history ->
            historyAdapter.updateData(history)

            // Ẩn/hiện label và nút clear
            if (history.isEmpty()) {
                binding.layoutHistoryHeader.visibility = View.GONE
            } else {
                binding.layoutHistoryHeader.visibility = View.VISIBLE
            }
        }

        viewModel.movies.observe(viewLifecycleOwner) { list ->
            resultsAdapter.updateData(list)
            if (list.isEmpty()) {
                Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
            }
        }

        if (viewModel.history.value == null) {
            viewModel.loadSearchHistory(requireContext())
        }

        // Search khi nhấn Enter
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.performSearch(requireContext(), query)
            }
            true
        }

        // Xóa lịch sử
        binding.btnClearHistory.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear Search History")
                .setMessage("Are you sure you want to delete all search history?")
                .setPositiveButton("Yes") { _, _ ->
                    SearchHistoryManager.clearHistory(requireContext())
                    viewModel.loadSearchHistory(requireContext())
                    Toast.makeText(requireContext(), "Search history cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupAdapters() {
        historyAdapter = SearchHistoryAdapter(mutableListOf()) { query ->
            binding.etSearch.setText(query)
            viewModel.performSearch(requireContext(), query)
        }
        binding.rvSearchHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchHistory.adapter = historyAdapter

        resultsAdapter = MovieSearchAdapter(mutableListOf()) { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie_id", movie.id)
            startActivity(intent)
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = resultsAdapter
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
