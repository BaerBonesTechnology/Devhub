package com.baerhous.devhub

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baerhous.devhub.databinding.ActivityStatusPostBinding
import com.baerhous.devhub.model.Posts
import com.baerhous.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private const val TAG = "PostActivity"
private lateinit var auth: FirebaseAuth
private lateinit var db:FirebaseFirestore
private lateinit var storage:StorageReference
private var signedInUser: Users? = null
private const val PICK_PHOTO_CODE = 1234
private var photo_uri: Uri? = null
private var url = ""


class StatusPost : AppCompatActivity() {
    private lateinit var binding: ActivityStatusPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatusPostBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance().reference


        db.collection("Users")
            .document(auth.currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(Users::class.java)
                Log.i(TAG, " signed in user: $signedInUser")

            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failure to fetch signed in user", exception)
            }

        binding.pictureAddButton.setOnClickListener {
            Log.i(TAG, "Open up gallery on device")

            val galleryImageIntent = Intent(Intent.ACTION_GET_CONTENT)

            galleryImageIntent.type = "image/*"
            if (galleryImageIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(galleryImageIntent, PICK_PHOTO_CODE)
            }

        }



        binding.statusPostBtn.setOnClickListener {
            val photoRef = storage.child("images/${System.currentTimeMillis()}-photo.jpg")

            if (photo_uri == null &&  binding.postEdit.text.isBlank()) {
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
                            binding.postEdit.editableText.toString(),
                            photoDownloadTask.result.toString(),
                            signedInUser
                        )
                        photo_uri = null


                        db.collection("Posts").add(posts)
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



                                    db.collection("Users")
                                        .document(auth.currentUser?.uid ?: "")
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
                    binding.postEdit.editableText.toString(),
                    url,
                    signedInUser
                )

                db.collection("Posts").add(posts)
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



                            db.collection("Users")
                                .document(auth.currentUser?.uid ?: "")
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
        setContentView(binding.root)
    }

            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                super.onActivityResult(requestCode, resultCode, data)
                if (requestCode == PICK_PHOTO_CODE)
                    if (resultCode == Activity.RESULT_OK) {
                        photo_uri = data?.data
                        binding.imageView.setImageURI(photo_uri)

                    } else {
                        Log.i(TAG, "Gallery Closed user cancelled")
                    }
            }
        }