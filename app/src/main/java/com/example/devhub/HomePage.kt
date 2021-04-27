package com.example.devhub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_clicked__post.*
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_home_page.LogoutBtn
import kotlinx.android.synthetic.main.activity_home_page.Post_Btn
import kotlinx.android.synthetic.main.activity_home_page.homeLogo
import kotlinx.android.synthetic.main.activity_home_page.nav_Coding1
import kotlinx.android.synthetic.main.activity_home_page.nav_Home
import kotlinx.android.synthetic.main.activity_home_page.nav_Profile
import kotlinx.android.synthetic.main.activity_home_page.view.*
import kotlinx.android.synthetic.main.activity_status_post.*
import kotlinx.android.synthetic.main.item_post.*
import kotlinx.android.synthetic.main.item_post.view.*


private var signedInUser: Users? = null
private const val TAG = "Post:"
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
private lateinit var posts:MutableList<Posts>
private lateinit var adapter: PostAdapter
private lateinit var storage: StorageReference
private var photo_uri: Uri? = null
private var dooted: Boolean = false
private var post: Posts? = null
private const val EXTRA_POST_ID = "EXTRA_POST_ID"
private lateinit var firestoreDB: FirebaseFirestore
private lateinit var auth: FirebaseAuth
private const val PICK_PHOTO_CODE = 1234
private var url = ""




open class HomePage : AppCompatActivity(), PostAdapter.DootsClickListener {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        auth = FirebaseAuth.getInstance()
        firestoreDB = FirebaseFirestore.getInstance()
        //TODO: Create Layout File
        //TODO: Create Data Source
        posts = mutableListOf()
        //TODO: Create Adapter
        adapter = PostAdapter(this, posts, this)
        //TODO: Bind Adapter and layout manager to the RV
        postFeed.adapter = adapter
        postFeed.layoutManager = LinearLayoutManager(this)

        // make a query to firestore to gather posts
        firestoreDB.collection("Users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(Users::class.java)
                Log.i(TAG, " signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure to fetch signed in user", exception)
            }

        val postsRef = ActionLibrary().getPosts()

        storage = FirebaseStorage.getInstance().reference



        postsRef.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }

            val postList = snapshot.documents
            posts.clear()
            postList.forEach {
                post = it.toObject(Posts::class.java)
                if (post != null) {
                    post!!.PostId = it.id
                    posts.add(post!!)
                    Log.e(TAG, post?.PostId!!)
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

        newPostString.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                photoButton.isGone = false;
                Log.i("NEW POST ACTIVITY", "Text Focused")
            }else{
                photoButton.isGone = true;
                Log.i("NEW POST ACTIVITY", "Text unfocused")

            }
        }

        Post_Button.setOnClickListener {
            Post_Button.isEnabled = false
            val photoRef = storage.child("images/${System.currentTimeMillis()}-photo.jpg")

            if (photo_uri == null && newPostString.text.isBlank()) {
                Toast.makeText(this, "Please post content", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (signedInUser == null) {
                Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }


            if (photo_uri != null) {
                photoRef.putFile(photo_uri!!)
                        .continueWithTask { photoUploadTask ->

                            Log.i(TAG, "upload bytes: ${photoUploadTask.result?.bytesTransferred}")
                            photoRef.downloadUrl
                        }
                        .continueWithTask { photoDownloadTask ->


                            val posts = Posts(
                                    System.currentTimeMillis(),
                                    newPostString.editableText.toString(),
                                    photoDownloadTask.result.toString(),
                                    signedInUser
                            )
                            photo_uri = null


                            firestoreDB.collection("Posts").add(posts)
                                    .addOnCompleteListener { postCreationTask ->
                                        if (!postCreationTask.isSuccessful) {
                                            Log.e(TAG, "Error creating post", postCreationTask.exception)
                                            Toast.makeText(
                                                    baseContext,
                                                    "Posting failed",
                                                    Toast.LENGTH_SHORT
                                            )
                                                    .show()

                                            val intent = Intent(this, HomePage::class.java)
                                            startActivity(intent)

                                        } else {
                                            Log.d(TAG, "Post Made written with ID: $signedInUser")

                                            Toast.makeText(
                                                    baseContext,
                                                    "Posting succeeded",
                                                    Toast.LENGTH_SHORT
                                            )
                                                    .show()


                                            val userNewInfo = Users(
                                                    signedInUser!!.username,
                                                    signedInUser!!.age,
                                                    signedInUser!!.doots,
                                                    signedInUser!!.posts.plus(1),
                                                    bio = signedInUser!!.bio
                                            )



                                            firestoreDB.collection("Users")
                                                    .document(auth.currentUser.uid)
                                                    .set(userNewInfo)
                                                    .addOnSuccessListener {
                                                        Log.d(
                                                                TAG,
                                                                "DocumentSnapshot successfully written!"
                                                        )
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.w(
                                                                TAG,
                                                                "Error writing document",
                                                                e
                                                        )
                                                    }

                                            val intent = Intent(this, HomePage::class.java)
                                            startActivity(intent)
                                        }
                                    }
                        }
            } else {

                val posts = Posts(
                        System.currentTimeMillis(),
                        newPostString.editableText.toString(),
                        url,
                        signedInUser
                )

                firestoreDB.collection("Posts").add(posts)
                        .addOnCompleteListener { postCreationTask ->

                            if (!postCreationTask.isSuccessful) {
                                Log.e(TAG, "Error creating post", postCreationTask.exception)
                                Toast.makeText(baseContext, "Posting failed", Toast.LENGTH_SHORT)
                                        .show()

                                val intent = Intent(this, HomePage::class.java)
                                startActivity(intent)

                            } else {
                                Log.d(TAG, "Post Made written with ID: $signedInUser")

                                Toast.makeText(baseContext, "Posting succeeded", Toast.LENGTH_SHORT)
                                        .show()


                                val userNewInfo = Users(
                                        signedInUser!!.username,
                                        signedInUser!!.age,
                                        signedInUser!!.doots,
                                        signedInUser!!.posts.plus(1),
                                        bio = signedInUser!!.bio
                                )



                                firestoreDB.collection("Users")
                                        .document(auth.currentUser.uid)
                                        .set(userNewInfo)
                                        .addOnSuccessListener {
                                            Log.d(
                                                    TAG,
                                                    "DocumentSnapshot successfully written!"
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w(
                                                    TAG,
                                                    "Error writing document",
                                                    e
                                            )
                                        }

                                val intent = Intent(this, HomePage::class.java)
                                startActivity(intent)

                            }

                        }
            }

        }

        photoButton.setOnClickListener {
            Log.i(TAG, "Open up gallery on device")

            val galleryImageIntent = Intent(Intent.ACTION_GET_CONTENT)

            galleryImageIntent.type = "image/*"
            if (galleryImageIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(galleryImageIntent, PICK_PHOTO_CODE)
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
            val intent = Intent(this, StatusPost::class.java)
            startActivity(intent)
        }

        homeLogo.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        nav_Profile.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)

        }
        nav_Home.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        nav_Coding1.setOnClickListener {
            val intent = Intent(this, CodingNotes::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }
        nav_Notifs.setOnClickListener {
            val intent = Intent(this, Notifications::class.java)
            startActivity(intent)
        }

    }

    @SuppressLint("SetTextI18n")
    override fun dootButton(button: ImageButton, post: Posts, textView: TextView) {
        //sets a variable named postUpdate to gather document from Firestore
        Log.i("Doot Button", "For post ${post.PostId} is pressed and textView is equal to ${textView.text}")


        val postUpdate = firestoreDB.collection("Posts").document(post.PostId)



        if (dooted && post.doots > 0) {
            postUpdate.collection("LikedBy").document(signedInUser!!.username).delete()
            post.doots = post.doots - 1


        } else {
            postUpdate.collection("LikedBy").document(signedInUser!!.username)
                .set(signedInUser!!)
            post.doots = post.doots + 1
        }

        dooted = !dooted
        //sets post profile modification and adds success and failure listeners to post to log
        val set = postUpdate.set(post)


        set.addOnSuccessListener {
            Log.d("Firebase", "Liked by _${signedInUser?.username}")
        }
        set.addOnFailureListener {
            Log.d("Firebase", "Like Error")
        }

        //sends notification to user
        if (signedInUser!!.userID != post.user!!.userID) {
            val notification = Notification(
                signedInUser,
                post,
                "dooted your post",
                System.currentTimeMillis()
            )
            firestoreDB.collection("Users").document(post.user!!.userID)
                .collection("Notifications")
                .add(notification).addOnSuccessListener {
                    Log.d(TAG, "Notification Sent")
                }.addOnFailureListener {
                    Log.e(TAG, "Notification Failure", it)
                }
        }else{
            Log.i(TAG, "user affected own post")

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
    override fun commentButton(post: Posts) {
            val intent = Intent(this, ClickedPost::class.java)
            intent.putExtra(EXTRA_POST_ID, post.PostId)
            startActivity(intent)

        }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_CODE)
            if (resultCode == Activity.RESULT_OK) {
                photo_uri = data?.data
                postImage.setImageURI(photo_uri)
                postImage.isGone = false;

            } else {
                Log.i(TAG, "Gallery Closed user cancelled")
            }
    }

}

