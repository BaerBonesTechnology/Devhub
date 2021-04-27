package com.example.devhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.devhub.com.example.devhub.model.Notification
import com.example.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_notifications.*


@SuppressLint("StaticFieldLeak")
private lateinit var db: FirebaseFirestore
private lateinit var auth: FirebaseAuth
private var signedInUser: Users? = null
private lateinit var notifications: MutableList<Notification>
private const val TAG = "Notification Activity"
@SuppressLint("StaticFieldLeak")
private lateinit var adapter: NotificationsAdapter
private const val EXTRA_POST_ID = "EXTRA_POST_ID"
private var notification: Notification? = null


class Notifications : AppCompatActivity(), NotificationsAdapter.GoToPost {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        notifications = mutableListOf()
        adapter = NotificationsAdapter(this, notifications, this)
        notifAdapter.adapter = adapter
        notifAdapter.layoutManager = LinearLayoutManager(this)


        db.collection("Users").document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { userSnapshot->
                signedInUser = userSnapshot.toObject(Users::class.java)
            }

        val notificationsRef = db.collection("Users").document(auth.currentUser!!.uid)
                .collection("Notifications").orderBy("time", Query.Direction.DESCENDING)


        notificationsRef.addSnapshotListener{snapshot, exception ->
            if(exception != null || snapshot == null){
                Log.e(TAG, "Exception when querying notifications", exception)
                return@addSnapshotListener
            }
            val notificationLists = snapshot.documents

            notifications.clear()
            notificationLists.forEach{
                notification = it.toObject(Notification::class.java)

                if(notification != null){
                    notifications.add(notification!!)
                    adapter.notifyDataSetChanged()
                }
            }
        }


    }

    override fun clickedNotification (notification: Notification){
        val intent = Intent(this, ClickedPost::class.java)
        intent.putExtra(EXTRA_POST_ID, notification.userPost?.PostId)
        startActivity(intent)
    }

}