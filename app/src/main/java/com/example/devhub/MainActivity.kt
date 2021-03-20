package com.example.devhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.devhub.Model.Users
import com.example.devhub.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*


private const val TAG = "AccountActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth
    private lateinit var firebaseDB: FirebaseFirestore
    private var signedInUser: Users? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseDB = FirebaseFirestore.getInstance()

        auth = Firebase.auth


        //if user is logged in send to Home screen
        val currUser = auth.currentUser

        if (currUser != null) {
            //Send to 'News Feed'
            firebaseDB.collection("Users")
                .document(currUser.uid as String)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    signedInUser= userSnapshot.toObject(Users::class.java)
                    Log.i(TAG, " signed in user: $signedInUser")

                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, "Failure to fetch signed in user", exception)
                }

            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)

            Toast.makeText(baseContext, "Welcome _${signedInUser?.username}", Toast.LENGTH_SHORT).show()
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

                        val newUser = Users(username, 18, 0, 0, "")
                        firebaseDB.collection("Users").document(currentUser.uid)
                            .set(newUser)
                            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)}

                        Log.d("Create User Successful", "$email has been added")
                        Toast.makeText(baseContext, "profile @$username succesfully created.",
                                Toast.LENGTH_SHORT).show()




                        val intent = Intent(this, LoginActivity::class.java)

                        startActivity(intent)

                    }else{
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