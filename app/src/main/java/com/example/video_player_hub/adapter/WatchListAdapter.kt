package com.example.video_player_hub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.video_player_hub.R
import com.example.video_player_hub.room.WatchList

class WatchListAdapter(
    private var items: MutableList<WatchList>,
    private val onViewClick: (WatchList) -> Unit,
    private val onDeleteClick: (WatchList) -> Unit
) : RecyclerView.Adapter<WatchListAdapter.WatchListViewHolder>() {

    inner class WatchListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textTitle: TextView = itemView.findViewById(R.id.watchlistTitle)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(watchlist: WatchList) {
            textTitle.text = watchlist.title

            // 🔁 Delete button works the same
            buttonDelete.setOnClickListener {
                onDeleteClick(watchlist)
            }

            // ✅ Entire card click to view detail
            itemView.setOnClickListener {
                onViewClick(watchlist)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.watchlist_item, parent, false)
        return WatchListViewHolder(view)
    }

    override fun onBindViewHolder(holder: WatchListViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<WatchList>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(post: WatchList) {
        val index = items.indexOf(post)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}

