package com.example.movieinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieinfo.R
import com.example.movieinfo.models.MovieDetail
import com.example.movieinfo.util.Constants

class MovieVerticalAdapter(
    private val onItemClick: (MovieDetail) -> Unit
) : ListAdapter<MovieDetail, MovieVerticalAdapter.MovieViewHolder>(DiffCallback()) {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val tvGenres: TextView = itemView.findViewById(R.id.tvGenre)
        private val tvReleaseDate: TextView = itemView.findViewById(R.id.tvYear)
        private val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)

        fun bind(movie: MovieDetail) {
            tvTitle.text = movie.title
            tvRating.text = " %.1f".format(movie.vote_average ?: 0f)
            tvGenres.text = movie.genres?.joinToString { it.name }
            tvReleaseDate.text = movie.release_date ?: ""
            tvDuration.text = if ((movie.runtime ?: 0) > 0) "${movie.runtime} min" else ""

            Glide.with(itemView.context)
                .load(Constants.IMAGE_BASE_URL + movie.poster_path)
                .placeholder(R.drawable.img_loading)
                .into(ivPoster)

            itemView.setOnClickListener { onItemClick(movie) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_list, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MovieDetail>() {
        override fun areItemsTheSame(oldItem: MovieDetail, newItem: MovieDetail) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MovieDetail, newItem: MovieDetail) =
            oldItem == newItem
    }
}
