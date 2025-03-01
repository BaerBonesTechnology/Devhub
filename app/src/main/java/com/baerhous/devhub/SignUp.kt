package com.baerhous.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baerhous.devhub.databinding.ActivityMainBinding
import com.baerhous.devhub.model.Notification
import com.baerhous.devhub.model.Users
import com.baerhous.devhub.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


private const val TAG = "AccountActivity"
private lateinit var auth:FirebaseAuth

@SuppressLint("StaticFieldLeak")
private lateinit var firebaseDB: FirebaseFirestore

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null){
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
            finish()
        }

        firebaseDB = FirebaseFirestore.getInstance()
            binding.RegisterButton.setOnClickListener {
                val username = binding.UsernameView.text.toString()
            val email = binding.EmailView.text.toString()
            val password = binding.PasswordView.text.toString()


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


                        val intent = Intent(this, HomePage::class.java)

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
        binding.SwitchToLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }

        setContentView(binding.root)
    }

}