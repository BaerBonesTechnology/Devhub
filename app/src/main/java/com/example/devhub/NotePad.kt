package com.example.devhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.devhub.model.Notes
import com.example.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_note_pad.*

private var signedInUser: Users? = null
private const val TAG = "Note Post Activity"
private lateinit var auth:FirebaseAuth
private lateinit var db:FirebaseFirestore
private const val EXTRA_USERNAME = "EXTRA_USERNAME"


class NotePad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_pad)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        //TODO: Get signed in user and username
        db.collection("Users")
                .document(auth.currentUser.uid)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    signedInUser = userSnapshot.toObject(Users::class.java)
                    Log.i(TAG, " signed in user: $signedInUser")

                }


                .addOnFailureListener { exception ->
                    Log.i(TAG, "Failure to fetch signed in user", exception)
                }



        cancelButton.setOnClickListener {
            val intent = Intent(this, CodingNotes::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }
        saveButton.setOnClickListener {

            if(!saveButton.isEnabled){
                Toast.makeText(baseContext, "Please wait for note to finish saving", Toast.LENGTH_SHORT).show()
            }
            saveButton.isEnabled = false



            val notes = Notes(
                    signedInUser,
                    NoteTitle.editableText.toString(),
                    System.currentTimeMillis(),
                    noteContent.editableText.toString()
            )

            db.collection("notes")
                    .add(notes)
                    .addOnCompleteListener { postCreationTask ->

                        if (!postCreationTask.isSuccessful) {
                            Log.e(TAG, "Error creating post", postCreationTask.exception)
                            Toast.makeText(baseContext, "Posting failed", Toast.LENGTH_SHORT)
                                    .show()




                        } else {
                            Log.d(TAG, "Note Made written with ID: $signedInUser")

                            Toast.makeText(baseContext, "Posting succeeded", Toast.LENGTH_SHORT)
                                    .show()
                        }

                        val intent = Intent(this, CodingNotes::class.java)
                        intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                        startActivity(intent)

        }
    }
}
}