package com.example.movieinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movieinfo.R

class SearchHistoryAdapter(
    private var historyList: MutableList<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQuery: TextView = itemView.findViewById(R.id.tvQuery)
        private val ivSearchIcon: ImageView = itemView.findViewById(R.id.ivSearchIcon)

        fun bind(query: String) {
            tvQuery.text = query
            itemView.setOnClickListener { onItemClick(query) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size

    fun updateData(newList: List<String>) {
        historyList.clear()
        historyList.addAll(newList)
        notifyDataSetChanged()
    }
}
