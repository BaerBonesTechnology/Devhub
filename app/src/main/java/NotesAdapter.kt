package com.example.devhub

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.devhub.com.example.devhub.model.Notes
import kotlinx.android.synthetic.main.notes_adapter.view.*


class NotesAdapter(val context: Context, private val Notes: List<Notes>, private var clickListener: ClickListener):RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    inner class ViewHolder(NotesPost: View): RecyclerView.ViewHolder(NotesPost){


        @SuppressLint("SetTextI18n")
        fun bind(Notes: Notes){
            itemView.txtNote.text = Notes.note_title
            itemView.txt_dateLastUpdated.text = DateUtils.getRelativeTimeSpanString(Notes.note_time_Created)
            itemView.txt_Content.text = Notes.note_Content

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notes_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val note = Notes[holder.layoutPosition]
        holder.bind(note)
        holder.itemView.setOnClickListener { clickListener.clickedItem(note, holder.itemView.txt_Content) }

    }

    override fun getItemCount() = Notes.size

    interface ClickListener{
        fun clickedItem(Notes: Notes, textView: TextView)
    }
}