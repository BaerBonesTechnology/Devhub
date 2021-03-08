package com.example.devhub

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.devhub.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if(currentUser != null){

        }


        RegisterButton.setOnClickListener {
            val username = UsernameView.text.toString()
            val email = EmailView.text.toString()
            val password = PasswordView.text.toString()
            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task->
                    if(task.isSuccessful){
                       Log.d("Create User Successful", "$email has been added")
                        val user = auth.currentUser
                    }else{
                        //If sign in fails, display a message to the user to try again
                        Log.w("createUserW/Email:fail", task.exception)
                        Toast.makeText(baseContext, "Authentication Failed.",
                        Toast.LENGTH_SHORT).show()
                    }
                }


        }

        SwitchToLogin.setOnClickListener{

            val intent = Intent(this, LoginActivity::class.java)

            startActivity(intent)
        }


    }

}