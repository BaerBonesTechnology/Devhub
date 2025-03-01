package com.baerhous.devhub

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.baerhous.devhub.databinding.NotificationAdapterBinding
import com.baerhous.devhub.model.UserNotification

class NotificationsAdapter(val context: Context, private var notifications: MutableList<UserNotification>, private var notificationClick: GoToPost):
    RecyclerView.Adapter<NotificationsAdapter.ViewHolder>(){
    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){

        private lateinit var binding: NotificationAdapterBinding

        @SuppressLint("SetTextI18n")
        fun bind(notification: UserNotification){

            binding.notificationText.text = notification.body
            binding.timeView.text = DateUtils.getRelativeTimeSpanString(notification.time)
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