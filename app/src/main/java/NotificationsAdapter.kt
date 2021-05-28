package com.example.devhub

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.devhub.model.UserNotification
import kotlinx.android.synthetic.main.notification_adapter.view.*

class NotificationsAdapter(val context: Context, private var notifications: MutableList<UserNotification>, private var notificationClick: GoToPost):
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>(){
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){

        @SuppressLint("SetTextI18n")
        fun bind(notification: UserNotification){

            itemView.notificationText.text = notification.body
            itemView.timeView.text = DateUtils.getRelativeTimeSpanString(notification.time)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notification_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
        holder.itemView.setOnClickListener { notificationClick.clickedNotification(notification) }

    }

    override fun getItemCount()= notifications.size

    interface GoToPost{
        fun clickedNotification (notification: UserNotification)
    }
}