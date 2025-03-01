package com.baerhous.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.baerhous.devhub.databinding.ActivityProfileSettingsBinding
import com.baerhous.devhub.model.Posts
import com.baerhous.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private var signedInUser: Users? = null
private lateinit var auth: FirebaseAuth

@SuppressLint("StaticFieldLeak")
private lateinit var db: FirebaseFirestore

private const val TAG: String = "PROFILE_UPDATE"
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
class ProfileSettings : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        db.collection("Users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { userSnapshot->
                signedInUser = userSnapshot.toObject(Users::class.java)
            }

        binding.saveButton.setOnClickListener {

            binding.saveButton.isEnabled = false

            db.collection("Posts")
                .whereEqualTo("user.userID", signedInUser?.userID)
                .get()
                .addOnSuccessListener {

                    val postRef = it.documents

                    postRef.forEach{ docSnap ->
                        val post = docSnap.toObject(Posts::class.java)

                        post?.user?.username =  binding.usernameChangeTxt.editableText.toString()

                        db.collection("Posts").document(docSnap.id).set(post!!)
                    }

                }.addOnFailureListener{
                    Log.e(TAG, "post update failed cant get posts", it)
                }


                if ( binding.usernameChangeTxt.editableText.isNotBlank() &&  binding.usernameChangeTxt.editableText.toString() != signedInUser?.username) {
                    signedInUser?.username =  binding.usernameChangeTxt.editableText.toString()
                }
                if ( binding.bioChangeTxt.editableText.isNotBlank() &&  binding.bioChangeTxt.editableText.toString() != signedInUser?.bio) {
                    signedInUser?.bio =  binding.bioChangeTxt.editableText.toString()
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

        setContentView(binding.root)
    }
}