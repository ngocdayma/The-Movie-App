package com.example.movieinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieinfo.R
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.util.Constants
import java.util.Locale

class MovieVerticalAdapter(
    private val movies: List<MovieDetail>,
    private val onItemClick: (MovieDetail) -> Unit
) : RecyclerView.Adapter<MovieVerticalAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPoster: ImageView = view.findViewById(R.id.ivPoster)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val tvGenre: TextView = view.findViewById(R.id.tvGenre)
        val tvYear: TextView = view.findViewById(R.id.tvYear)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(movies[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_list, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]

        // Load poster
        Glide.with(holder.itemView.context)
            .load(Constants.IMAGE_BASE_URL + movie.poster_path)
            .into(holder.ivPoster)

        // Title
        holder.tvTitle.text = movie.title

        // Rating
        holder.tvRating.text = String.format(Locale.US, " %.1f", movie.vote_average)

        // Year
        holder.tvYear.text = movie.release_date.take(4)

        // Duration
        holder.tvDuration.text = movie.runtime?.takeIf { it > 0 }?.let { "$it min" } ?: ""

        // Genre: map từ list genre_names nếu có
        val genreNames = movie.genres?.joinToString(", ") { it.name } ?: ""
        holder.tvGenre.text = genreNames
    }

    override fun getItemCount() = movies.size
}
