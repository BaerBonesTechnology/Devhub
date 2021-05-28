package com.example.devhub

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random


private const val TAG = "CLOUD MESSAGING INFO"
private const val CHANNEL_ID = "my channel"
private lateinit var notificationManager: NotificationManager


class MyFirebaseMessagingService : FirebaseMessagingService()
{
    companion object{
        private var sharedPreferences: SharedPreferences? = null

        var token: String?
        get() {
            return sharedPreferences?.getString("token", "")
        }
        set(value) {
            sharedPreferences?.edit()?.putString("token", value)?.apply()
        }
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.i(TAG, p0.data.toString())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            notifyThis(p0.data["title"], p0.data["body"])
        }


        Log.d(TAG, "From: "+ p0.from)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        token = p0
        Log.d("TAG","Token refreshed: $p0")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val notificationID = Random(System.currentTimeMillis()).nextInt()

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
            val name = "DevHub"
            val description = "this is the notification Channel"
            val importance = IMPORTANCE_HIGH
            val channel = NotificationChannel(notificationID.toString(), name, importance)
            channel.description = description

            notificationManager = getSystemService(
                NOTIFICATION_SERVICE
            ) as NotificationManager

            notificationManager.createNotificationChannel(channel)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun notifyThis(title: String?, message: String?) {

        val intent = Intent(this, HomePage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_ONE_SHOT)

        val mBuilder =  Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notifications)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager = getSystemService(
            NOTIFICATION_SERVICE
        ) as NotificationManager
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, mBuilder.build())
    }



}