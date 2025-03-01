package com.baerhous.devhub

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baerhous.devhub.model.Users
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_splash_screen.*

private lateinit var auth: FirebaseAuth
@SuppressLint("StaticFieldLeak")
private lateinit var db: FirebaseFirestore
private var signedInUser: Users? = null
private var tokenCloudMessage: String = ""
private var tokenInAppMessaging: String = ""
private const val CHANNEL_ID = ""
private lateinit var notificationManager: NotificationManager



@Suppress("DEPRECATION")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            tokenCloudMessage = task.result

            Log.d("TAG", tokenCloudMessage)

        })

        FirebaseInstallations.getInstance().id.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "FIAM ID failed to gather", task.exception)
                return@OnCompleteListener
            }

            tokenInAppMessaging = task.result

            Log.d("Installation ID", tokenInAppMessaging)
        })




        splashScreenLogo2.alpha = 0f

        if (auth.currentUser != null) {
            db.collection("Users")
                .document(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    signedInUser = userSnapshot.toObject(Users::class.java)
                    Log.i("User Activity", " signed in user: ${signedInUser?.username}")
                    signedInUser?.FCM = tokenCloudMessage
                    signedInUser?.FIAM = tokenInAppMessaging
                    signedInUser?.userID = auth.currentUser!!.uid

                }
                .addOnFailureListener { exception ->
                    Log.i("User Activity", "Failure to fetch signed in user", exception)
                }



        }

            splashScreenLogo2.animate().setDuration(1500).alpha(1f).withEndAction {
                if (auth.currentUser != null) {
                    Log.i("USER", "returns $signedInUser")

                    val intent = Intent(this, HomePage::class.java)
                    startActivity(intent)

                    db.collection("Users").document(auth.currentUser!!.uid).set(signedInUser!!).addOnSuccessListener {
                        Log.i("User file", "Updated")
                    }.addOnFailureListener {
                        Log.e("USER FILE", "Error updating file")
                    }

                    Toast.makeText(
                        baseContext,
                        "Welcome _${signedInUser?.username}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val intent = Intent(this, SignUp::class.java)
                    startActivity(intent)
                }
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }
