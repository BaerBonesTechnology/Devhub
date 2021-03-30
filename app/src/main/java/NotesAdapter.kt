package com.example.devhub

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.devhub.com.example.devhub.Model.Notes
import kotlinx.android.synthetic.main.notes_adapter.view.*


class NotesAdapter(val context: Context, val Notes: List<Notes>):RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    inner class ViewHolder(NotesPost: View): RecyclerView.ViewHolder(NotesPost){

        @SuppressLint("SetTextI18n")
        fun bind(Notes: Notes){
            itemView.txtNote.text = Notes.note_title
            itemView.txt_dateLastUpdated.text = DateUtils.getRelativeTimeSpanString(Notes.note_time_Created)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.notes_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(Notes[position])
    }

    override fun getItemCount()=Notes.size
}