package com.example.devhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.devhub.Model.Posts
import com.example.devhub.com.example.devhub.Model.Notes
import kotlinx.android.synthetic.main.activity_note_pad.*


class NotePad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_pad)

        cancelButton.setOnClickListener {
            val intent = Intent(this, CodingNotes::class.java)
            startActivity(intent)
        }
        saveButton.setOnClickListener {
            val intent = Intent(this, CodingNotes::class.java)
            startActivity(intent)
        }
    }
}