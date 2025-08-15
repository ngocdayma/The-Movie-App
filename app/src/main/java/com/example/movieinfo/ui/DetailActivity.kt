package com.example.movieinfo.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.coroutines.async
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

    private lateinit var tvNoCast: TextView
    private lateinit var tvNoReview: TextView
    private lateinit var progressBar: ProgressBar

    private var movieId: Int = -1
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
        tvNoCast = findViewById(R.id.tvNoCast)
        tvNoReview = findViewById(R.id.tvNoReview)
        progressBar = findViewById(R.id.progressBarLoading)

        // Setup RecyclerView
        castAdapter = CastAdapter(emptyList())
        rvCast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCast.adapter = castAdapter

        reviewAdapter = ReviewAdapter(emptyList())
        rvReviews.layoutManager = LinearLayoutManager(this)
        rvReviews.adapter = reviewAdapter

        // Bắt đầu tải tất cả dữ liệu
        loadAllData(movieId)
    }

    private fun loadAllData(movieId: Int) {
        lifecycleScope.launch {
            showLoading(true)
            try {
                // Chạy song song 3 API
                val detailDeferred = async { repository.getMovieDetail(movieId, Constants.API_KEY) }
                val creditsDeferred = async { repository.getMovieCredits(Constants.API_KEY, movieId) }
                val reviewsDeferred = async { repository.getMovieReviews(Constants.API_KEY, movieId) }

                val detail = detailDeferred.await()
                val credits = creditsDeferred.await()
                val reviews = reviewsDeferred.await()

                // Gán dữ liệu movie detail
                tvTitle.text = detail.title
                tvReleaseDate.text = detail.release_date
                tvRating.text = String.format(Locale.US, "%.1f", detail.vote_average)
                tvOverview.text = detail.overview
                tvDuration.text = "${detail.runtime}min"
                tvGenre.text = detail.genres.joinToString { it.name }
                Glide.with(this@DetailActivity)
                    .load(Constants.IMAGE_BASE_URL + detail.poster_path)
                    .placeholder(R.drawable.img_loading)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivPoster)

                // Gán cast
                if (credits.cast.isEmpty()) {
                    tvNoCast.visibility = View.VISIBLE
                    rvCast.visibility = View.GONE
                } else {
                    tvNoCast.visibility = View.GONE
                    rvCast.visibility = View.VISIBLE
                    castAdapter.updateData(credits.cast)
                }

                // Gán reviews
                if (reviews.results.isEmpty()) {
                    tvNoReview.visibility = View.VISIBLE
                    rvReviews.visibility = View.GONE
                } else {
                    tvNoReview.visibility = View.GONE
                    rvReviews.visibility = View.VISIBLE
                    reviewAdapter.updateData(reviews.results)
                }

                setupWatchlist()

            } catch (e: Exception) {
                Log.e("DetailActivity", "Error loading data", e)
                Toast.makeText(this@DetailActivity, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
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

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
