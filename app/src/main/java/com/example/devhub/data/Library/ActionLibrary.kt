package com.example.devhub.data.Library

import android.annotation.SuppressLint
import android.util.Log
import com.example.devhub.model.Notification
import com.example.devhub.model.Posts
import com.example.devhub.model.Users
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@SuppressLint("StaticFieldLeak")
private lateinit var db: FirebaseFirestore


open class ActionLibrary {

    fun sendNotification(signedInUser: Users?, post: Posts?, activity: String, userNotified:Users?){
        if(signedInUser?.userID != userNotified?.userID) {
            db = FirebaseFirestore.getInstance()


            val notification = Notification(
                "Devhub",
                signedInUser!!.username + activity,
                //System.currentTimeMillis(),
                //post!!.PostId

            )
            db.collection("Users").document(userNotified!!.userID).collection("Notifications")
                .add(notification)
                .addOnSuccessListener {
                    Log.d("Notification Activity", "Notification ${it.id} Sent")
                }.addOnFailureListener {
                    Log.e("Notification Activity", "Notification Failure", it)
                }
        }
    }


    fun getPosts(): Query {
        db = FirebaseFirestore.getInstance()
        return db.collection("Posts").orderBy("creation_time", Query.Direction.DESCENDING)
    }

    // fun dootsCheck(documentReference: DocumentReference?, users: Users?): Boolean {
 //     var isDooted = false

 //     documentReference?.collection("LikedBy")?.get()!!
 //             .addOnSuccessListener({
 //         Log.d("LIKED_QUERY", "Getting document")
 //         if (it.isEmpty) {
 //             Log.d("LIKED_QUERY", "Collections are empty")
 //         } else {
 //             val list = it.documents
 //             for (documents in list) {
 //                 if (documents.id == users?.username) {
 //                     isDooted = true
 //                 }
 //             }

 //         }
 //     }).addOnFailureListener {
 //         Log.d("LIKED_QUERY", "Error getting document", it)
 //     }
 //         return isDooted
 // }

    }