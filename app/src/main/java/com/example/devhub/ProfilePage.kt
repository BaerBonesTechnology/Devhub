package com.example.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.devhub.com.example.devhub.data.Library.ActionLibrary
import com.example.devhub.com.example.devhub.model.Notification
import com.example.devhub.model.Posts
import com.example.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_home_page.LogoutBtn
import kotlinx.android.synthetic.main.activity_home_page.Post_Btn
import kotlinx.android.synthetic.main.activity_home_page.homeLogo
import kotlinx.android.synthetic.main.activity_home_page.postFeed
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.item_post.*
import kotlinx.android.synthetic.main.item_post.view.*

private var signedInUser: Users? = null
private const val TAG = "Post:"
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
private lateinit var auth: FirebaseAuth
@SuppressLint("StaticFieldLeak")
private lateinit var firestoreDB: FirebaseFirestore
private lateinit var posts:MutableList<Posts>
@SuppressLint("StaticFieldLeak")
private lateinit var adapter: PostAdapter
private var dooted:Boolean = false
private var post: Posts? = null
private const val EXTRA_POST_ID = "EXTRA_POST_ID"


class ProfilePage : AppCompatActivity(), PostAdapter.DootsClickListener
{
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        firestoreDB = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        posts = mutableListOf()
        adapter = PostAdapter(this, posts, this)
        postFeed.adapter = adapter
        postFeed.layoutManager = LinearLayoutManager(this)


        // make a query to firestore to gather posts

        firestoreDB.collection("Users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(Users::class.java)
                usernameView.text = "_"+ signedInUser?.username +"\n User Posts:"+ signedInUser?.posts
                bioTxtView.text = signedInUser?.bio
                Log.i(TAG, " signed in user: $signedInUser")
                val username = intent.getStringExtra(EXTRA_USERNAME)

                val postsRef = ActionLibrary().getPosts().whereEqualTo("user.username", username)

                postsRef.addSnapshotListener { snapshot, exception ->
                    if (exception != null || snapshot == null) {
                        Log.e(TAG, "Exception when querying posts", exception)
                        return@addSnapshotListener
                    }else {

                        val postList = snapshot.documents
                        posts.clear()
                        postList.forEach {
                            post = it.toObject(Posts::class.java)
                            if (post != null) {
                                post!!.PostId = it.id
                                posts.add(post!!)

                                adapter.notifyDataSetChanged()
                            }
                            val dootsRef = firestoreDB
                                    .collection("Posts").document(post!!.PostId).collection("LikedBy")

                            dootsRef.get().addOnSuccessListener {
                                Log.d("LIKED_QUERY", "Getting document")

                                if (it.isEmpty) {
                                    Log.d("LIKED_QUERY", "Collections are empty")
                                } else {
                                    val list = it.documents
                                    for (documents in list) {
                                        if (documents.id == signedInUser?.username) {
                                            dooted = true
                                        }
                                    }

                                }
                            }.addOnFailureListener {
                                Log.d("LIKED_QUERY", "Error getting document", exception)

                            }

                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure to fetch signed in user", exception)
            }




//Adds button to log out (Temporary while building)
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

        nav_ProfileHome.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)

        }
        nav_HomeHome.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        editProfileBtn.setOnClickListener{
            val intent = Intent(this, ProfileSettings::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun dootButton(button: ImageButton, post: Posts, textView: TextView) {
        //sets a variable named postUpdate to gather document from Firestore
        val postUpdate = firestoreDB.collection("Posts").document(post.PostId)



        if (dooted && post.doots > 0) {
            postUpdate.collection("LikedBy").document(signedInUser!!.username).delete()
            post.doots = post.doots - 1


        } else {
            postUpdate.collection("LikedBy").document(signedInUser!!.username).set(signedInUser!!)
                .addOnSuccessListener {
                Log.d("Firebase", "Liked by _${signedInUser?.username}")
            }
                .addOnFailureListener {
                    Log.d("Firebase", "Like Error")
                }
            post.doots = post.doots + 1
        }

        dooted = !dooted
        //sets post profile modification and adds success and failure listeners to post to log

        //sends notification to user
        if(signedInUser != post.user) {
            val notification = Notification(
                    signedInUser,
                    post,
                    "dooted",
                    System.currentTimeMillis()
            )
            firestoreDB.collection("Users").document(post.user!!.userID)
                .collection("Notifications").add(notification).addOnSuccessListener {
                        Log.d(TAG, "Notification Sent")
                    }.addOnFailureListener {
                        Log.e(TAG, "Notification Failure", it)
                    }
        }

        //logs both user activity and the number of doots in a single post
        Log.i("User Activity", "Post liked ${post.PostId} by ${signedInUser?.username}")
        Log.d("PostActivity", "Post $post now has ${post.doots} doots")

        //shows and hides doots counter on post
        if (post.doots >= 1) {
            textView.isGone = false
            textView.text = "${post.doots} doots"
        } else {
            textView.isGone = true
        }
    }

    override fun commentButton( post:Posts){
        val intent = Intent(this, ClickedPost::class.java)
        intent.putExtra(EXTRA_POST_ID, post.PostId)
        startActivity(intent)
    }
}
