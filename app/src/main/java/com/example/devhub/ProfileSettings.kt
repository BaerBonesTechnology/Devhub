package com.example.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.devhub.model.Posts
import com.example.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_profile_settings.*

private var signedInUser: Users? = null
private lateinit var auth: FirebaseAuth

@SuppressLint("StaticFieldLeak")
private lateinit var db: FirebaseFirestore

private const val TAG: String = "PROFILE_UPDATE"
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
class ProfileSettings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        db.collection("Users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { userSnapshot->
                signedInUser = userSnapshot.toObject(Users::class.java)
            }

        save_Button.setOnClickListener {

            save_Button.isEnabled = false

            db.collection("Posts")
                .whereEqualTo("user.userID", signedInUser?.userID)
                .get()
                .addOnSuccessListener {

                    val postRef = it.documents

                    postRef.forEach{ docSnap ->
                        val post = docSnap.toObject(Posts::class.java)

                        post?.user?.username = usernameChangeTxt.editableText.toString()

                        db.collection("Posts").document(docSnap.id).set(post!!)
                    }

                }.addOnFailureListener{
                    Log.e(TAG, "post update failed cant get posts", it)
                }


                if (usernameChangeTxt.editableText.isNotBlank() && usernameChangeTxt.editableText.toString() != signedInUser?.username) {
                    signedInUser?.username = usernameChangeTxt.editableText.toString()
                }
                if (bioChangeTxt.editableText.isNotBlank() && bioChangeTxt.editableText.toString() != signedInUser?.bio) {
                    signedInUser?.bio = bioChangeTxt.editableText.toString()
                }

                db.collection("Users").document(auth.currentUser!!.uid)
                    .set(signedInUser!!)
                    .addOnSuccessListener {
                        Log.i(
                            TAG,
                            "${signedInUser?.username} is now has the username ${signedInUser?.username} and the bio ${signedInUser?.bio}"
                        )
                    }.addOnFailureListener { exception ->

                        Log.e(TAG, "Error setting user information", exception)
                    }




            val intent = Intent(baseContext, ProfilePage::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }


    }
}