package com.example.devhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.devhub.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        RegisterButton.setOnClickListener {
            val email = EmailView.text.toString()
            val password = PasswordView.text.toString()

            Log.d("MainActivity", "Email is $email")
            Log.d("MainActivity", "Password is $password")

        }

        SwitchToLogin.setOnClickListener{

            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)
        }


    }

}