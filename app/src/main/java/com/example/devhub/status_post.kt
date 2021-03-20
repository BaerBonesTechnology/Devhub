package com.example.devhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.example.devhub.Model.Posts
import com.example.devhub.Model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.internal.api.FirebaseNoSignedInUserException
import kotlinx.android.synthetic.main.activity_status_post.*

private const val TAG = "PostActivity"
private lateinit var auth: FirebaseAuth
private lateinit var db:FirebaseFirestore
private var signedInUser: Users? = null

class status_post : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_post)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        db.collection("Users")
            .document(auth.currentUser.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot->
                signedInUser = userSnapshot.toObject(Users::class.java)
                Log.i(TAG, " signed in user: $signedInUser")

            }
            .addOnFailureListener{exception->
                Log.i(TAG, "Failure to fetch signed in user", exception)
            }



        status_post_btn.setOnClickListener {

            val postUpdate = postEdit.editableText.toString()

            val newPost = Posts(creation_time = System.currentTimeMillis(),
                description = postUpdate, image_url = "", user = signedInUser
            )

            db.collection("Posts")
                .add(newPost)
                .addOnSuccessListener {
                    Log.d(TAG, "Post Made written with ID: $signedInUser")
                    Toast.makeText(baseContext, "Posting succeeded", Toast.LENGTH_SHORT).show()


                    val userNewInfo = Users(signedInUser!!.username, signedInUser!!.age, signedInUser!!.doots, signedInUser!!.posts.plus(1), bio = signedInUser!!.bio )



                    db.collection("Users")
                        .document(auth.currentUser.uid)
                        .set(userNewInfo)
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)}


                    val intent = Intent(this, HomePage::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener{exception->
                    Log.e(TAG, "Error creating post", exception)
                    Toast.makeText(baseContext, "Posting failed", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, HomePage::class.java)
                    startActivity(intent)
                }






        }
    }
}