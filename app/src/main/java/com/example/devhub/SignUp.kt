package com.example.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.devhub.model.Notification
import com.example.devhub.model.Users
import com.example.devhub.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*


private const val TAG = "AccountActivity"
private lateinit var auth:FirebaseAuth

@SuppressLint("StaticFieldLeak")
private lateinit var firebaseDB: FirebaseFirestore

class SignUp : AppCompatActivity() {


        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        firebaseDB = FirebaseFirestore.getInstance()
        RegisterButton.setOnClickListener {
            val username = UsernameView.text.toString()
            val email = EmailView.text.toString()
            val password = PasswordView.text.toString()


            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val currentUser = auth.currentUser
                        // Updates the user attributes

                        val newUser = Users(username, 18, 0, 0, "", "", currentUser!!.uid)
                        firebaseDB.collection("Users").document(currentUser.uid)
                                .set(newUser)
                                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                        Log.d("Create User Successful", "$email has been added")
                        Toast.makeText(baseContext, "profile @$username succesfully created.",
                                Toast.LENGTH_SHORT).show()


                        val intent = Intent(this, SplashScreenActivity::class.java)

                        startActivity(intent)

                        val welcome = Notification(
                                "Welcome to devHub",
                            "Where you can share you creatives with other creatives"
                        )
                        firebaseDB.collection("Users").document(currentUser.uid)
                                .collection("Notifications").add(welcome)


                    } else {
                        //If sign in fails, display a message to the user to try again

                        Log.w("createUserW/Email:fail", task.exception)
                        Toast.makeText(
                                baseContext, "Authentication Failed.",
                                Toast.LENGTH_SHORT
                        ).show()
                    }

                }


        }
        SwitchToLogin.setOnClickListener{

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }


    }

}