package com.example.devhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.devhub.Model.Posts
import com.example.devhub.Model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_home_page.LogoutBtn
import kotlinx.android.synthetic.main.activity_home_page.Post_Btn
import kotlinx.android.synthetic.main.activity_home_page.homeLogo
import kotlinx.android.synthetic.main.activity_home_page.postFeed
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.item_post.*
import kotlinx.android.synthetic.main.activity_home_page.nav_Home as nav_Home
import kotlinx.android.synthetic.main.activity_profile.nav_Profile as nav_Profile

private var signedInUser: Users? = null
private const val TAG = "Post:"
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
private lateinit var auth: FirebaseAuth
private lateinit var firestoreDB: FirebaseFirestore
private lateinit var posts:MutableList<Posts>
private lateinit var adapter: PostAdapter

class profile_page : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        auth = FirebaseAuth.getInstance()


        //TODO: Create Layout File
        //TODO: Create Data Source
        posts = mutableListOf()
        //TODO: Create Adapter
        adapter = PostAdapter(this, posts)
        //TODO: Bind Adapter and layout manager to the RV
        postFeed.adapter = adapter
        postFeed.layoutManager = LinearLayoutManager(this)


        // make a query to firestore to gather posts
        firestoreDB = FirebaseFirestore.getInstance()

        firestoreDB.collection("Users")
            .document(auth.currentUser.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(Users::class.java)
                usernameView.text = "_${signedInUser?.username} \n User Posts:${signedInUser?.posts}"
                Log.i(TAG, " signed in user: $signedInUser")

            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure to fetch signed in user", exception)
            }

        val username = intent.getStringExtra(EXTRA_USERNAME)

        var postsRef = firestoreDB
            .collection("Posts")
            .limit(20)
            .orderBy("creation_time", Query.Direction.DESCENDING)
            .whereEqualTo("user.username", username)




        postsRef.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }

            val postList = snapshot.toObjects(Posts::class.java)
            posts.clear()
            posts.addAll(postList)
            adapter.notifyDataSetChanged()
            for (post in postList) {
                Log.i(
                    TAG,
                    "Post $post"
                )
            }
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
            val intent = Intent(this, Status_post::class.java)
            startActivity(intent)
            //TODO: create adapter to save in firebase
            //TODO: return to home page
        }

        homeLogo.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        nav_Profile.setOnClickListener {
            val intent = Intent(this, profile_page::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)

        }
        nav_Home.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }


    }
}