package com.example.devhub.com.example.devhub.data.Library

import android.util.Log
import com.example.devhub.CommentsAdapter
import com.example.devhub.com.example.devhub.model.Comment
import com.example.devhub.com.example.devhub.model.Notification
import com.example.devhub.model.Posts
import com.example.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

private lateinit var auth: FirebaseAuth
private lateinit var db: FirebaseFirestore


open class ActionLibrary {

    fun sendNotification(signedInUser: Users?, post: Posts?, activity: String, userNotified:Users?){
        db = FirebaseFirestore.getInstance()

        if(signedInUser!!.userID != userNotified!!.userID){
            val notification = Notification(signedInUser, post, "${signedInUser.username} $activity", System.currentTimeMillis())
            db.collection("Users").document(userNotified.userID).collection("Notifications").add(notification)
                    .addOnSuccessListener {
                        Log.d("Notification Activity", "Notification ${it.id} Sent")
                    }.addOnFailureListener{
                        Log.e("Notification Activity", "Notification Failure", it)
                    }

        }else{
            Log.i("Notification Activity", "User liked their own post")
        }
    }


    fun sendComment(text: String, post: Posts?, signedInUser: Users?){
        db = FirebaseFirestore.getInstance()

        val newComment = Comment(signedInUser, System.currentTimeMillis(), text, 0)

        db.collection("Posts").document(post!!.PostId).collection("Comments").add(newComment)
                .addOnCompleteListener { commentCreationTask ->
                    if (!commentCreationTask.isSuccessful)
                        Log.e("Comment Activity", "error creating post", commentCreationTask.exception)
                else{
                    Log.d("Comment Activity", "Comment Made written with ID: $signedInUser")
                     }
                }
    }

    fun sendDoot(postRef: DocumentReference?, signedInUser: Users, isDooted: Boolean) {

        var post: Posts? = null

        postRef?.get()?.addOnSuccessListener { postSnapshot ->

            post = postSnapshot.toObject(Posts::class.java)
            if(post != null){
                if(isDooted && post!!.doots > 0){
                    postRef.collection("LikedBy")
                        .document(signedInUser.username).delete()
                    post!!.doots--
                }else{
                    postRef.collection("LikedBy")
                        .document(signedInUser.username).set(signedInUser)
                    post!!.doots++
                }
            }
        }



        postRef?.collection("LikedBy")?.document(signedInUser.username)?.set(post!!)!!.addOnSuccessListener {
            Log.d("Doot Activity", "Updooted by ${signedInUser.username}")
        }?.addOnFailureListener{e ->
            Log.d("Doot Activity", "Doot Error", e)
        }

    }

    fun getPosts(): Query {
        db = FirebaseFirestore.getInstance()
        return db.collection("Posts").orderBy("creation_time", Query.Direction.DESCENDING)
    }

    fun getUser(): Users? {

        var user  = Users()
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        db.collection("Users")
                .document(auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener {
                   user = it.toObject(Users::class.java)!!
                    Log.i("User Activity", " signed in user: $user")

                }
                .addOnFailureListener { exception ->
                    Log.i("User Activity", "Failure to fetch signed in user", exception)
                }

        Log.i("USER", "returns $user")
        return user
    }

    fun getComments( postRef: DocumentReference?, commentList: MutableList<Comment>, adapter: CommentsAdapter){

    }

    fun getPostSpecific(PostId:String): DocumentReference {
        db = FirebaseFirestore.getInstance()
        return db.collection("Posts").document(PostId)
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