package com.example.devhub

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.devhub.com.example.devhub.model.Comment
import kotlinx.android.synthetic.main.comment_adapter.view.*

class CommentsAdapter(val context:Context, private var comments: List<Comment>):
RecyclerView.Adapter<CommentsAdapter.ViewHolder>(){
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){

        fun bind(comment: Comment){
            itemView.commentUsernameView.text = "_${comment.user?.username}"
            itemView.commentContext.text = comment.comment_Content
            itemView.commentTime.text = DateUtils.getRelativeTimeSpanString(comment.comment_time_Created)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount()= comments.size
}