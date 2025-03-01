
package com.baerhous.devhub

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.baerhous.devhub.data.Library.ActionLibrary
import com.baerhous.devhub.databinding.ActivityHomePageBinding
import com.baerhous.devhub.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


private var photo_uri: Uri? = null
private var post: Posts? = null
private var url = ""

private val postsRef = ActionLibrary().getPosts()

private const val TAG = "Post:"
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
private const val EXTRA_USER_ID = "EXTRA_USERID"
private const val EXTRA_POST_ID = "EXTRA_POST_ID"
private const val PICK_PHOTO_CODE = 1234

private var signedInUser: Users? = null
private lateinit var posts:MutableList<Posts>
private lateinit var storage: StorageReference
private lateinit var firestoreDB: FirebaseFirestore
private lateinit var adapter: PostAdapter
private lateinit var auth: FirebaseAuth
private lateinit var fcm: FirebaseMessaging
private lateinit var fiam: FirebaseInstallations




open class HomePage : AppCompatActivity(), PostAdapter.DootsClickListener {

    private lateinit var binding: ActivityHomePageBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)

        // make a query to firestore to gather posts
        fcm = FirebaseMessaging.getInstance()
        fiam = FirebaseInstallations.getInstance()
        firestoreDB = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        firestoreDB.collection("Users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(Users::class.java)
                Log.i(TAG, " signed in user: $signedInUser")
                posts = mutableListOf()
                adapter = PostAdapter(this, posts, this, signedInUser!!.username)
                binding.postFeed.adapter = adapter
                binding.postFeed.layoutManager = LinearLayoutManager(this)

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
                            firestoreDB.collection("Posts").document(it.id).set(post!!)
                            posts.add(post!!)
                            Log.e(TAG, post?.PostId!!)
                            adapter.notifyDataSetChanged()
                        }

                    }

                }





        storage = FirebaseStorage.getInstance().reference

                binding.newPostString.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                binding.photoButton.isGone = false
                Log.i("NEW POST ACTIVITY", "Text Focused")
            }else{
                binding.photoButton.isGone = true
                Log.i("NEW POST ACTIVITY", "Text unfocused")

            }
        }

                binding.PostButton.setOnClickListener {
            binding.PostButton.isEnabled = false
            val photoRef = storage.child("images/${System.currentTimeMillis()}-photo.jpg")

            if (photo_uri == null && binding.newPostString.text.isBlank()) {
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
                                binding.newPostString.editableText.toString(),
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
                                                    .document(signedInUser!!.userID)
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
                                        }

                                    }
                        }
            } else {

                val posts = Posts(
                        System.currentTimeMillis(),
                    binding.newPostString.editableText.toString(),
                        url,
                        signedInUser
                )

                firestoreDB.collection("Posts").add(posts)
                        .addOnCompleteListener { postCreationTask ->

                            if (!postCreationTask.isSuccessful) {
                                Log.e(TAG, "Error creating post", postCreationTask.exception)
                                Toast.makeText(baseContext, "Posting failed", Toast.LENGTH_SHORT)
                                        .show()

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
                                        .document(signedInUser!!.userID)
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
                            }

                        }
            }
            binding.newPostString.editableText.clear()
            binding.PostButton.isEnabled = true
            binding.postImage.setImageURI(null)
            binding.postImage.isVisible = false
        }

                binding.photoButton.setOnClickListener {
            Log.i(TAG, "Open up gallery on device")

            val galleryImageIntent = Intent(Intent.ACTION_GET_CONTENT)

            galleryImageIntent.type = "image/*"
            if (galleryImageIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(galleryImageIntent, PICK_PHOTO_CODE)
            }
        }


//Adds button to log out (Temporary while building)
                binding.LogoutBtn.setOnClickListener {

            Firebase.auth.signOut()

            val intent = Intent(this, SignUp::class.java)

            startActivity(intent)

            Toast.makeText(baseContext, "You have been logged out", Toast.LENGTH_LONG).show()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)

        }

                binding.HPPostBtn.setOnClickListener {
            val intent = Intent(this, StatusPost::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)

        }

                binding.homeLogo.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)

        }

                binding.HPnavProfile.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            intent.putExtra(EXTRA_USER_ID, signedInUser?.userID)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)


        }
                binding.HPnavHome.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)

        }

                binding.HPnavCoding.setOnClickListener {
            val intent = Intent(this, CodingNotes::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)

        }
                binding.HPnavNotifs.setOnClickListener {
            val intent = Intent(this, NotificationPage::class.java)
            intent.putExtra(EXTRA_USER_ID, signedInUser?.userID)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)

        }
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure to fetch signed in user", exception)
            }
        setContentView(binding.root)
    }


    @SuppressLint("SetTextI18n")
    override fun dootButton(button: ImageButton, post: Posts, textView: TextView) {
        //sets a variable named postUpdate to gather document from Firestore


        Log.i("Doot Button", "For post ${post.PostId} is pressed and textView is equal to ${textView.text}")

        val postUpdate = firestoreDB.collection("Posts").document(post.PostId).collection("LikedBy")

        firestoreDB.collection("Users").document(auth.currentUser?.uid ?: "").get().addOnCompleteListener{
            if(it.isSuccessful){
                signedInUser = it.result.toObject(Users::class.java)
            }
        }

        postUpdate.get().addOnSuccessListener { it ->
            if (it.isEmpty) {
                Log.d("Liked Activity", "Collections are empty")
                postUpdate.document(signedInUser!!.username).set(signedInUser!!)
                if(post.user?.userID != signedInUser!!.userID){

                    val title = "Devhub"
                    val message = "${signedInUser?.username }dooted your post"


                    UserNotification(message, title, System.currentTimeMillis(), post.PostId).also {
                        firestoreDB.collection("Users").document(post.user!!.userID).collection("Notifications").add(it)
                    }

                    PushNotifications(
                        Notification(title, message//
                                // , System.currentTimeMillis(),post.PostId
                                ),
                        post.user!!.FCM
                        ).also{
                            sendNotifications(it)
                        }
                    }
            } else {
                postUpdate.document(signedInUser!!.username)
                    .get()
                    .addOnSuccessListener {
                        postUpdate.document(signedInUser!!.username).delete()
                        button.colorFilter = LightingColorFilter(Color.BLACK, Color.BLACK)
                    }.addOnFailureListener {
                        postUpdate.document(signedInUser!!.username).set(signedInUser!!)
                        if(post.user?.userID != signedInUser!!.userID){
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
    override fun commentButton(post: Posts) {
            val intent = Intent(this, ClickedPost::class.java)
            intent.putExtra(EXTRA_POST_ID, post.PostId)
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)


    }

    override fun goToProfile(user: Users?) {
        val intent = Intent(this, ProfilePage::class.java)
        intent.putExtra(EXTRA_USERNAME, user?.username)
        intent.putExtra(EXTRA_USER_ID, user?.userID)
        startActivity(intent)
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_CODE)
            if (resultCode == Activity.RESULT_OK) {
                photo_uri = data?.data
                binding.postImage.setImageURI(photo_uri)
                binding.postImage.isGone = false

            } else {
                Log.i(TAG, "Gallery Closed user cancelled")
            }
    }

    override fun delete(post: Posts) {
        firestoreDB.collection("Posts").document(post.PostId).delete()
        Log.i(TAG, "User deleted post")
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

