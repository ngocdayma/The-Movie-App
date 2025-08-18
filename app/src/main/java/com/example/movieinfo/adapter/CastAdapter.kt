package com.example.movieinfo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movieinfo.R
import com.example.movieinfo.models.Cast
import com.bumptech.glide.Glide


class CastAdapter(private var castList: List<Cast>) :
    RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    inner class CastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProfile: ImageView = view.findViewById(R.id.ivProfile)
        val tvName: TextView = view.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val cast = castList[position]
        holder.tvName.text = cast.name
        Glide.with(holder.itemView.context)
            .load("https://image.tmdb.org/t/p/w185${cast.profile_path}")
            .placeholder(R.drawable.img_loading)
            .into(holder.ivProfile)
    }

    override fun getItemCount() = castList.size

    fun updateData(newList: List<Cast>) {
        castList = newList
        notifyDataSetChanged()
    }
}
