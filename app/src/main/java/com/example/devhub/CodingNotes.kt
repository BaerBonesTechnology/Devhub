package com.example.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.devhub.com.example.devhub.model.Notes
import com.example.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_coding_notes.*
import kotlinx.android.synthetic.main.notes_adapter.*

private lateinit var notes:MutableList<Notes>
private lateinit var adapter: NotesAdapter
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
private var signedInUser: Users? = null
private const val TAG = "NotesActivity"


class CodingNotes : AppCompatActivity(), NotesAdapter.ClickListener {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coding_notes)

        auth = FirebaseAuth.getInstance()


        //TODO: Create Layout File
        //TODO: Create Data Source
        notes = mutableListOf()
        //TODO: Create Adapter
        adapter = NotesAdapter(this, notes, this)
        //TODO: Bind Adapter and layout manager to the RV
        NotesView.adapter = adapter
        NotesView.layoutManager = LinearLayoutManager(this)


        db = FirebaseFirestore.getInstance()

        db.collection("Users")
                .document(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    signedInUser = userSnapshot.toObject(Users::class.java)
                    Log.i(TAG, " signed in user: $signedInUser")
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, "Failure to fetch signed in user", exception)
                }

        val username = intent.getStringExtra(EXTRA_USERNAME)


        val notesRef = db
                .collection("notes")
                .limit(20)
                .orderBy("note_time_Created", Query.Direction.DESCENDING)
                .whereEqualTo("user.username", signedInUser?.username)



        notesRef.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }

            val notesList = snapshot.toObjects(Notes::class.java)

            notes.clear()
            notes.addAll(notesList)
            adapter.notifyDataSetChanged()
            
            for (notes in notesList) {
                Log.i(
                        TAG,
                        "Notes $notes"
                )
            }
        }



        addNotesButton.setOnClickListener {
            val intent = Intent(this, NotePad::class.java)
            startActivity(intent)
        }

        homeBtn.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }
        profileBtn.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }
        codingBtn.setOnClickListener {
            val intent = Intent(this, CodingNotes::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }


    }

    override fun clickedItem(Notes: Notes, textView: TextView) {
        //TODO send information from note to notepad
        textView.isGone = !textView.isGone
    }
}