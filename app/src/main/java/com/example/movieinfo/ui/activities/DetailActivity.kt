package com.example.movieinfo.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
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
import com.example.movieinfo.viewmodel.MovieDetailFull
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var ivBackDrop: ImageView
    private lateinit var ivPoster: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvReleaseDate: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvOverview: TextView
    private lateinit var cbWatchlist: CheckBox
    private lateinit var tvDuration: TextView
    private lateinit var tvGenre: TextView
    private lateinit var btnWatchTrailer: Button

    private lateinit var rvCast: RecyclerView
    private lateinit var rvReviews: RecyclerView
    private lateinit var castAdapter: CastAdapter
    private lateinit var reviewAdapter: ReviewAdapter

    private lateinit var tvNoCast: TextView
    private lateinit var tvNoReview: TextView
    private lateinit var progressBar: ProgressBar

    private var movieId: Int = -1

    private val repository by lazy { MovieRepository(RetrofitClient.api) }
    private val viewModel: DetailViewModel by viewModels {
        DetailViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        movieId = intent.getIntExtra("movie_id", -1)
        if (movieId == -1) {
            Toast.makeText(this, "Invalid movie ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindViews()
        setupRecyclerViews()
        setupBackButton()
        setupWatchlist()

        // Quan sát LiveData từ ViewModel
        viewModel.movieDetailFull.observe(this, Observer { full ->
            full?.let { bindMovieDetailFull(it) }
        })

        viewModel.error.observe(this, Observer { msg ->
            msg?.let {
                Toast.makeText(this, "Load data failed: $it", Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.loading.observe(this, Observer { isLoading ->
            showLoading(isLoading)
        })

        // Bắt đầu fetch dữ liệu
        viewModel.fetchMovieDetailFull(movieId, Constants.API_KEY)
    }

    private fun bindViews() {
        ivBackDrop = findViewById(R.id.imageBackdrop)
        ivPoster = findViewById(R.id.imagePosterSmall)
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
        btnWatchTrailer = findViewById(R.id.btnWatchTrailer)
    }

    private fun setupRecyclerViews() {
        castAdapter = CastAdapter(emptyList())
        rvCast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCast.adapter = castAdapter

        reviewAdapter = ReviewAdapter(emptyList())
        rvReviews.layoutManager = LinearLayoutManager(this)
        rvReviews.adapter = reviewAdapter
    }

    private fun setupBackButton() {
        val btnBack: ImageView = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
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

    private fun bindMovieDetailFull(full: MovieDetailFull) {
        val detail = full.detail
        tvTitle.text = detail.title
        tvReleaseDate.text = detail.release_date
        tvRating.text = String.format(Locale.US, "%.1f", detail.vote_average)
        tvOverview.text = detail.overview
        tvDuration.text = "${detail.runtime}min"
        tvGenre.text = detail.genres.joinToString { it.name }

        Glide.with(this)
            .load(detail.backdropUrl)
            .placeholder(R.drawable.img_logo)
            .error(R.drawable.ic_launcher_background)
            .into(ivBackDrop)

        Glide.with(this)
            .load(detail.posterUrl)
            .placeholder(R.drawable.img_logo)
            .error(R.drawable.ic_launcher_background)
            .into(ivPoster)

        // Cast
        if (full.cast.isEmpty()) {
            tvNoCast.visibility = View.VISIBLE
            rvCast.visibility = View.GONE
        } else {
            tvNoCast.visibility = View.GONE
            rvCast.visibility = View.VISIBLE
            castAdapter.updateData(full.cast)
        }

        // Reviews
        if (full.reviews.isEmpty()) {
            tvNoReview.visibility = View.VISIBLE
            rvReviews.visibility = View.GONE
        } else {
            tvNoReview.visibility = View.GONE
            rvReviews.visibility = View.VISIBLE
            reviewAdapter.updateData(full.reviews)
        }

        // Trailer
        val trailer = full.videos.firstOrNull { it.type == "Trailer" && it.site == "YouTube" }
        btnWatchTrailer.setOnClickListener {
            if (trailer != null) {
                val intent = Intent(this, TrailerActivity::class.java)
                intent.putExtra("VIDEO_KEY", trailer.key)
                startActivity(intent)
            } else {
                Toast.makeText(this, "There is no trailer available!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        findViewById<View>(R.id.scrollContent).visibility = if (show) View.GONE else View.VISIBLE
    }

}
