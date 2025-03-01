package com.baerhous.devhub

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baerhous.devhub.databinding.CommentAdapterBinding
import com.baerhous.devhub.model.Comment

class CommentsAdapter(val context:Context, private var comments: List<Comment>):
RecyclerView.Adapter<CommentsAdapter.ViewHolder>(){

    private lateinit var binding: CommentAdapterBinding

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){


        fun bind(comment: Comment){
            binding.commentUsernameView.text = "_" + comment.user?.username
            binding.commentContext.text = comment.comment_Content
            binding.commentTime.text = DateUtils.getRelativeTimeSpanString(comment.comment_time_Created)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = binding.root

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount()= comments.size
}