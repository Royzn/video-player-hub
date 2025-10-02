package com.example.video_player_hub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.video_player_hub.R
import com.example.video_player_hub.data.Post

class ContentAdapter(
    private var fullList: MutableList<Post> = mutableListOf(),
    private val onViewDetailClick: (Post) -> Unit
) : RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    private var filteredList: MutableList<Post> = mutableListOf()

    init {
        filteredList.addAll(fullList)
    }

    inner class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val bodyTextView: TextView = itemView.findViewById(R.id.bodyTextView)
        val viewDetailButton: Button = itemView.findViewById(R.id.viewDetailButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.content_card, parent, false)
        return ContentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val post = filteredList[position]
        holder.titleTextView.text = post.title
        holder.bodyTextView.text = post.body
        holder.viewDetailButton.setOnClickListener {
            onViewDetailClick(post)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun setData(newPosts: List<Post>) {
        fullList.clear()
        fullList.addAll(newPosts)
        filteredList.clear()
        filteredList.addAll(newPosts)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val lowerCaseQuery = query.lowercase()
        filteredList.clear()
        if (lowerCaseQuery.isEmpty()) {
            filteredList.addAll(fullList)
        } else {
            val filtered = fullList.filter {
                it.title.lowercase().contains(lowerCaseQuery) || it.body.lowercase().contains(lowerCaseQuery)
            }
            filteredList.addAll(filtered)
        }
        notifyDataSetChanged()
    }
}
