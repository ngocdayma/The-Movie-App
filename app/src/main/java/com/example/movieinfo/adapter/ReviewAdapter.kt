package com.example.movieinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movieinfo.models.Review
import com.example.movieinfo.R

class ReviewAdapter(private var reviewList: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAuthor: TextView = view.findViewById(R.id.tvAuthor)
        val tvContent: TextView = view.findViewById(R.id.tvContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviewList[position]
        holder.tvAuthor.text = review.author
        holder.tvContent.text = review.content
    }

    override fun getItemCount() = reviewList.size

    fun updateData(newList: List<Review>) {
        reviewList = newList
        notifyDataSetChanged()
    }
}
