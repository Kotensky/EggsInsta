package com.kotensky.eggsinsta

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.feed_item.view.*

class FeedAdapter(
        private val likedIds: ArrayList<Int>,
        private val listener: OnFeedClickListener
) : RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    private var lastClickTime = 0L
    private var lastClickPosition = -1
    var doubleClickItemPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed_item, parent, false)
        return FeedViewHolder(view)
    }

    override fun getItemCount() = 100000

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        holder.itemView.like_img?.setImageResource(
                if (likedIds.contains(position))
                    R.drawable.ic_favorite_selected
                else
                    R.drawable.ic_favorite
        )

        when (position) {
            0 -> {
                holder.itemView.item_img?.setImageResource(R.drawable.hello)
                holder.itemView.title_txt?.setText(R.string.hello)
            }
            itemCount - 1 -> {
                holder.itemView.item_img?.setImageResource(R.drawable.general)
                holder.itemView.title_txt?.setText(R.string.general)
            }
            else -> {
                holder.itemView.item_img?.setImageResource(R.drawable.egg_0)
                holder.itemView.title_txt?.setText(R.string.world_record_egg)
            }
        }



        if (doubleClickItemPosition == position) {

            holder.itemView.middle_like_image?.alpha = 0f
            holder.itemView.middle_like_image?.scaleX = 0.8f
            holder.itemView.middle_like_image?.scaleY = 0.8f

            holder.itemView.middle_like_image
                    .animate()
                    .cancel()

            holder.itemView.middle_like_image
                    .animate()
                    .alpha(1f)
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setStartDelay(0)
                    .setDuration((MIDDLE_LIKE_ANIM_DURATION).toLong())
                    .withEndAction {
                        holder.itemView.middle_like_image
                                .animate()
                                .scaleX(0.95f)
                                .scaleY(0.95f)
                                .setDuration((MIDDLE_LIKE_ANIM_DURATION / 2).toLong())
                                .withEndAction {
                                    holder.itemView.middle_like_image
                                            .animate()
                                            .scaleX(1f)
                                            .scaleY(1f)
                                            .setDuration((MIDDLE_LIKE_ANIM_DURATION / 2).toLong())
                                            .withEndAction {
                                                holder.itemView.middle_like_image
                                                        .animate()
                                                        .alpha(0f)
                                                        .scaleX(0.2f)
                                                        .scaleY(0.2f)
                                                        .setInterpolator(AccelerateInterpolator())
                                                        .setDuration((MIDDLE_LIKE_ANIM_DURATION / 2).toLong())
                                                        .setStartDelay((MIDDLE_LIKE_ANIM_DURATION * 2).toLong())
                                                        .start()
                                            }
                                            .start()
                                }
                                .start()
                    }
                    .start()

            doubleClickItemPosition = -1

        } else if (holder.itemView.middle_like_image?.alpha ?: 0f > 0f) {
            holder.itemView.middle_like_image
                    .animate()
                    .cancel()

            holder.itemView.middle_like_image
                    .animate()
                    .alpha(0f)
                    .setDuration((MIDDLE_LIKE_ANIM_DURATION * 2).toLong())
                    .start()
        }

        holder.itemView.more_img?.setOnClickListener {
            listener.onMoreClick(position)
        }

        holder.itemView.like_img?.setOnClickListener {
            listener.onLikeClick(position)
        }


        holder.itemView.item_img?.setOnClickListener {
            if (lastClickPosition == position &&
                    System.currentTimeMillis() - lastClickTime <= DOUBLE_CLICK_INTERVAL_MILLIS) {

                listener.onImageDoubleClick(position)

                lastClickPosition = -1
                lastClickTime = 0L
            } else {
                lastClickPosition = position
                lastClickTime = System.currentTimeMillis()
            }
        }

        holder.itemView.item_like_count_txt.text = likedIds.size.toString()
    }

    inner class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    interface OnFeedClickListener {
        fun onImageDoubleClick(position: Int)
        fun onLikeClick(position: Int)
        fun onMoreClick(position: Int)
    }

    companion object {
        const val DOUBLE_CLICK_INTERVAL_MILLIS = 400
        const val MIDDLE_LIKE_ANIM_DURATION = 400
    }
}