package com.kotensky.eggsinsta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.feed_item.view.*

class FeedAdapter(
    private val likedIds: ArrayList<Int>,
    private val listener: OnFeedClickListener
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return FeedViewHolder(view)
    }

    override fun getItemCount() = Int.MAX_VALUE

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.itemView.like_img?.setImageResource(
            if (likedIds.contains(position))
                R.drawable.ic_favorite_selected
            else
                R.drawable.ic_favorite
        )

        holder.itemView.like_img?.setOnClickListener {
            listener.onLikeClick(position)
        }

        holder.itemView.item_like_count_txt.text = likedIds.size.toString()
    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnFeedClickListener {
        fun onImageDoubleClick(position: Int)
        fun onLikeClick(position: Int)
    }
}