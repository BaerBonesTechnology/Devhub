package com.example.devhub

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.devhub.model.Posts
import com.example.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.item_post.view.*

class PostAdapter(val context: Context, private var posts: List<Posts>, private var dootsClickListener: DootsClickListener, private var user: String):
        RecyclerView.Adapter<PostAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        @SuppressLint("SetTextI18n")
        fun bind(post: Posts) {



            val userDb = FirebaseAuth.getInstance()

            dootsClickListener.colorChange(itemView.doots2, post.PostId, itemView.DootsView, user)
            itemView.username.text = "_${post.user?.username}"
            itemView.PostDesc.text = post.description
            if (post.image_url == "") {
                Glide.with(context)
                    .load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.colorhexa.com%2F2c2c2c&psig=AOvVaw03_3ZfPX-Do1R1zJCUd4Ua&ust=1616165424832000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCKDB4sWLuu8CFQAAAAAdAAAAABAD")
                    .into(itemView.PostImage)
                itemView.PostImage.layoutParams.height = 0
            } else {
                Glide.with(context).load(post.image_url).into(itemView.PostImage)
            }
            itemView.datePosted.text = DateUtils.getRelativeTimeSpanString(post.creation_time)

            itemView.deleteButton.isGone = userDb.currentUser?.uid != post.user?.userID

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false)



        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val post = posts[position]


        holder.itemView.doots2.setOnClickListener {
            dootsClickListener
                .dootButton(holder.itemView.doots2, post, holder.itemView.DootsView)
            notifyItemChanged(position)
        }

        holder.itemView.commentBtn.setOnClickListener {
            dootsClickListener
                .commentButton(post)
        }
        holder.itemView.username.setOnClickListener { dootsClickListener.goToProfile(post.user) }

        holder.itemView.deleteButton.setOnClickListener { dootsClickListener.delete(post) }


        holder.bind(post)

    }

    override fun getItemCount() = posts.size

    interface DootsClickListener {
        @SuppressLint("SetTextI18n")
        fun dootButton(button: ImageButton, post: Posts, textView: TextView)
        fun commentButton(post: Posts)
        fun goToProfile(user: Users?)
        fun delete(post: Posts)

        @SuppressLint("SetTextI18n")
        fun colorChange(button: ImageButton, postId: String, textView: TextView, user: String) {
            val db = FirebaseFirestore.getInstance()


                val likeCollection = db.collection("Posts").document(postId)
                likeCollection.collection("LikedBy").addSnapshotListener { value, error ->
                    if (value == null || error != null) {
                        Log.d("POST ERROR", "Error getting likes")
                        return@addSnapshotListener
                    }

                    val likedList = value.documents

                    if(value.documents.isNotEmpty()) {
                        textView.isGone = false
                        textView.text = value.documents.size.toString() + " doots"
                        likedList.forEach {
                            if (it.id == user) {
                                button.colorFilter = LightingColorFilter(Color.BLACK, Color.GREEN)
                            } else {
                                button.colorFilter = LightingColorFilter(Color.BLACK, Color.BLACK)
                            }
                        }
                    }else{
                        textView.isGone = true
                    }
                }

            }
        }
    }

