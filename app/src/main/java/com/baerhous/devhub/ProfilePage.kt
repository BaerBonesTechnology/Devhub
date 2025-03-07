package com.baerhous.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.baerhous.devhub.data.Library.ActionLibrary
import com.baerhous.devhub.databinding.ActivityProfileBinding
import com.baerhous.devhub.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private var signedInUser: Users? = null
private var profileUser: Users? = null
private const val TAG = "Post:"
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
private const val EXTRA_USER_ID = "EXTRA_USERID"
private lateinit var auth: FirebaseAuth
private lateinit var firestoreDB: FirebaseFirestore
private lateinit var posts:MutableList<Posts>
private lateinit var adapter: PostAdapter
private var post: Posts? = null
private const val EXTRA_POST_ID = "EXTRA_POST_ID"
private var userID: String? = ""
private var username:String? = ""


class ProfilePage : AppCompatActivity(), PostAdapter.DootsClickListener
{
    private lateinit var binding: ActivityProfileBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)

        userID = intent.getStringExtra(EXTRA_USER_ID)
        username = intent.getStringExtra(EXTRA_USERNAME)

        firestoreDB = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        binding.usernameView.text = username

        firestoreDB.collection("Users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(Users::class.java)

                posts = mutableListOf()
                adapter = PostAdapter(this, posts, this, signedInUser!!.username)
                binding.postFeed.adapter = adapter
                binding.postFeed.layoutManager = LinearLayoutManager(this)

                Log.i(TAG, " signed in user: $signedInUser")

                if(signedInUser?.userID != userID) {
                    firestoreDB.collection("Users")
                        .document(userID!!)
                        .get()
                        .addOnSuccessListener { userSnapshot ->
                            profileUser = userSnapshot.toObject(Users::class.java)
                            Log.i(TAG, " signed in user: $signedInUser")

                            val postsRef =
                                ActionLibrary().getPosts().whereEqualTo("user.userID", profileUser?.userID)



                            postsRef.addSnapshotListener { snapshot, exception ->
                                if (exception != null || snapshot == null) {
                                    Log.e(TAG, "Exception when querying posts", exception)
                                    return@addSnapshotListener
                                } else {

                                    val postList = snapshot.documents
                                    posts.clear()
                                    postList.forEach { it ->
                                        post = it.toObject(Posts::class.java)
                                        if (post != null) {
                                            post!!.PostId = it.id
                                            posts.add(post!!)
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                }
                            }

                            binding.usernameView.text = "_" + username
                            binding.bioTxtView.text = profileUser?.bio
                            binding.editProfileBtn.isGone = true

                        }
                        .addOnFailureListener { exception ->
                            Log.i(TAG, "Failure to fetch signed in user", exception)
                        }
                }
                else {



                    val postsRef =
                        ActionLibrary().getPosts().whereEqualTo("user.userID", signedInUser?.userID)



                    postsRef.addSnapshotListener { snapshot, exception ->
                        if (exception != null || snapshot == null) {
                            Log.e(TAG, "Exception when querying posts", exception)
                            return@addSnapshotListener
                        } else {

                            val postList = snapshot.documents
                            posts.clear()
                            postList.forEach { it ->
                                post = it.toObject(Posts::class.java)
                                if (post != null) {
                                    post!!.PostId = it.id
                                    posts.add(post!!)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }

                    binding.usernameView.text = "_" + username
                    binding.bioTxtView.text = signedInUser?.bio

                }

            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure to fetch signed in user", exception)
            }







//Adds button to log out (Temporary while building)
        binding.LogoutBtn.setOnClickListener {

            Firebase.auth.signOut()

            val intent = Intent(this, SignUp::class.java)

            startActivity(intent)

            Toast.makeText(baseContext, "You have been logged out", Toast.LENGTH_LONG).show()
        }

        binding.PPPostBtn.setOnClickListener {
            //TODO: create UI for adding post
            val intent = Intent(this, StatusPost::class.java)
            startActivity(intent)
            //TODO: create adapter to save in firebase
            //TODO: return to home page
        }

        binding.homeLogo.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        binding.PPnavProfileHome.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra(EXTRA_USER_ID, signedInUser?.userID)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)

        }
        binding.PPnavHomeHome.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }
        binding.PPnavCoding.setOnClickListener {
            val intent = Intent(this, CodingNotes::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }
        binding.PPnavNotifs.setOnClickListener {
            val intent = Intent(this, NotificationPage::class.java)
            intent.putExtra(EXTRA_USER_ID, signedInUser?.userID)
            startActivity(intent)
        }

        binding.editProfileBtn.setOnClickListener{
            val intent = Intent(this, ProfileSettings::class.java)
            startActivity(intent)

        }

        setContentView(binding.root)
    }

    @SuppressLint("SetTextI18n")
    override fun dootButton(button: ImageButton, post: Posts, textView: TextView) {
        //sets a variable named postUpdate to gather document from Firestore
        Log.i("Doot Button", "For post ${post.PostId} is pressed and textView is equal to ${textView.text}")


        val postUpdate = firestoreDB.collection("Posts").document(post.PostId).collection("LikedBy")

        val userSignedIn = signedInUser!!

        postUpdate.get().addOnSuccessListener { it ->
            if (it.isEmpty) {
                Log.d("Liked Activity", "Collections are empty")
                postUpdate.document(userSignedIn.username).set(userSignedIn)
                if(post.user?.userID != userSignedIn.userID){

                    val title = "Devhub"
                    val message = "${signedInUser?.username} dooted your post"
                    button.colorFilter = LightingColorFilter(Color.BLACK, Color.GREEN)

                    UserNotification(message, title, System.currentTimeMillis(), post.PostId).also {
                        firestoreDB.collection("Users").document(post.user!!.userID).collection("Notifications").add(it)
                    }

                    PushNotifications(
                        Notification(title, message
                        ),
                        post.user!!.FCM
                    ).also{
                        sendNotifications(it)
                    }

                }
            } else {
                postUpdate.document(userSignedIn.username)
                    .get()
                    .addOnSuccessListener {
                        postUpdate.document(userSignedIn.username).delete()
                        button.colorFilter = LightingColorFilter(Color.BLACK, Color.BLACK)
                    }.addOnFailureListener {
                        postUpdate.document(userSignedIn.username).set(userSignedIn)
                        button.colorFilter = LightingColorFilter(Color.BLACK, Color.GREEN)
                        if(post.user?.userID != userSignedIn.userID){

                            val title = "Devhub"
                            val message = "${signedInUser?.username} dooted your post"
                            PushNotifications(
                                Notification(title, message),
                                post.user!!.FCM
                            ).also{
                                sendNotifications(it)
                            }
                        }
                    }
            }
        }.addOnFailureListener{
            Log.e("Liked Query", "Error getting document", it)
        }


    }

    override fun commentButton( post:Posts){
        val intent = Intent(this, ClickedPost::class.java)
        intent.putExtra(EXTRA_POST_ID, post.PostId)
        startActivity(intent)
    }
    override fun goToProfile(user: Users?) {
        val intent = Intent(this, ProfilePage::class.java)
        intent.putExtra(EXTRA_USER_ID, user?.userID)
        intent.putExtra(EXTRA_USERNAME, user?.username)
        startActivity(intent)
    }

    override fun delete(post: Posts) {
        firestoreDB.collection("Posts").document(post.PostId).delete()

        Log.i(TAG, "User deleted post")

        val intent = Intent(this, ProfilePage::class.java)
        intent.putExtra(EXTRA_USER_ID, post.user?.userID)
        intent.putExtra(EXTRA_USERNAME, post.user?.username)
        startActivity(intent)
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
