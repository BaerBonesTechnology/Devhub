package com.example.devhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.devhub.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth


        //if user is logged in send to Home screen
        val currUser = auth.currentUser
        if (currUser != null) {
            //Send to 'News Feed'

            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)

            Toast.makeText(baseContext, "Welcome @$emailLogin", Toast.LENGTH_SHORT).show()
        }

        RegisterButton.setOnClickListener {
            val username = UsernameView.text.toString()
            val email = EmailView.text.toString()
            val password = PasswordView.text.toString()


            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task->
                    if(task.isSuccessful){

                        val currentUser = auth.currentUser
                        // Updates the user attributes

                        Log.d("Create User Successful", "$email has been added")
                        Toast.makeText(baseContext, "profile @$username succesfully created.",
                                Toast.LENGTH_SHORT).show()




                        val intent = Intent(this, LoginActivity::class.java)

                        startActivity(intent)

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