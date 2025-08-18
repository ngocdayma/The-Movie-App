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

class MovieSearchAdapter(
    private val movieList: MutableList<MovieDetail>,
    private val onItemClick: (MovieDetail) -> Unit
) : RecyclerView.Adapter<MovieSearchAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)
        private val tvYear: TextView = itemView.findViewById(R.id.tvYear)

        fun bind(movie: MovieDetail) {
            tvTitle.text = movie.title
            tvRating.text = " %.1f".format(movie.vote_average)
            tvYear.text = movie.release_date ?: ""

            Glide.with(itemView.context)
                .load(Constants.IMAGE_BASE_URL + movie.poster_path)
                .placeholder(R.drawable.img_logo)
                .into(ivPoster)

            itemView.setOnClickListener { onItemClick(movie) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_list, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movieList[position])
    }

    override fun getItemCount(): Int = movieList.size

    fun updateData(newList: List<MovieDetail>) {
        movieList.clear()
        movieList.addAll(newList)
        notifyDataSetChanged()
    }
}
