package com.baerhous.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.baerhous.devhub.NotificationsAdapter
import com.baerhous.devhub.model.UserNotification
import com.baerhous.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_home_page.*
import kotlinx.android.synthetic.main.activity_notifications.*



@SuppressLint("StaticFieldLeak")
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth
private var signedInUser: Users? = null
private lateinit var notifications: MutableList<UserNotification>
private const val TAG = "Notification Activity"
@SuppressLint("StaticFieldLeak")
private lateinit var adapter: NotificationsAdapter
private const val EXTRA_POST_ID = "EXTRA_POST_ID"
private var userNotification: UserNotification? = null
private const val EXTRA_USERNAME = "EXTRA_USERNAME"
private const val EXTRA_USER_ID = "EXTRA_USERID"



class NotificationPage : AppCompatActivity(), NotificationsAdapter.GoToPost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val userID = intent.getStringExtra(EXTRA_USER_ID)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        notifications = mutableListOf()

        adapter = NotificationsAdapter(this, notifications, this)
        notifAdapter.adapter = adapter

        notifAdapter.layoutManager = LinearLayoutManager(this)


        Log.i(TAG, "$userID is the user ID")

            db.collection("Users").document(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener {
                    signedInUser = it.toObject(Users::class.java)
                    Log.i(TAG, "signed in user is $signedInUser")

                    val notificationsRef = db.collection("Users").document(signedInUser!!.userID)
                        .collection("Notifications").orderBy("time", Query.Direction.DESCENDING)


                    notificationsRef.addSnapshotListener { snapshot, exception ->
                        if (exception != null || snapshot == null) {
                            Log.e(TAG, "Exception when querying notifications", exception)
                            return@addSnapshotListener
                        }
                        val notificationLists = snapshot.documents

                        notifications.clear()
                        notificationLists.forEach {
                            userNotification = it.toObject(UserNotification::class.java)

                            if (userNotification != null) {
                                notifications.add(userNotification!!)
                                Log.i(TAG, userNotification!!.body)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, "Failure to fetch signed in user", exception)
                }







        this.NPost_Btn.setOnClickListener {
            val intent = Intent(this, StatusPost::class.java)
            startActivity(intent)
        }

        this.Nnav_Profile.setOnClickListener {
            val intent = Intent(this, ProfilePage::class.java)
            intent.putExtra(EXTRA_USER_ID, signedInUser?.userID)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)

        }
        this.Nnav_Home.setOnClickListener {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }

        this.Nnav_Coding.setOnClickListener {
            val intent = Intent(this, CodingNotes::class.java)
            intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
            startActivity(intent)
        }
        this.Nnav_Notifs.setOnClickListener {
            val intent = Intent(this, NotificationPage::class.java)
            intent.putExtra(EXTRA_USER_ID, signedInUser?.userID)
            startActivity(intent)
        }

    }

    override fun clickedNotification (notification: UserNotification){
        if(notification.postID != ""){
        val intent = Intent(this, ClickedPost::class.java)
        intent.putExtra(EXTRA_POST_ID, notification.postID)
        startActivity(intent)}
        else{
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        }
    }
}