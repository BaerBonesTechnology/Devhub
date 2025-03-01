package com.baerhous.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.baerhous.devhub.CommentsAdapter
import com.bumptech.glide.Glide
import com.baerhous.devhub.model.Comment
import com.baerhous.devhub.model.Posts
import com.baerhous.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_clicked__post.*
import kotlinx.android.synthetic.main.item_post.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth
@SuppressLint("StaticFieldLeak")
private lateinit var adapter: CommentsAdapter
private lateinit var comments: MutableList<Comment>
private  var users: Users? = null
private const val TAG = "Post:"
private const val EXTRA_POST_ID = "EXTRA_POST_ID"
private const val EXTRA_USER_ID = "EXTRA_USER_ID"
private var post: Posts? = null
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
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
                        postImageView.isGone = true
                    } else {
                        Glide.with(this).load(post?.image_url).into(postImageView)
                        postImageView.isGone = false
                    }

                    db.collection("Posts").document(postId).addSnapshotListener { snapshot, exception ->

                        if (exception != null || snapshot == null) {
                            Log.e(TAG, "Error grabbing collection")
                            return@addSnapshotListener
                        }
                        postRef.collection("LikeBy").get().addOnSuccessListener { query ->
                            if (query != null && query.size() > 0) {

                                dootsCount.text = "${query.size()} doots"
                                dootsCount.isGone = false




                                postRef.collection("LikedBy").document(signedInUser!!.username).get()
                                    .addOnSuccessListener {
                                        doot_btn.colorFilter =
                                            LightingColorFilter(Color.BLACK, Color.GREEN)
                                    }.addOnFailureListener {
                                        doot_btn.colorFilter =
                                            LightingColorFilter(Color.BLACK, Color.BLACK)
                                    }

                            }else{
                                dootsCount.isGone = true
                                doot_btn.colorFilter = LightingColorFilter(Color.BLACK, Color.BLACK)
                            }

                        }.addOnFailureListener {
                            Log.d(TAG, "ERROR GRABBING POST LIKES", it)
                        }
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



        usernameViewExpanded.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra(EXTRA_USERNAME, post?.user?.username)
            startActivity(intent)
        }

        //comment button logic
        comment_Btn.setOnClickListener {
            comment_Btn.isEnabled = false

            val newComment = Comment(signedInUser, System.currentTimeMillis(), commentSend.editableText.toString(), 0)

            val postID = intent.getStringExtra(EXTRA_POST_ID)

            Log.i(TAG, "post id: $postID")
            if(postID != null){
            db.collection("Posts").document(postID).collection("Comments").add(newComment)
                    .addOnCompleteListener { commentCreationTask ->
                        if (!commentCreationTask.isSuccessful)
                            Log.e("Comment Activity", "error creating post", commentCreationTask.exception)
                        else{
                            Log.d("Comment Activity", "Comment Made written with ID: $signedInUser")
                        }
                    }
            //sends notification
                val title = "Devhub"
                val message = "${signedInUser?.username} commented on your post"

                post?.let { it1 ->
                    UserNotification(
                        message,
                        title,
                        System.currentTimeMillis(),
                        it1.PostId
                    ).also {
                        db.collection("Users").document(post?.user!!.userID).collection("Notifications").add(it)
                    }
                }

                PushNotifications(
                    Notification(title, message
                    ),
                    post?.user!!.FCM
                ).also{
                    sendNotifications(it)
                }

            val intent = Intent(this, ClickedPost::class.java)
            intent.putExtra("EXTRA_POST_ID", postID)
            startActivity(intent)}
            else{
                Log.i(TAG, "Error posting comment")
            }
        }

        doot_btn.setOnClickListener {

            val postUpdate = db.collection("Posts").document(postId).collection("LikedBy")

            db.collection("Posts").document(postId).addSnapshotListener { snapshot, error ->
                if (snapshot == null || error != null) {
                    Log.e(TAG, "Error collecting post", error)
                    return@addSnapshotListener
                }

                postUpdate.get().addOnSuccessListener {
                    if (it.isEmpty) {
                        Log.d("Liked Activity", "Collections are empty")
                        postUpdate.document(signedInUser!!.username).set(signedInUser!!)
                    } else {
                        postUpdate.document(signedInUser!!.username)
                            .get()
                            .addOnSuccessListener {
                                postUpdate.document(signedInUser!!.username).delete()
                            }.addOnFailureListener {
                                postUpdate.document(signedInUser!!.username).set(signedInUser!!)
                            }
                    }
                }.addOnFailureListener {
                    Log.e("Liked Query", "Error getting document", it)
                }
            }
            val intent = Intent(this, ClickedPost::class.java)
            intent.putExtra(EXTRA_POST_ID, postId)
            startActivity(intent)

        }


        LogoutBtn.setOnClickListener {

            Firebase.auth.signOut()

            val intent = Intent(this, SignUp::class.java)

            startActivity(intent)

            Toast.makeText(baseContext, "You have been logged out", Toast.LENGTH_LONG).show()
        }

        CPPost_Btn.setOnClickListener {
            val intent = Intent(this, StatusPost::class.java)
            startActivity(intent)
        }

        homeLogo.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        CPnav_Profile.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra(EXTRA_USERNAME, users?.username)
            intent.putExtra(EXTRA_USER_ID, users?.userID)
            startActivity(intent)

        }
        CPnav_Home.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        CPnav_Notifs.setOnClickListener {
            val intent = Intent(this, NotificationPage::class.java)
            intent.putExtra(EXTRA_USERNAME, users?.userID)
            startActivity(intent)
        }


    }

    private fun sendNotifications(notification: PushNotifications) = CoroutineScope(Dispatchers.IO).launch {

        try{
            val response = RetrofitInstance.api.postNotification(notification)

            if(response.isSuccessful){
                Log.d(TAG, "Response: $response")
            }else{
                Log.e(TAG, "No response ${response.errorBody().toString()}")
            }

        }catch (e: Exception){
            Log.e(TAG, "cant send message", e)
        }
    }

}