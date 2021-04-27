package com.example.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.devhub.com.example.devhub.data.Library.ActionLibrary
import com.example.devhub.com.example.devhub.model.Comment
import com.example.devhub.com.example.devhub.model.Notification
import com.example.devhub.model.Posts
import com.example.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_clicked__post.*

private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth
private lateinit var adapter: CommentsAdapter
private lateinit var comments: MutableList<Comment>
private  var users: Users? = null
private const val TAG = "Post:"
private const val EXTRA_POST_ID = "EXTRA_POST_ID"
private var post: Posts? = null
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
private var dooted: Boolean = false
private var signedInUser: Users? = null
private var comment: Comment? = null



class ClickedPost : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clicked__post)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        db.collection("Users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(Users::class.java)
                Log.i(TAG, " signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure to fetch signed in user", exception)
            }

        val postId = intent.getStringExtra(EXTRA_POST_ID)

        val postRef = db.collection("Posts").document(postId!!)


        postRef.get()
            .addOnSuccessListener{ postSnapshot->
                post = postSnapshot.toObject(Posts::class.java)
                if(post != null) {
                    usernameViewExpanded.text = "_${post?.user?.username}"
                    postContextView.text = post?.description
                    if (post?.image_url == "") {
                        Glide.with(this)
                            .load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.colorhexa.com%2F2c2c2c&psig=AOvVaw03_3ZfPX-Do1R1zJCUd4Ua&ust=1616165424832000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCKDB4sWLuu8CFQAAAAAdAAAAABAD")
                            .into(postImageView)
                        postImageView.layoutParams.height = 0
                    } else {
                        Glide.with(this).load(post?.image_url).into(postImageView)
                    }
                    if(post!!.doots > 0){
                        dootsCount.text = "${post?.doots} doots"
                    }
                }

                Log.i("Post Activity", "${post!!.PostId} loaded")

        }
            .addOnFailureListener{exception ->
            Log.i(TAG, "Failure attempting to capture post $EXTRA_POST_ID}",exception)
        }


        comments = mutableListOf()
        adapter = CommentsAdapter(this, comments)
        commentView.adapter = adapter
        commentView.layoutManager = LinearLayoutManager(this)



        Log.i(TAG, "$users")
        val commentRef =  postRef.collection("Comments")



        commentRef.addSnapshotListener{
                snapshot, exception ->
            if(exception != null || snapshot == null){
                Log.e( "Comment Screening", "Exception when querying comments", exception)
                return@addSnapshotListener
            }
        }

        commentRef.orderBy("comment_time_created", Query.Direction.ASCENDING)

        commentRef.get().addOnSuccessListener { snapshot ->
            Log.d("Comment Screening", "Comments Loading")

            if(snapshot.isEmpty){
                Log.d("Comment Screening", "Empty Collection")
            }else{
                val commentList = snapshot.documents

                comments.clear()
                commentList.forEach{
                    comment = it.toObject(Comment::class.java)
                    if(comment != null){
                        comments.add(comment!!)
                        adapter.notifyDataSetChanged()
                    }

                }
            }

        }.addOnFailureListener{
            Log.e("Comment Screening", "Post has no comments", it)
        }




        //comment button logic
        comment_Btn.setOnClickListener {
            comment_Btn.isEnabled = false
            db.collection("Users")
                .document(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    signedInUser = userSnapshot.toObject(Users::class.java)
                    Log.i(TAG, " signed in user: $signedInUser")
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, "Failure to fetch signed in user", exception)
                }

            ActionLibrary().sendComment(commentSend.editableText.toString(), post, signedInUser)
            //sends notification
            ActionLibrary().sendNotification(signedInUser, post, "commented on your post", post?.user)


            val intent = Intent(this, ClickedPost::class.java)
            intent.putExtra("EXTRA_POST_ID", post?.PostId)
            startActivity(intent)
        }

        doot_btn.setOnClickListener {
            //sets a variable named postUpdate to gather document from Firestore
            Log.i("Doot Button", "For post ${post?.PostId} is pressed and textView is equal to ${dootsCount.text}")


            val postUpdate = db.collection("Posts").document(post!!.PostId)



            if (dooted && post!!.doots > 0) {
                postUpdate.collection("LikedBy").document(signedInUser!!.username).delete()
                post!!.doots = post!!.doots - 1


            } else {
                postUpdate.collection("LikedBy").document(signedInUser!!.username)
                    .set(signedInUser!!)
                post!!.doots = post!!.doots + 1
            }

            dooted = !dooted
            //sets post profile modification and adds success and failure listeners to post to log
            val set = postUpdate.set(post!!)


            set.addOnSuccessListener {
                Log.d("Firebase", "Liked by _${signedInUser?.username}")
            }
            set.addOnFailureListener {
                Log.d("Firebase", "Like Error")
            }

            //sends notification to user
            if (signedInUser != post?.user) {
                val notification = Notification(
                    signedInUser,
                    post,
                    "dooted your post",
                    System.currentTimeMillis()
                )
                db.collection("Users").document(post!!.user!!.userID)
                    .collection("Notifications")
                    .add(notification).addOnSuccessListener {
                        Log.d(TAG, "Notification Sent")
                    }.addOnFailureListener {
                        Log.e(TAG, "Notification Failure", it)
                    }
            }

            //logs both user activity and the number of doots in a single post
            Log.i("User Activity", "Post liked ${post!!.PostId} by ${signedInUser?.username}")
            Log.d("PostActivity", "Post $post now has ${post!!.doots} doots")

            //shows and hides doots counter on post
            if (post!!.doots >= 1) {
                dootsCount.isGone = false
                dootsCount.text = "${post?.doots} doots"
            } else {
                dootsCount.isGone = true
            }


        }

        LogoutBtn.setOnClickListener {

            Firebase.auth.signOut()

            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)

            Toast.makeText(baseContext, "You have been logged out", Toast.LENGTH_LONG).show()
        }

        Post_Btn.setOnClickListener {
            //TODO: create UI for adding post
            val intent = Intent(this, StatusPost::class.java)
            startActivity(intent)
            //TODO: create adapter to save in firebase
            //TODO: return to home page
        }

        homeLogo.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        nav_Profile.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra(EXTRA_USERNAME, users?.username)
            startActivity(intent)

        }
        nav_Home.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        nav_NotifsFromPost.setOnClickListener {
            val intent = Intent(this, Notifications::class.java)
            startActivity(intent)
        }


    }
}