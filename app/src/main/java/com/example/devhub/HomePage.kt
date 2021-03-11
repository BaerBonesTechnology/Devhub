package com.example.devhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_page.*

class HomePage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        LogoutBtn.setOnClickListener {

            Firebase.auth.signOut()

            val intent = Intent(this, MainActivity::class.java)

            startActivity(intent)

            Toast.makeText(baseContext, "You have been logged out", Toast.LENGTH_LONG).show()
        }


    }
}