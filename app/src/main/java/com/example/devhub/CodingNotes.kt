package com.example.devhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.devhub.Model.Posts
import com.example.devhub.com.example.devhub.Model.Notes
import kotlinx.android.synthetic.main.activity_coding_notes.*
import kotlinx.android.synthetic.main.activity_home_page.*

private lateinit var notes:MutableList<Notes>
private lateinit var adapter: NotesAdapter

class CodingNotes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coding_notes)

        //TODO: Create Layout File
        //TODO: Create Data Source
        notes = mutableListOf()
        //TODO: Create Adapter
        adapter = NotesAdapter(this, notes)
        //TODO: Bind Adapter and layout manager to the RV
        NotesView.adapter = adapter
        NotesView.layoutManager = LinearLayoutManager(this)


        addNotesButton.setOnClickListener {
            val intent = Intent(this, NotePad::class.java)
            startActivity(intent)
        }
    }
}