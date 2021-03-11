package com.example.devhub.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.example.devhub.HomePage
import com.example.devhub.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()




        val currUser = auth.currentUser
        if (currUser != null) {
            //Send to 'News Feed'

            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)

            Toast.makeText(baseContext, "Welcome @$emailLogin", Toast.LENGTH_SHORT).show()
        }

            loginBtn.setOnClickListener {

                val email = emailLogin.text.toString()
                val password = passwordLogin.text.toString()

                if (TextUtils.isEmpty(emailLogin.text.toString()) || TextUtils.isEmpty(passwordLogin.text.toString())) {
                    //if user and/or password is null send toast
                    Toast.makeText(baseContext, "please fill in all the fields", Toast.LENGTH_LONG).show()
                }
                else
                {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //Sign in success
                        val user = auth.currentUser

                        val intent = Intent(this, HomePage::class.java)
                        startActivity(intent)

                        Toast.makeText(baseContext, "Successfully Logged In", Toast.LENGTH_LONG).show()

                        finish()
                    } else {
                        //Sign in failure
                        Toast.makeText(baseContext, "Login Failed", Toast.LENGTH_LONG).show()
                    }
                })

                }
            }
        }
    }

