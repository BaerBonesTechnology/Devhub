package com.baerhous.devhub.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baerhous.devhub.HomePage
import com.baerhous.devhub.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()



            binding.loginBtn.setOnClickListener {

                val email = binding.emailLogin.text.toString()
                val password = binding.passwordLogin.text.toString()

                if (TextUtils.isEmpty(binding.emailLogin.text.toString()) || TextUtils.isEmpty(binding.passwordLogin.text.toString())) {
                    //if user and/or password is null send toast
                    Toast.makeText(baseContext, "please fill in all the fields", Toast.LENGTH_LONG).show()
                }
                else
                {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //Sign in success
                        val user = auth.currentUser

                        val intent = Intent(this, HomePage::class.java)
                        startActivity(intent)

                        Toast.makeText(baseContext, "Successfully Logged In", Toast.LENGTH_LONG)
                            .show()

                        finish()
                    } else {
                        //Sign in failure
                        Toast.makeText(baseContext, "Login Failed", Toast.LENGTH_LONG).show()
                    }
                }

                }
            }
        setContentView(binding.root)
    }
    }

