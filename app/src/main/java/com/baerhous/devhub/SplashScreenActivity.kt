package com.baerhous.devhub

import android.annotation.SuppressLint
import android.app.NotificationManager
import com.baerhous.devhub.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var auth: FirebaseAuth
@SuppressLint("StaticFieldLeak")
private lateinit var db: FirebaseFirestore
private var signedInUser: Users? = null
private var tokenCloudMessage: String = ""
private var tokenInAppMessaging: String = ""
private const val CHANNEL_ID = ""
private lateinit var notificationManager: NotificationManager

