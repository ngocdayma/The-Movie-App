package com.example.movieinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movieinfo.R
import com.example.movieinfo.models.Movie

class SeeMoreAdapter(
    private val movies: MutableList<Movie>,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_MOVIE = 0
        const val VIEW_TYPE_LOADING = 1
    }

    private var isLoadingMore = false

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgMovie: ImageView = itemView.findViewById(R.id.imgMovie)
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoadingMore && position == movies.size) {
            VIEW_TYPE_LOADING
        } else {
            VIEW_TYPE_MOVIE
        }
    }

    // Public method để fragment có thể truy cập
    fun getViewType(position: Int): Int = getItemViewType(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie, parent, false)
                MovieViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MovieViewHolder -> {
                val movie = movies[position]
                Glide.with(holder.itemView.context)
                    .load(movie.posterUrl)
                    .placeholder(R.drawable.img_logo)
                    .error(R.drawable.img_logo)
                    .into(holder.imgMovie)

                holder.itemView.setOnClickListener { onItemClick(movie) }
            }
            is LoadingViewHolder -> {
                // Loading item - no action needed, just show progress
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isLoadingMore) movies.size + 1 else movies.size
    }

    fun updateData(newMovies: List<Movie>) {
        movies.clear()
        movies.addAll(newMovies)
        notifyDataSetChanged()
    }

    fun addData(newMovies: List<Movie>) {
        val startPosition = movies.size
        movies.addAll(newMovies)
        notifyItemRangeInserted(startPosition, newMovies.size)
    }

    fun setLoadingMore(loading: Boolean) {
        if (isLoadingMore != loading) {
            val wasLoading = isLoadingMore
            isLoadingMore = loading

            if (wasLoading && !loading) {
                // Remove loading item
                notifyItemRemoved(movies.size)
            } else if (!wasLoading && loading) {
                // Add loading item
                notifyItemInserted(movies.size)
            }
        }
    }

    fun clearData() {
        movies.clear()
        notifyDataSetChanged()
    }
}