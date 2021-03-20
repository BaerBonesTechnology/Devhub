package com.example.devhub

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.devhub.Model.Posts
import kotlinx.android.synthetic.main.item_post.view.*

class PostAdapter(val context: Context, val posts: List<Posts>):
    RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(post: Posts){
            itemView.username.text = "_${post.user?.username}"
            itemView.PostDesc.text = post.description
            if(post.image_url == "")
            {
                Glide.with(context).load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.colorhexa.com%2F2c2c2c&psig=AOvVaw03_3ZfPX-Do1R1zJCUd4Ua&ust=1616165424832000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCKDB4sWLuu8CFQAAAAAdAAAAABAD").into(itemView.PostImage)
                itemView.PostImage.layoutParams.height = 0
            }else
            {
                Glide.with(context).load(post.image_url).into(itemView.PostImage)
            }
            itemView.datePosted.text = DateUtils.getRelativeTimeSpanString(post.creation_time)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

}