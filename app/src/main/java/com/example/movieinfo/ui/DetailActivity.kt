package com.example.movieinfo.ui

import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieinfo.R
import com.example.movieinfo.adapter.CastAdapter
import com.example.movieinfo.adapter.ReviewAdapter
import com.example.movieinfo.repository.MovieRepository
import com.example.movieinfo.retrofit.RetrofitClient
import com.example.movieinfo.util.Constants
import com.example.movieinfo.util.WatchlistManager
import com.example.movieinfo.viewmodel.DetailViewModel
import com.example.movieinfo.viewmodel.DetailViewModelFactory
import kotlinx.coroutines.launch
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var ivPoster: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvReleaseDate: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvOverview: TextView
    private lateinit var cbWatchlist: CheckBox
    private lateinit var tvDuration: TextView
    private lateinit var tvGenre: TextView

    private lateinit var rvCast: RecyclerView
    private lateinit var rvReviews: RecyclerView
    private lateinit var castAdapter: CastAdapter
    private lateinit var reviewAdapter: ReviewAdapter

    private var movieId: Int = -1

    private lateinit var viewModel: DetailViewModel
    private val repository by lazy { MovieRepository(RetrofitClient.api) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Nhận movieId từ Intent
        movieId = intent.getIntExtra("movie_id", -1)
        if (movieId == -1) {
            Toast.makeText(this, "Invalid movie ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Gắn view
        ivPoster = findViewById(R.id.imagePoster)
        tvTitle = findViewById(R.id.textTitle)
        tvReleaseDate = findViewById(R.id.textReleaseDate)
        tvRating = findViewById(R.id.textRating)
        tvOverview = findViewById(R.id.textOverview)
        cbWatchlist = findViewById(R.id.checkboxWatchlist)
        tvDuration = findViewById(R.id.textDuration)
        tvGenre = findViewById(R.id.textGenre)
        rvCast = findViewById(R.id.rvCast)
        rvReviews = findViewById(R.id.rvReviews)

        // Setup RecyclerView
        castAdapter = CastAdapter(emptyList())
        rvCast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCast.adapter = castAdapter

        reviewAdapter = ReviewAdapter(emptyList())
        rvReviews.layoutManager = LinearLayoutManager(this)
        rvReviews.adapter = reviewAdapter

        // Setup ViewModel
        val repository = MovieRepository(RetrofitClient.api)
        val factory = DetailViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[DetailViewModel::class.java]

        // Gọi API
        viewModel.fetchMovieDetail(movieId, Constants.API_KEY)

        // Quan sát dữ liệu
        viewModel.movieDetail.observe(this) { detail ->
            tvTitle.text = detail.title
            tvReleaseDate.text = detail.release_date
            tvRating.text = String.format(Locale.US, " %.1f", detail.vote_average)
            tvOverview.text = detail.overview
            tvDuration.text = "${detail.runtime}min"
            tvGenre.text = detail.genres.joinToString { it.name }

            val fullUrl = Constants.IMAGE_BASE_URL + detail.poster_path

            Glide.with(this)
                .load(fullUrl)
                .into(ivPoster)
            Log.d("ImageURL", fullUrl)

        }

        loadExtraData(movieId)

        setupWatchlist()
    }

    private fun loadExtraData(movieId: Int) {
        lifecycleScope.launch {
            try {
                val credits = repository.getMovieCredits(Constants.API_KEY, movieId)
                castAdapter.updateData(credits.cast)

                val reviews = repository.getMovieReviews(Constants.API_KEY, movieId)
                reviewAdapter.updateData(reviews.results)
            } catch (e: Exception) {
                Log.e("DetailActivity", "Error loading extra data", e)
            }
        }
    }

    private fun setupWatchlist() {
        cbWatchlist.isChecked = WatchlistManager.isInWatchlist(this, movieId)

        cbWatchlist.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                WatchlistManager.addToWatchlist(this, movieId)
            } else {
                WatchlistManager.removeFromWatchlist(this, movieId)
            }
        }
    }
}
